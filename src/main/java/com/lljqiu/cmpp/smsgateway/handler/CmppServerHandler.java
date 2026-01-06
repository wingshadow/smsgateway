package com.lljqiu.cmpp.smsgateway.handler;

import com.lljqiu.cmpp.smsgateway.service.PutMsgService;
import com.lljqiu.cmpp.smsgateway.service.ReadMsgService;
import com.lljqiu.cmpp.smsgateway.stack.*;
import com.lljqiu.cmpp.smsgateway.utils.GateWayUtils;
import com.lljqiu.cmpp.smsgateway.utils.MsgIdGenerator;
import com.lljqiu.cmpp.smsgateway.utils.TpsCounter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class CmppServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(CmppServerHandler.class);

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /**
     * 业务线程池（网关级）
     */
    private static final ExecutorService BUSINESS_POOL =
            new ThreadPoolExecutor(
                    8,
                    32,
                    60L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(20000),
                    r -> {
                        Thread t = new Thread(r);
                        t.setName("CMPP-BIZ-" + t.getId());
                        t.setDaemon(true);
                        return t;
                    },
                    new ThreadPoolExecutor.AbortPolicy()
            );

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        final byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        try {
            BUSINESS_POOL.execute(() -> process(ctx, data));
        } catch (RejectedExecutionException e) {
            // 线程池满：直接拒绝（网关允许）
            log.warn("业务线程池已满，丢弃请求");
        }
    }

    /**
     * 业务处理
     */
    private void process(ChannelHandlerContext ctx, byte[] data) {
        try {
            MsgHead head = ReadMsgService.readHead(data);

            switch (head.getCommandId()) {

                case MsgCommand.CMPP_CONNECT: {

                    MsgConnect req = ReadMsgService.readConnect(
                            data,
                            ((InetSocketAddress) ctx.channel().remoteAddress())
                                    .getAddress().getHostAddress()
                    );
                    byte[] resp = PutMsgService.setConnectResp(req);
                    write(ctx, resp);
                    break;
                }

                case MsgCommand.CMPP_SUBMIT: {
                    MsgSubmit submit = ReadMsgService.readSubmit(data);

                    long msgId = MsgIdGenerator.nextId();

                    // 1️⃣ 立即回 SubmitResp（CMPP 核心）
                    byte[] resp = PutMsgService.setSubmitResp(submit, msgId);
                    write(ctx, resp);
                    // 2️⃣ 延迟发状态报告（走 EventLoop）
//                    scheduleReport(ctx, submit, msgId);
                    break;
                }

                default:
                    log.warn("未知命令: 0x{}", Integer.toHexString(head.getCommandId()));
            }

        } catch (Exception e) {
            log.error("处理 CMPP 失败", e);
        }
    }

    /**
     * 写响应（线程安全）
     */
    private void write(ChannelHandlerContext ctx, byte[] data) {
        if (!ctx.channel().isActive()) {
            return;
        }
        ctx.writeAndFlush(Unpooled.wrappedBuffer(data))
                .addListener(f -> {
                    if (f.isSuccess()) {
                        TpsCounter.mark();   // ⭐ 真正的 TPS
                    } else {
                        log.error("发送失败", f.cause());
                    }
                });
    }


    /**
     * 延迟状态报告（一定走 EventLoop）
     */
    private void scheduleReport(ChannelHandlerContext ctx, MsgSubmit submit, long msgId) {
        String now = LocalDateTime.now().format(TS_FMT);
        ctx.channel().eventLoop().schedule(() -> {
            if (!ctx.channel().isActive()) {
                return;
            }
            try {
                MsgDeliver report = MsgDeliver.createReport(
                        (String) submit.getDestTerminalId().get(0),
                        submit.getSrcId(),
                        msgId,
                        "DELIVRD",
                        now,
                        now
                );
                report.setLinkId("GW");

                ctx.writeAndFlush(Unpooled.wrappedBuffer(report.generateBytes()));
            } catch (Exception e) {
                log.error("发送状态报告失败", e);
            }
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("CMPP 连接异常", cause);
        ctx.close();
    }
}

