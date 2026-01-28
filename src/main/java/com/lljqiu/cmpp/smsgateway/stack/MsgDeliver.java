package com.lljqiu.cmpp.smsgateway.stack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CMPP3 Deliver 消息（状态报告）
 * ISMG -> SP 用于下发状态报告
 */
public class MsgDeliver {
    // 消息头
    private int totalLength;
    private int commandId = 0x00000005; // CMPP_DELIVER
    private int sequenceId;

    // 消息体
    private long msgId;               // Msg_Id 8字节
    private String destId = "";       // SP接入码，21字节
    private String serviceId = "";    // 业务代码，10字节
    private int tpPid = 0;            // GSM协议类型
    private int tpUdhi = 0;           // 长短信标识
    private int msgFmt = 0;           // 状态报告默认为ASCII
    private String srcTerminalId = "";// 手机号，32字节
    private byte srcTerminalType = 0;
    private byte registeredDelivery = 1; // 状态报告
    private int msgLength = 0;
    private byte[] msgContent;
    private String linkId = "";       // 链路标识

    // 状态报告专用字段
    private long reportMsgId;        // Submit 消息对应 Msg_Id
    private String reportStat;       // 状态，如 DELIVRD
    private String reportSubmitTime; // YYMMDDHHMM
    private String reportDoneTime;   // YYMMDDHHMM
    private String reportDestTerminalId; // 目的号码
    private int reportSmscSequence;  // 4字节数字

    // 计数器
    private static final AtomicInteger SEQUENCE_COUNTER = new AtomicInteger(1);

    // 字符集
    private static final Charset ASCII = StandardCharsets.ISO_8859_1;

    /**
     * 创建 CMPP3 状态报告
     */
    public static MsgDeliver createReport(String mobile, String spCode,
                                          long submitMsgId, String stat) {
        MsgDeliver deliver = new MsgDeliver();
        deliver.sequenceId = SEQUENCE_COUNTER.getAndIncrement();
        deliver.msgId = generateMsgId();
        deliver.srcTerminalId = mobile;
        deliver.destId = spCode;
        deliver.registeredDelivery = 1;
        deliver.tpPid = 0;
        deliver.tpUdhi = 0;
        deliver.msgFmt = 0; // ASCII
        deliver.serviceId = "";
        deliver.srcTerminalType = 0;
        deliver.linkId = "";

        // 状态报告字段
        deliver.reportMsgId = submitMsgId;
        deliver.reportStat = fixLength(stat, 7); // 左对齐7字节
        deliver.reportSubmitTime = getCurrentYYMMDDHHMM(); // 当前时间
        deliver.reportDoneTime   = getCurrentYYMMDDHHMM(); // 当前时间
        deliver.reportDestTerminalId = fixLength(mobile, 32); // 左对齐32字节
        deliver.reportSmscSequence = (int)(submitMsgId % 10000); // 4字节

        // 构建 MsgContent
        deliver.buildReportContent();

        return deliver;
    }

    /**
     * 构建 CMPP3 状态报告 MsgContent (固定长度)
     */
    private void buildReportContent() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // 1. Msg_Id 8字节，右对齐，左补0
            dos.write(fixLengthBytes(String.format("%08d", reportMsgId), 8));

            // 2. Stat 7字节，左对齐，右补空格
            dos.write(fixLengthBytes(String.format("%-7s", reportStat), 7));

            // 3. Submit_time 10字节
            dos.write(fixLengthBytes(reportSubmitTime, 10));

            // 4. Done_time 10字节
            dos.write(fixLengthBytes(reportDoneTime, 10));

            // 5. Dest_terminal_Id 32字节，左对齐，右补空格
            dos.write(fixLengthBytes(String.format("%-32s", reportDestTerminalId), 32));

            // 6. SMSC_sequence 4字节，右对齐，左补0
            dos.write(fixLengthBytes(String.format("%04d", reportSmscSequence), 4));

            this.msgContent = baos.toByteArray();
            this.msgLength = msgContent.length;

            dos.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
            this.msgContent = new byte[0];
            this.msgLength = 0;
        }
    }

    private static byte[] fixLengthBytes(String str, int length) {
        byte[] bytes = new byte[length];
        if (str != null) {
            byte[] src = str.getBytes(ASCII);
            int copyLen = Math.min(src.length, length);
            System.arraycopy(src, 0, bytes, 0, copyLen);
        }
        return bytes;
    }

    /**
     * 生成最终发送的 byte[]
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // 构建消息体
        ByteArrayOutputStream bodyBaos = new ByteArrayOutputStream();
        DataOutputStream bodyDos = new DataOutputStream(bodyBaos);

        bodyDos.writeLong(msgId);                       // Msg_Id 8字节
        writeString(bodyDos, destId, 21);              // Dest_Id
        writeString(bodyDos, serviceId, 10);           // Service_Id
        bodyDos.writeByte(tpPid);
        bodyDos.writeByte(tpUdhi);
        bodyDos.writeByte(msgFmt);
        writeString(bodyDos, srcTerminalId, 32);       // Src_terminal_Id
        bodyDos.writeByte(srcTerminalType);
        bodyDos.writeByte(registeredDelivery);
        bodyDos.writeByte(msgLength);
        bodyDos.write(msgContent);                     // MsgContent
        writeString(bodyDos, linkId, 20);              // LinkID 20字节

        byte[] body = bodyBaos.toByteArray();
        bodyBaos.close();

        // 消息头
        totalLength = 12 + body.length;
        dos.writeInt(totalLength);
        dos.writeInt(commandId);
        dos.writeInt(sequenceId);
        dos.write(body);

        byte[] result = baos.toByteArray();
        baos.close();
        return result;
    }

    private static String getCurrentYYMMDDHHMM() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
        return LocalDateTime.now().format(formatter);
    }

    private static void writeString(DataOutputStream dos, String str, int length) throws IOException {
        byte[] bytes = new byte[length];
        if (str != null) {
            byte[] src = str.getBytes(ASCII);
            int copyLen = Math.min(src.length, length);
            System.arraycopy(src, 0, bytes, 0, copyLen);
        }
        dos.write(bytes);
    }

    private static String fixLength(String str, int length) {
        if (str == null) return String.format("%" + length + "s", "");
        if (str.length() > length) return str.substring(0, length);
        return str;
    }

    private static long generateMsgId() {
        long timestamp = System.currentTimeMillis() / 1000;
        long gatewayId = 1; // 网关ID
        long seq = SEQUENCE_COUNTER.get() % 65536;
        return ((timestamp & 0xFFFFFFFFL) << 32) | ((gatewayId & 0xFFFFL) << 16) | (seq & 0xFFFFL);
    }

    // Getter
    public byte[] getMsgContent() { return msgContent; }
    public int getMsgLength() { return msgLength; }
    public int getSequenceId() { return sequenceId; }

    // 测试
    public static void main(String[] args) throws Exception {
        MsgDeliver deliver = MsgDeliver.createReport(
                "13800138000",
                "106584040417",
                12345678L,
                "DELIVRD"
        );

        byte[] data = deliver.toBytes();

        System.out.println("MsgContent长度: " + deliver.getMsgLength());
        System.out.println("MsgContent(十六进制):");
        for (int i = 0; i < deliver.getMsgLength(); i++) {
            System.out.printf("%02X ", deliver.getMsgContent()[i]);
            if ((i + 1) % 16 == 0) System.out.println();
        }
    }
}
