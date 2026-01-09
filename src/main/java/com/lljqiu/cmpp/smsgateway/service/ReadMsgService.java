package com.lljqiu.cmpp.smsgateway.service;

import java.io.*;
import java.net.Socket;

import com.lljqiu.cmpp.smsgateway.stack.*;
import com.lljqiu.cmpp.smsgateway.utils.MsgIdGenerator;
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

            submitReq.setTotalLength(requestData.length + 4);
            submitReq.setCommandId(dins.readInt());
            submitReq.setSequenceId(dins.readInt());

            byte[] Msg_Id = new byte[8];
            dins.readFully(Msg_Id);
            submitReq.setMsgId(GateWayUtils.Bytes8ToLong(Msg_Id));

            byte[] Pk_total = new byte[1];
            dins.readFully(Pk_total);
            submitReq.setPkTotal(GateWayUtils.byteToInt(Pk_total[0]));

            byte[] Pk_number = new byte[1];
            dins.readFully(Pk_number);
            submitReq.setPkNumber(GateWayUtils.byteToInt(Pk_number[0]));

            byte[] Registered_Delivery = new byte[1];
            dins.readFully(Registered_Delivery);
            submitReq.setRegisteredDelivery(GateWayUtils.byteToInt(Registered_Delivery[0]));

            byte[] Msg_level = new byte[1];
            dins.readFully(Msg_level);
            submitReq.setMsgLevel(GateWayUtils.byteToInt(Msg_level[0]));

            byte[] Service_Id = new byte[10];
            dins.readFully(Service_Id);
            submitReq.setServiceId(new String(Service_Id, 0, 10));

            byte[] Fee_UserType = new byte[1];
            dins.readFully(Fee_UserType);
            submitReq.setFeeUserType(GateWayUtils.byteToInt(Fee_UserType[0]));

            byte[] Fee_terminal_Id = new byte[32];
            dins.readFully(Fee_terminal_Id);
            submitReq.setFeeTerminalId(new String(Fee_terminal_Id, 0, 32));

            byte[] Fee_terminal_type = new byte[1];
            dins.readFully(Fee_terminal_type);
            submitReq.setFeeTerminalType(GateWayUtils.byteToInt(Fee_terminal_type[0]));

            byte[] TP_pId = new byte[1];
            dins.readFully(TP_pId);
            submitReq.setTpPId(GateWayUtils.byteToInt(TP_pId[0]));

            byte[] TP_udhi = new byte[1];
            dins.readFully(TP_udhi);
            submitReq.setTpUdhi(GateWayUtils.byteToInt(TP_udhi[0]));

            byte[] Msg_Fmt = new byte[1];
            dins.readFully(Msg_Fmt);
            submitReq.setMsgFmt(GateWayUtils.byteToInt(Msg_Fmt[0]));

            byte[] Msg_src = new byte[6];
            dins.readFully(Msg_src);
            submitReq.setMsgSrc(new String(Msg_src, 0, 6));

            byte[] FeeType = new byte[2];
            dins.readFully(FeeType);
            submitReq.setFeeType(new String(FeeType, 0, 2));

            byte[] FeeCode = new byte[6];
            dins.readFully(FeeCode);
            submitReq.setFeeCode(new String(FeeCode, 0, 6));

            byte[] ValId_Time = new byte[17];
            dins.readFully(ValId_Time);
            submitReq.setValIdTime(new String(ValId_Time, 0, 17));

            byte[] At_Time = new byte[17];
            dins.readFully(At_Time);
            submitReq.setAtTime(new String(At_Time, 0, 17));

            byte[] Src_Id = new byte[21];
            dins.readFully(Src_Id);
            submitReq.setSrcId(new String(Src_Id, 0, 21));

            byte[] DestUsr_tl = new byte[1];
            dins.readFully(DestUsr_tl);
            int destUsrTl = GateWayUtils.byteToInt(DestUsr_tl[0]);
            submitReq.setDestUsrTl(destUsrTl);

            int Dest_terminal_Id_length = 32 * destUsrTl;
            byte[] Dest_terminal_Id = new byte[Dest_terminal_Id_length];
            dins.readFully(Dest_terminal_Id);
            submitReq.addDestTerminalId(new String(Dest_terminal_Id, 0, Dest_terminal_Id_length));

            byte[] Dest_terminal_type = new byte[1];
            dins.readFully(Dest_terminal_type);
            submitReq.setDestTerminalType(GateWayUtils.byteToInt(Dest_terminal_type[0]));

            byte[] Msg_Length = new byte[1];
            dins.readFully(Msg_Length);
            int msgLength = GateWayUtils.byteToInt(Msg_Length[0]);
            submitReq.setMsgLength(msgLength);

            byte[] Msg_Content = new byte[msgLength];
            dins.readFully(Msg_Content);
            submitReq.setMsgContent(Msg_Content);

            byte[] LinkID = new byte[20];
            dins.readFully(LinkID);
            submitReq.setLinkID(new String(LinkID, 0, 20));

        } catch (EOFException eof) {
            logger.info("客户端断开或数据未完整: {}", eof.getMessage());
        } catch (IOException e) {
            logger.error("read Submit Message error {}", e.getMessage(), e);
        }

        return submitReq;
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
