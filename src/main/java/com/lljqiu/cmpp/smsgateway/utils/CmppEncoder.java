package com.lljqiu.cmpp.smsgateway.utils;

import com.lljqiu.cmpp.smsgateway.stack.MsgDeliver;
import com.lljqiu.cmpp.smsgateway.stack.MsgReport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * @program: smsgateway-master
 * @description:
 * @author: zhb
 * @create: 2026-01-03 15:08
 */
public class CmppEncoder {

    public static byte[] encode(MsgDeliver deliver) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            // ===== 先写消息体（不含 total_length）=====
            writeDeliverBody(dos, deliver);

            byte[] body = bos.toByteArray();

            // ===== 再拼消息头 =====
            ByteArrayOutputStream packet = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(packet);

            // Total_Length = 4 + body.length
            out.writeInt(body.length + 4);
            out.write(body);

            out.flush();
            return packet.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("CMPP encode error", e);
        }
    }

    private static void writeDeliverBody(DataOutputStream dos,
                                         MsgDeliver d) throws IOException {

        // ===== CMPP 消息头（不含 total_length）=====
        dos.writeInt(d.getCommandId());
        dos.writeInt(d.getSequenceId());

        // ===== CMPP_DELIVER 固定字段 =====
        dos.writeLong(d.getMsgId());                    // 8
        writeString(dos, d.getDestId(), 21);
        writeString(dos, d.getServiceId(), 10);

        dos.writeByte(d.getTpPid());
        dos.writeByte(d.getTpUdhi());
        dos.writeByte(d.getMsgFmt());

        writeString(dos, d.getSrcTerminalId(), 32);

        dos.writeByte(d.getSrcTerminalType());
        dos.writeByte(d.getRegisteredDelivery());

        if (d.getRegisteredDelivery() == 1) {
            // ===== 状态报告 =====
            dos.writeByte(60);                          // Msg_Length
            writeReport(dos, d.getReport());
        } else {
            // ===== 上行短信（你现在可以不管）=====
            byte[] content = d.getMoContent();
            dos.writeByte(content.length);
            dos.write(content);
        }
    }

    private static void writeReport(DataOutputStream dos,
                                    MsgReport r) throws IOException {

        dos.writeLong(r.getMsgId());                    // 8
        writeString(dos, r.getStat(), 7);               // 7
        writeString(dos, r.getSubmitTime(), 10);        // 10
        writeString(dos, r.getDoneTime(), 10);          // 10
        writeString(dos, r.getDestTerminalId(), 32);    // 32
        dos.writeInt(r.getSmscSequence());              // 4
    }

    private static void writeString(DataOutputStream dos,
                                    String value,
                                    int fixedLen) throws IOException {

        byte[] data = value == null
                ? new byte[0]
                : value.getBytes(StandardCharsets.US_ASCII);

        if (data.length >= fixedLen) {
            dos.write(data, 0, fixedLen);
        } else {
            dos.write(data);
            // 右补 0x00
            for (int i = data.length; i < fixedLen; i++) {
                dos.writeByte(0);
            }
        }
    }
}




