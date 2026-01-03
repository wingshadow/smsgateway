package com.lljqiu.cmpp.smsgateway.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.lljqiu.cmpp.smsgateway.stack.MsgCommand;
import com.lljqiu.cmpp.smsgateway.stack.MsgDeliver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lljqiu.cmpp.smsgateway.service.ReadMsgService;
import com.lljqiu.cmpp.smsgateway.utils.GatewayConfig;

public class SMSServer {
    private static Logger logger = LoggerFactory.getLogger(SMSServer.class);

    public void socketServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("start server success on port {}", port);

            while (true) {
                Socket socket = serverSocket.accept();
                String spIp = socket.getInetAddress().getHostAddress();
                logger.info("客户端 {} 已连接", spIp);

                // 每个客户端单独线程处理
                new Thread(() -> handleClient(socket, spIp)).start();
            }
        } catch (IOException e) {
            logger.error("start server error", e);
        }
    }

    private void handleClient(Socket socket, String spIp) {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            while (true) {
                byte[] respMessage = ReadMsgService.readRequestMessage(socket,input, spIp);

                if (respMessage == null) {
                    logger.info("客户端 {} 已断开或数据异常，关闭连接", spIp);
                    break;
                }

                output.write(respMessage);
                output.flush();

                //
                int cmd = parseCommand(respMessage);

                if (cmd == MsgCommand.CMPP_SUBMIT_RESP) {
                    // 从byte数组里面获取sequeueId
                    long seqId = readSequenceId(respMessage);
                    // 发送resp后再发送状态报告
                    ReportSender.submitPendingReports(seqId);
                }
            }

        } catch (IOException e) {
            logger.error("客户端 {} 通信异常", spIp, e);
        } finally {
            try {
                socket.close();
                logger.info("客户端 {} 已关闭连接", spIp);
            } catch (IOException e) {
                logger.error("关闭客户端 {} socket 异常", spIp, e);
            }
        }
    }

    /**
     * 解析 CMPP 消息类型
     * @param message 完整消息字节数组
     * @return 对应的 MsgCommand 常量值，如果无法识别返回 -1
     */
    private int parseCommand(byte[] message) {
        if (message == null || message.length < 8) {
            return -1; // 消息太短
        }

        // CMPP 协议：前 4 字节是总长度，紧接 4 字节是 Command_Id
        int commandId = ((message[4] & 0xFF) << 24)
                | ((message[5] & 0xFF) << 16)
                | ((message[6] & 0xFF) << 8)
                | (message[7] & 0xFF);

        // 可选：检查是否在 MsgCommand 常量里
        switch (commandId) {
            case MsgCommand.CMPP_CONNECT: return MsgCommand.CMPP_CONNECT;
            case MsgCommand.CMPP_CONNECT_RESP: return MsgCommand.CMPP_CONNECT_RESP;
            case MsgCommand.CMPP_TERMINATE: return MsgCommand.CMPP_TERMINATE;
            case MsgCommand.CMPP_TERMINATE_RESP: return MsgCommand.CMPP_TERMINATE_RESP;
            case MsgCommand.CMPP_SUBMIT: return MsgCommand.CMPP_SUBMIT;
            case MsgCommand.CMPP_SUBMIT_RESP: return MsgCommand.CMPP_SUBMIT_RESP;
            case MsgCommand.CMPP_DELIVER: return MsgCommand.CMPP_DELIVER;
            case MsgCommand.CMPP_DELIVER_RESP: return MsgCommand.CMPP_DELIVER_RESP;
            // 如果需要，可以加其他命令
            default: return -1; // 未知命令
        }
    }

    public static int readSequenceId(byte[] message) {
        if (message == null || message.length < 12) {
            return -1; // 消息太短
        }

        // CMPP 采用大端（网络字节序）
        int seqId = ((message[8] & 0xFF) << 24)
                | ((message[9] & 0xFF) << 16)
                | ((message[10] & 0xFF) << 8)
                | (message[11] & 0xFF);

        return seqId;
    }


    public void start() {
        int port = GatewayConfig.getGatewayPort();
        logger.info("start server port={}", port);
        socketServer(port);
    }
}
