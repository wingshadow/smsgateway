package com.lljqiu.cmpp.smsgateway.service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.lljqiu.cmpp.smsgateway.stack.*;
import com.lljqiu.cmpp.smsgateway.utils.MsgIdGenerator;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lljqiu.cmpp.smsgateway.utils.GateWayUtils;

public class ReadMsgService {

    private static Logger logger = LoggerFactory.getLogger(ReadMsgService.class);

    /** 读取消息头 */
    public static MsgHead readHead(byte[] data) {
        MsgHead head = new MsgHead();
        try (ByteArrayInputStream bins = new ByteArrayInputStream(data);
             DataInputStream dins = new DataInputStream(bins)) {

            head.setTotalLength(data.length + 4);
            head.setCommandId(dins.readInt());
            head.setSequenceId(dins.readInt());

        } catch (IOException e) {
            logger.error("read message head error {}", e.getMessage(), e);
        }
        return head;
    }

    /**
     * 读取客户端请求消息，并返回响应
     */
    public static byte[] readRequestMessage(Socket socket, DataInputStream input, String spIp) {
        try {
            // 1️⃣ 读取总长度
            int len;
            try {
                len = input.readInt();
            } catch (EOFException eof) {
                logger.info("客户端 {} 已断开连接", spIp);
                return null;
            }

            if (len < 12 || len > 1024 * 1024) {
                logger.warn("非法CMPP包长度: {}", len);
                return null;
            }

            // 2️⃣ 读取剩余数据
            byte[] requestData = new byte[len - 4];
            int readLen = input.read(requestData);
            if (readLen != requestData.length) {
                logger.warn("读取CMPP数据长度不一致,期望 {}, 实际 {}", requestData.length, readLen);
                return null;
            }

            logger.debug("客户端发送内容长度: {}", requestData.length);

            // 3️⃣ 解析消息头
            MsgHead head = readHead(requestData);
            logger.debug("请求头消息: {}", ToStringBuilder.reflectionToString(head));

            byte[] result = null;
            switch (head.getCommandId()) {
                case MsgCommand.CMPP_CONNECT:
                    MsgConnect connectReq = readConnect(requestData, spIp);
                    logger.info("<链接短信网关, version:{}, seq:{}>",
                            connectReq.getVersion(), connectReq.getSequenceId());
                    result = PutMsgService.setConnectResp(connectReq);
                    break;

                case MsgCommand.CMPP_SUBMIT:
                    MsgSubmit submitReq = readSubmit(requestData);
                    logger.info("<下发短信, 手机号:{}, seq:{},content:{}>",
                            submitReq.getDestTerminalId(), submitReq.getSequenceId(),submitReq.getStrMsgContent());

                    long msgId = MsgIdGenerator.nextId();
                    result = PutMsgService.setSubmitResp(submitReq,msgId);
                    break;

                default:
                    logger.warn("未知CMPP命令: {}", head.getCommandId());
            }

            return result;

        } catch (IOException e) {
            logger.error("读取CMPP请求异常", e);
            return null;
        }
    }

    /** 读取Submit消息（不改动原逻辑，只保证安全读取） */
    public static MsgSubmit readSubmit(byte[] requestData) {
        MsgSubmit submitReq = new MsgSubmit();

        try (ByteArrayInputStream bins = new ByteArrayInputStream(requestData);
             DataInputStream dins = new DataInputStream(bins)) {

            // 解析基本字段
            submitReq.setTotalLength(requestData.length + 4);
            submitReq.setCommandId(dins.readInt());
            submitReq.setSequenceId(dins.readInt());

            // 解析 MsgId
            submitReq.setMsgId(readMsgId(dins));

            // 解析包信息
            submitReq.setPkTotal(readByteField(dins));
            submitReq.setPkNumber(readByteField(dins));
            submitReq.setRegisteredDelivery(readByteField(dins));
            submitReq.setMsgLevel(readByteField(dins));

            // 解析服务信息
            submitReq.setServiceId(readStringField(dins, 10));
            submitReq.setFeeUserType(readByteField(dins));
            submitReq.setFeeTerminalId(readStringField(dins, 32));
            submitReq.setFeeTerminalType(readByteField(dins));

            // 解析TP信息
            submitReq.setTpPId(readByteField(dins));
            submitReq.setTpUdhi(readByteField(dins));
            submitReq.setMsgFmt(readByteField(dins));

            // 解析源信息
            submitReq.setMsgSrc(readStringField(dins, 6));
            submitReq.setFeeType(readStringField(dins, 2));
            submitReq.setFeeCode(readStringField(dins, 6));
            submitReq.setValIdTime(readStringField(dins, 17));
            submitReq.setAtTime(readStringField(dins, 17));
            submitReq.setSrcId(readFixString(dins, 21));

            // 解析接收用户信息
            int destUsrTl = readDestUsrTl(dins);
            if (destUsrTl < 0) {
                logger.error("Invalid destUsrTl value: {}", destUsrTl);
                return null;  // 返回 null 或者抛出异常
            }
            submitReq.setDestUsrTl(destUsrTl);

            // 计算目的终端ID长度并校验
            int destTerminalIdLength = 32 * destUsrTl;
            if (destTerminalIdLength < 0) {
                logger.error("Calculated destTerminalIdLength is negative: {}", destTerminalIdLength);
                return null;  // 返回 null 或者抛出异常
            }

            // 解析目的终端ID
            submitReq.addDestTerminalId(readStringField(dins, destTerminalIdLength));

            // 解析终端类型
            submitReq.setDestTerminalType(readByteField(dins));

            // 解析消息内容长度
            int msgLength = readByteField(dins);
            submitReq.setMsgLength(msgLength);

            // 解析消息内容
            submitReq.setMsgContent(readBytesField(dins, msgLength));

            // 解析LinkID
            submitReq.setLinkID(readStringField(dins, 20));

        } catch (EOFException eof) {
            logger.info("客户端断开或数据未完整: {}", eof.getMessage());
        } catch (IOException e) {
            logger.error("读取提交消息时出错: {}", e.getMessage(), e);
        }

        return submitReq;
    }

    // 解析消息ID
    private static long readMsgId(DataInputStream dins) throws IOException {
        byte[] msgId = new byte[8];
        dins.readFully(msgId);
        return GateWayUtils.Bytes8ToLong(msgId);
    }

    // 解析一个字节字段
    private static int readByteField(DataInputStream dins) throws IOException {
        byte[] field = new byte[1];
        dins.readFully(field);
        return Byte.toUnsignedInt(field[0]);
    }

    // 解析一个定长字符串字段
    private static String readStringField(DataInputStream dins, int length) throws IOException {
        byte[] field = new byte[length];
        dins.readFully(field);
        return new String(field, 0, length, StandardCharsets.UTF_8);
    }

    // 解析DestUsr_tl字段
    private static int readDestUsrTl(DataInputStream dins) throws IOException {
        byte[] destUsrTl = new byte[1];
        dins.readFully(destUsrTl);
        return GateWayUtils.byteToInt(destUsrTl[0]);
    }

    // 解析一个字节数组字段
    private static byte[] readBytesField(DataInputStream dins, int length) throws IOException {
        byte[] field = new byte[length];
        dins.readFully(field);
        return field;
    }

    private static String readFixString(DataInputStream dins, int length) throws IOException {
        byte[] data = new byte[length];
        dins.readFully(data);
        String s = new String(data, StandardCharsets.UTF_8);
        return s.replace("\0", "").trim();
    }


    /** 读取CMPP连接消息 */
    public static MsgConnect readConnect(byte[] requestData, String spIp) {
        MsgConnect msgConnect = new MsgConnect();
        msgConnect.setSpIp(spIp);

        if (requestData.length != 8 + 6 + 16 + 1 + 4) {
            logger.warn("Analysis packet error, packet length inconsistent. Length: {}", requestData.length);
            return msgConnect;
        }

        try (ByteArrayInputStream bins = new ByteArrayInputStream(requestData);
             DataInputStream dins = new DataInputStream(bins)) {

            msgConnect.setTotalLength(requestData.length + 4);
            msgConnect.setCommandId(dins.readInt());
            msgConnect.setSequenceId(dins.readInt());

            byte[] sourceAddr = new byte[6];
            dins.readFully(sourceAddr);
            msgConnect.setSourceAddr(new String(sourceAddr));

            byte[] aiByte = new byte[16];
            dins.readFully(aiByte);
            msgConnect.setAuthenticatorSource(aiByte);

            msgConnect.setVersion(dins.readByte());
            msgConnect.setTimestamp(dins.readInt());

        } catch (IOException e) {
            logger.error("read msg connect error {}", e.getMessage(), e);
        }

        return msgConnect;
    }

    public static MsgDeliverResp readDeliverResp(byte[] data) {
        MsgDeliverResp resp = new MsgDeliverResp();

        int offset = 0;

        // 1. Command_Id
        resp.setCommandId(GateWayUtils.bytes4ToInt(data, offset));
        offset += 4;

        // 2. Sequence_Id
        resp.setSequenceId(GateWayUtils.bytes4ToInt(data, offset));
        offset += 4;

        // 3. Msg_Id
        resp.setMsgId(GateWayUtils.bytes8ToLong(data, offset));
        offset += 8;

        // 4. Result（标准 4 字节 / 非标 1 字节 兼容）
        int remain = data.length - offset;
        if (remain >= 4) {
            resp.setResult(GateWayUtils.bytes4ToInt(data, offset));
            offset += 4;
        } else if (remain == 1) {
            resp.setResult(data[offset] & 0xFF);
            offset += 1;
        } else {
            throw new IllegalArgumentException(
                    "非法 CMPP_DELIVER_RESP, remain=" + remain);
        }

        resp.setTotalLength(data.length);
        return resp;
    }

}
