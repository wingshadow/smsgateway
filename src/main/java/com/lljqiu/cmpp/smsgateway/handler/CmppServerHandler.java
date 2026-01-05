package com.lljqiu.cmpp.smsgateway.handler;

import com.lljqiu.cmpp.smsgateway.server.ReportSender;
import com.lljqiu.cmpp.smsgateway.service.PutMsgService;
import com.lljqiu.cmpp.smsgateway.service.ReadMsgService;
import com.lljqiu.cmpp.smsgateway.stack.MsgCommand;
import com.lljqiu.cmpp.smsgateway.stack.MsgConnect;
import com.lljqiu.cmpp.smsgateway.stack.MsgHead;
import com.lljqiu.cmpp.smsgateway.stack.MsgSubmit;
import com.lljqiu.cmpp.smsgateway.utils.MsgIdGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-05 09:36
 */
public class CmppServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(CmppServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {


        // 1️⃣ ByteBuf → byte[]
        byte[] requestData = new byte[buf.readableBytes()];
        buf.readBytes(requestData);

        // 2️⃣ 解析消息头（与你原来一致）
        MsgHead head = ReadMsgService.readHead(requestData);
        logger.debug("请求头消息: {}", ToStringBuilder.reflectionToString(head));

        byte[] resp = null;

        switch (head.getCommandId()) {

            case MsgCommand.CMPP_CONNECT: {
                MsgConnect connectReq =
                        ReadMsgService.readConnect(requestData,
                                ((InetSocketAddress) ctx.channel()
                                        .remoteAddress()).getAddress().getHostAddress());

                logger.info("<链接短信网关, version:{}, seq:{}>",
                        connectReq.getVersion(),
                        connectReq.getSequenceId());

                resp = PutMsgService.setConnectResp(connectReq);
                break;
            }

            case MsgCommand.CMPP_SUBMIT: {
                MsgSubmit submitReq = ReadMsgService.readSubmit(requestData);

                logger.info("<下发短信, 手机号:{}, seq:{}, content:{}>",
                        submitReq.getDestTerminalId(),
                        submitReq.getSequenceId(),
                        submitReq.getStrMsgContent());

                long msgId = MsgIdGenerator.nextId();

                resp = PutMsgService.setSubmitResp(submitReq, msgId);

                // 🔥 Netty 下不能再用 Socket
                NettyReportSender.cachePendingReport(
                        ctx.channel(),
                        submitReq,
                        msgId
                );
                break;
            }

            default:
                logger.warn("未知 CMPP 命令: 0x{}",
                        Integer.toHexString(head.getCommandId()));
                return;
        }

        // 3️⃣ 写回响应
        if (resp != null) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(resp));
        }

        //添加deliver相关代码处理
        int cmd = parseCommand(resp);
        if (cmd == MsgCommand.CMPP_SUBMIT_RESP) {
            // 从byte数组里面获取sequeueId
            int seqId = readSequenceId(resp);
            logger.info("seqId:{}", seqId);
            // 发送resp后再发送状态报告
            NettyReportSender.submitPendingReports(seqId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("CMPP 通信异常", cause);
        ctx.close();
    }

    private int parseCommand(byte[] message) {
        if (message == null || message.length < 8) {
            return -1;
        }


        // 可选：检查是否在 MsgCommand 常量里
        int commandId =
                ((message[4] & 0xFF) << 24)
                        | ((message[5] & 0xFF) << 16)
                        | ((message[6] & 0xFF) << 8)
                        | (message[7] & 0xFF);

        switch (commandId) {

            case 0x00000001:
                return MsgCommand.CMPP_CONNECT;

            case 0x80000001:
                return MsgCommand.CMPP_CONNECT_RESP;

            case 0x00000002:
                return MsgCommand.CMPP_TERMINATE;

            case 0x80000002:
                return MsgCommand.CMPP_TERMINATE_RESP;

            case 0x00000004:
                return MsgCommand.CMPP_SUBMIT;

            case 0x80000004:
                return MsgCommand.CMPP_SUBMIT_RESP;

            default:
                logger.warn("Unknown CMPP Command_Id: 0x{}",
                        Integer.toHexString(commandId));
                return -1;
        }
    }

    private int readSequenceId(byte[] message) {
        if (message == null || message.length < 12) {
            return -1;
        }

        // CMPP 采用大端（网络字节序）
        int seqId = ((message[8] & 0xFF) << 24)
                | ((message[9] & 0xFF) << 16)
                | ((message[10] & 0xFF) << 8)
                | (message[11] & 0xFF);

        return seqId;
    }

}


