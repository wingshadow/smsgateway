/**
 * Project Name smsgateway
 * File Name PutMsgService.java
 * Package Name com.lljqiu.cmpp.smsgateway.service
 * Create Time 2017年5月13日
 * Create by name：liujie -- email: liujie@lljqiu.com
 * Copyright © 2015, 2017, www.lljqiu.com. All rights reserved.
 */
package com.lljqiu.cmpp.smsgateway.service;

import com.lljqiu.cmpp.smsgateway.stack.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lljqiu.cmpp.smsgateway.exception.GateWayException;
import com.lljqiu.cmpp.smsgateway.utils.GateWayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ClassName: PutMsgService.java <br>
 * Description: 拼接返回信息<br>
 * Create by: name：liujie <br>email: liujie@lljqiu.com <br>
 * Create Time: 2017年5月13日<br>
 */
public class PutMsgService {
    private static Logger    logger   = LoggerFactory.getLogger(PutMsgService.class);
    /**
     * Description：拼接连接请求
     * @param connectReq
     * @return
     * @return byte[]
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static byte[] setConnectResp(MsgConnect connectReq) {

        String remoteIp = connectReq.getSpIp();
        MsgConnectResp connectResp = new MsgConnectResp();
        connectResp.setTotalLength(12 + 4 + 16 + 1);
        connectResp.setCommandId(MsgCommand.CMPP_CONNECT_RESP);
        connectResp.setSequenceId(connectReq.getSequenceId());

        int status = 0x0000;
        try {
            CheckService.checkConnectRequest(connectReq, remoteIp);
        } catch (GateWayException e) {
            status = e.getErrorCode();
        }

        connectResp.setStatus(status);

        // CMPP 规范：AuthenticatorISMG 应重新计算，这里先保持你原逻辑
        connectResp.setAuthenticatorISMG(connectReq.getAuthenticatorSource());
        connectResp.setVersion(connectReq.getVersion());

        logger.debug("<{}响应消息{}>",
                "CONNECT_RESP",
                GateWayUtils.toHex(connectResp.toByteArry()));

        return connectResp.toByteArry();
    }


    /**
     * Description：拼接submit请求响应
     * @param submitReq
     * @return
     * @return byte []
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static byte[] setSubmitResp(MsgSubmit submitReq,long msgId) {
        MsgSubmitResp submitResp = new MsgSubmitResp();
        submitResp.setTotalLength(12 + 8 + 4);
        submitResp.setCommandId(MsgCommand.CMPP_SUBMIT_RESP);
        submitResp.setSequenceId(submitReq.getSequenceId());
        submitResp.setMsgId(msgId);
        int status = 0x0000;
        submitResp.setResult(status);
        return submitResp.toByteArry();
    }



    /**
     * 编码MsgReport为60字节
     */
    private static byte[] encodeMsgReport(MsgReport report) {
        ByteBuffer buffer = ByteBuffer.allocate(60);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // 1. Msg_Id (8字节)
        buffer.putLong(report.getMsgId());

        // 2. Stat (7字节，右补空格)
        byte[] statBytes = report.getStat().getBytes(StandardCharsets.US_ASCII);
        int statLen = Math.min(statBytes.length, 7);
        buffer.put(statBytes, 0, statLen);
        for (int i = statLen; i < 7; i++) {
            buffer.put((byte) ' ');  // 右补空格
        }

        // 3. Submit_time (10字节)
        buffer.put(fixedLengthBytes(report.getSubmitTime(), 10));

        // 4. Done_time (10字节)
        buffer.put(fixedLengthBytes(report.getDoneTime(), 10));

        // 5. Dest_terminal_Id (21字节，左对齐右补0)
        buffer.put(fixedLengthBytes(report.getDestTerminalId(), 21));

        // 6. SMSC_sequence (4字节)
        buffer.putInt(report.getSmscSequence());

        // 填充剩余字节为0（总60字节）
        while (buffer.position() < 60) {
            buffer.put((byte) 0);
        }

        return buffer.array();
    }

    /**
     * 固定长度字节数组，不足右补0
     */
    private static byte[] fixedLengthBytes(String str, int length) {
        byte[] bytes = new byte[length];
        if (str != null) {
            byte[] src = str.getBytes(StandardCharsets.US_ASCII);
            int copyLen = Math.min(src.length, length);
            System.arraycopy(src, 0, bytes, 0, copyLen);
            // 剩余部分已经是0
        }
        return bytes;
    }



    /**
     * 截断或填充字符串
     */
    private static String truncateOrPad(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }

        if (str.length() > length) {
            return str.substring(0, length);
        } else if (str.length() < length) {
            StringBuilder sb = new StringBuilder(str);
            while (sb.length() < length) {
                sb.append(padChar);
            }
            return sb.toString();
        }
        return str;
    }



    private static String now() {
        // CMPP 要求：YYMMDDHHMM
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyMMddHHmm");
        return LocalDateTime.now().format(formatter);
    }


}
