package com.lljqiu.cmpp.smsgateway.handler;

import com.alibaba.fastjson.JSON;
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
import java.util.concurrent.atomic.LongAdder;

public class CmppServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(CmppServerHandler.class);

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 业务线程池（处理 submit/connect）
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
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    /**
     * 状态报告线程池
     */
    private static final ScheduledExecutorService REPORT_POOL =
            Executors.newScheduledThreadPool(
                    4,
                    r -> {
                        Thread t = new Thread(r);
                        t.setName("CMPP-REPORT-" + t.getId());
                        t.setDaemon(true);
                        return t;
                    }
            );

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        // 1️⃣ 零拷贝，使用 retainedDuplicate
        final ByteBuf retained = buf.retainedDuplicate();

        try {
            BUSINESS_POOL.execute(() -> {
                try {
                    process(ctx, retained);
                } finally {
                    retained.release(); // 处理完必须释放
                }
            });
        } catch (RejectedExecutionException e) {
            // 队列满，回退到 IO 线程处理
            log.warn("业务线程池已满，IO 线程直接处理请求");
            process(ctx, retained);
            retained.release();
        }
    }

    /**
     * 业务处理
     */
    private void process(ChannelHandlerContext ctx, ByteBuf buf) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), data);

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
                    log.info("srcId:{},sms content:{}",submit.getSrcId(),submit.getStrMsgContent());
                    long msgId = MsgIdGenerator.nextId();

                    byte[] resp = PutMsgService.setSubmitResp(submit, msgId);
                    write(ctx, resp);

                    scheduleReport(ctx, submit, msgId);
                    break;
                }
                case MsgCommand.CMPP_DELIVER_RESP: {
                    MsgDeliverResp deliverResp = ReadMsgService.readDeliverResp(data);
                    log.info("deliver resp {}", JSON.toJSONString(deliverResp));
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
     * 写响应，线程安全 + TPS 统计
     */
    private void write(ChannelHandlerContext ctx, byte[] data) {
        if (!ctx.channel().isActive()) {
            return;
        }

        ctx.writeAndFlush(Unpooled.wrappedBuffer(data))
                .addListener(f -> {
                    if (f.isSuccess()) {
                        TpsCounter.mark(); // ⭐ 无锁计数 TPS
                    } else {
                        log.error("发送失败", f.cause());
                    }
                });
    }

    /**
     * 延迟状态报告（独立线程池，避免 EventLoop 阻塞）
     */
    private void scheduleReport(ChannelHandlerContext ctx, MsgSubmit submit, long msgId) {
        final String now = LocalDateTime.now().format(TS_FMT);

        REPORT_POOL.schedule(() -> {
            if (!ctx.channel().isActive()) {
                return;
            }

            try {
                MsgDeliver report = MsgDeliver.createReport(
                        (String) submit.getDestTerminalId().get(0),
                        submit.getSrcId(),
                        msgId,
                        "DELIVRD"
                );

                ctx.writeAndFlush(Unpooled.wrappedBuffer(report.toBytes()))
                        .addListener(f -> {
                            if (!f.isSuccess()) {
                                log.error("发送状态报告失败", f.cause());
                            }
                            log.info("发送成功");
                        });
            } catch (Exception e) {
                log.error("生成状态报告失败", e);
            }
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("CMPP 连接异常", cause);
        ctx.close();
    }
}
