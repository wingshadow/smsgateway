package com.lljqiu.cmpp.smsgateway.stack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CMPP3 Deliver消息（网关端实现）
 * ISMG -> SP 用于下发MO上行消息和状态报告
 */
public class MsgDeliver {
    // 消息头
    private int totalLength;
    private int commandId = 0x00000005; // CMPP_DELIVER
    private int sequenceId;

    // 消息体
    private long msgId;
    private String destId = "";      // 目的号码（SP接入码）
    private String serviceId = "";   // 业务代码
    private int tpPid = 0;           // GSM协议类型
    private int tpUdhi = 0;          // 长短信标识
    private int msgFmt = 15;         // 默认GBK编码
    private String srcTerminalId = ""; // 手机号码
    private byte srcTerminalType = 0; // 手机号码类型
    private byte registeredDelivery = 0; // 0:MO消息 1:状态报告
    private int msgLength = 0;
    private byte[] msgContent;
    private String linkId = "";      // 链路标识

    // 状态报告专用字段
    private String reportMsgId = "";      // 状态报告对应的MsgId
    private String reportStat = "";       // 状态
    private String reportSubmitTime = ""; // 状态报告提交时间
    private String reportDoneTime = "";   // 状态报告完成时间
    private String reportDestTerminalId = ""; // 目的号码
    private int reportSmscSequence = 0;   // SMSC序列号

    // 计数器
    private static final AtomicInteger SEQUENCE_COUNTER = new AtomicInteger(1);

    // 字符集
    private static final Charset ASCII = StandardCharsets.ISO_8859_1;
    private static final Charset GBK = Charset.forName("GBK");
    private static final Charset UCS2 = StandardCharsets.UTF_16BE;

    // 时间格式化
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 创建状态报告
     * @param mobile 手机号码
     * @param spCode SP接入码
     * @param submitMsgId Submit时返回的MsgId
     * @param stat 状态 DELIVRD:成功 EXPIRED:过期等
     * @param submitTime 提交时间(YYYYMMDDHHMMSS)
     * @param doneTime 完成时间(YYYYMMDDHHMMSS)
     * @return MsgDeliver对象
     */
    public static MsgDeliver createReport(String mobile, String spCode,
                                          long submitMsgId, String stat,
                                          String submitTime, String doneTime) {
        MsgDeliver deliver = new MsgDeliver();
        deliver.sequenceId = SEQUENCE_COUNTER.getAndIncrement();
        deliver.msgId = generateMsgId();
        deliver.srcTerminalId = mobile;
        deliver.destId = spCode;
        deliver.registeredDelivery = 1;
        deliver.msgFmt = 0;
        deliver.tpPid = 0;
        deliver.tpUdhi = 0;
        deliver.serviceId = "";
        deliver.srcTerminalType = 0;
        deliver.linkId = "";

        // 设置状态报告内容
        deliver.reportMsgId = String.format("%010d", submitMsgId);
        deliver.reportStat = fixLength(stat, 7); // 状态固定7字节
        deliver.reportSubmitTime = formatTime(submitTime); // 去掉年份，MMddHHmmss格式
        deliver.reportDoneTime = formatTime(doneTime);     // 去掉年份，MMddHHmmss格式
        deliver.reportDestTerminalId = fixLength(mobile, 21); // 目的号码21字节
        deliver.reportSmscSequence = (int)(submitMsgId % 1000000);

        // 构建状态报告消息内容
        deliver.buildReportContent();
        return deliver;
    }

    /**
     * 转换为字节数组（发送给SP）
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // 先构建消息体
        ByteArrayOutputStream bodyBaos = new ByteArrayOutputStream();
        DataOutputStream bodyDos = new DataOutputStream(bodyBaos);

        // 写入消息体
        bodyDos.writeLong(msgId);
        writeString(bodyDos, destId, 21);
        writeString(bodyDos, serviceId, 10);
        bodyDos.writeByte(tpPid);
        bodyDos.writeByte(tpUdhi);
        bodyDos.writeByte(msgFmt);
        writeString(bodyDos, srcTerminalId, 32);
        bodyDos.writeByte(srcTerminalType);
        bodyDos.writeByte(registeredDelivery);
        bodyDos.writeByte(msgLength);

        if (msgContent != null && msgLength > 0) {
            bodyDos.write(msgContent);
        }

        writeString(bodyDos, linkId, 8);

        byte[] body = bodyBaos.toByteArray();
        bodyBaos.close();

        // 写入消息头
        totalLength = 12 + body.length;

        dos.writeInt(totalLength);
        dos.writeInt(commandId);
        dos.writeInt(sequenceId);
        dos.write(body);

        byte[] result = baos.toByteArray();
        baos.close();
        return result;
    }

    /**
     * 构建状态报告内容
     * 状态报告格式：65字节
     * Msg_Id(10) + Stat(7) + Submit_time(10) + Done_time(10) + Dest_terminal_Id(21) + SMSC_sequence(6) + Reserve(1)
     */
    private void buildReportContent() {
        try {
            // 使用ByteBuffer构建固定长度的字节数组
            ByteBuffer buffer = ByteBuffer.allocate(65); // 10+7+10+10+21+6+1 = 65字节

            // 1. Msg_Id: 10字节数字字符串，右对齐，左补空格
            String msgIdStr = String.format("%10s", reportMsgId);
            buffer.put(msgIdStr.getBytes(ASCII));

            // 2. Stat: 7字节，左对齐，右补空格
            String statStr = String.format("%-7s", reportStat);
            buffer.put(statStr.getBytes(ASCII));

            // 3. Submit_time: 10字节，MMddHHmmss格式
            buffer.put(reportSubmitTime.getBytes(ASCII));

            // 4. Done_time: 10字节，MMddHHmmss格式
            buffer.put(reportDoneTime.getBytes(ASCII));

            // 5. Dest_terminal_Id: 21字节，左对齐，右补空格
            String destStr = String.format("%-21s", reportDestTerminalId);
            buffer.put(destStr.getBytes(ASCII));

            // 6. SMSC_sequence: 6字节数字字符串，右对齐，左补0
            String seqStr = String.format("%06d", reportSmscSequence);
            buffer.put(seqStr.getBytes(ASCII));

            // 7. Reserve: 1字节，固定为0
            buffer.put((byte) '0');

            this.msgContent = buffer.array();
            this.msgLength = msgContent.length;

        } catch (Exception e) {
            // 如果构建失败，使用旧的方法
            buildReportContentOld();
        }
    }

    /**
     * 备用的状态报告构建方法（字符串拼接方式）
     */
    private void buildReportContentOld() {
        StringBuilder sb = new StringBuilder();

        // 1. Msg_Id: 10字节
        sb.append(String.format("%10s", reportMsgId));

        // 2. Stat: 7字节
        sb.append(String.format("%-7s", reportStat));

        // 3. Submit_time: 10字节
        sb.append(reportSubmitTime);

        // 4. Done_time: 10字节
        sb.append(reportDoneTime);

        // 5. Dest_terminal_Id: 21字节
        sb.append(String.format("%-21s", reportDestTerminalId));

        // 6. SMSC_sequence: 6字节
        sb.append(String.format("%06d", reportSmscSequence));

        // 7. Reserve: 1字节
        sb.append("0");

        this.msgContent = sb.toString().getBytes(ASCII);
        this.msgLength = msgContent.length;
    }

    /**
     * 生成byte数组的便捷方法
     */
    public byte[] generateBytes() {
        try {
            return toBytes();
        } catch (IOException e) {
            // 在实际应用中应该记录日志
            e.printStackTrace();
            return new byte[0];
        }
    }

    // 辅助方法
    private static String formatTime(String timeStr) {
        if (timeStr == null || timeStr.length() < 14) {
            // 如果时间格式不对，使用当前时间
            String current = TIME_FORMAT.format(new Date());
            return current.substring(4);
        }
        // 去掉年份（前4位），保留月日时分秒
        return timeStr.substring(4);
    }

    private static String fixLength(String str, int length) {
        if (str == null) {
            return "";
        }
        if (str.length() > length) {
            return str.substring(0, length);
        } else {
            return str;
        }
    }

    private static void writeString(DataOutputStream dos, String str, int length)
            throws IOException {
        byte[] bytes = new byte[length];
        if (str != null) {
            byte[] src = str.getBytes(ASCII);
            int copyLen = Math.min(src.length, length);
            System.arraycopy(src, 0, bytes, 0, copyLen);
        }
        dos.write(bytes);
    }

    private static long generateMsgId() {
        // 网关端MsgId生成规则：8字节
        // 格式：时间戳(4字节) + 网关编号(2字节) + 序列号(2字节)
        long timestamp = System.currentTimeMillis() / 1000;
        long gatewayCode = 1; // 假设网关编号为1
        long sequence = SEQUENCE_COUNTER.get() % 65536;

        return ((timestamp & 0xFFFFFFFFL) << 32) |
                ((gatewayCode & 0xFFFFL) << 16) |
                (sequence & 0xFFFFL);
    }

    // Getter和Setter
    public long getMsgId() { return msgId; }
    public void setMsgId(long msgId) { this.msgId = msgId; }

    public String getDestId() { return destId; }
    public void setDestId(String destId) { this.destId = destId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public int getTpPid() { return tpPid; }
    public void setTpPid(int tpPid) { this.tpPid = tpPid; }

    public int getTpUdhi() { return tpUdhi; }
    public void setTpUdhi(int tpUdhi) { this.tpUdhi = tpUdhi; }

    public int getMsgFmt() { return msgFmt; }
    public void setMsgFmt(int msgFmt) { this.msgFmt = msgFmt; }

    public String getSrcTerminalId() { return srcTerminalId; }
    public void setSrcTerminalId(String srcTerminalId) {
        this.srcTerminalId = srcTerminalId;
    }

    public byte getSrcTerminalType() { return srcTerminalType; }
    public void setSrcTerminalType(byte srcTerminalType) {
        this.srcTerminalType = srcTerminalType;
    }

    public byte getRegisteredDelivery() { return registeredDelivery; }
    public void setRegisteredDelivery(byte registeredDelivery) {
        this.registeredDelivery = registeredDelivery;
    }

    public int getMsgLength() { return msgLength; }

    public byte[] getMsgContent() { return msgContent; }

    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }

    public int getTotalLength() { return totalLength; }
    public int getCommandId() { return commandId; }
    public int getSequenceId() { return sequenceId; }
    public void setSequenceId(int sequenceId) { this.sequenceId = sequenceId; }

    // 状态报告相关
    public boolean isReport() { return registeredDelivery == 1; }
    public String getReportMsgId() { return reportMsgId; }
    public String getReportStat() { return reportStat; }
    public String getReportSubmitTime() { return reportSubmitTime; }
    public String getReportDoneTime() { return reportDoneTime; }
    public String getReportDestTerminalId() { return reportDestTerminalId; }
    public int getReportSmscSequence() { return reportSmscSequence; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MsgDeliver{");
        sb.append("seq=").append(sequenceId);
        sb.append(", msgId=").append(msgId);
        if (isReport()) {
            sb.append(", type=REPORT");
            sb.append(", stat=").append(reportStat);
            sb.append(", origMsgId=").append(reportMsgId);
            sb.append(", submitTime=").append(reportSubmitTime);
            sb.append(", doneTime=").append(reportDoneTime);
        } else {
            sb.append(", type=MO");
            sb.append(", from=").append(srcTerminalId);
            sb.append(", to=").append(destId);
        }
        sb.append(", msgLength=").append(msgLength);
        sb.append('}');
        return sb.toString();
    }
}

/**
 * 使用示例和测试类
 */
class DeliverTest {

    /**
     * 生成状态报告byte[]的示例
     */
    public static void main(String[] args) {
        // 示例数据
        String mobile = "13800138000";           // 手机号码
        String spCode = "10690123456";          // SP接入码
        long submitMsgId = 1234567890L;         // Submit时返回的MsgId
        String stat = "DELIVRD";               // 状态：已送达
        String submitTime = "20230925103020";   // 提交时间
        String doneTime = "20230925103025";     // 完成时间

        // 创建状态报告
        MsgDeliver report = MsgDeliver.createReport(
                mobile, spCode, submitMsgId, stat, submitTime, doneTime);

        // 生成byte数组
        byte[] reportBytes = report.generateBytes();

        System.out.println("状态报告信息: " + report);
        System.out.println("生成的byte数组长度: " + reportBytes.length);

        // 打印前100个字节（十六进制）
        System.out.println("前100字节（十六进制）:");
        for (int i = 0; i < Math.min(100, reportBytes.length); i++) {
            System.out.printf("%02X ", reportBytes[i]);
            if ((i + 1) % 16 == 0) {
                System.out.println();
            }
        }
        System.out.println();

        // 验证状态报告内容
        if (report.isReport()) {
            System.out.println("状态报告内容:");
            System.out.println("Msg_Id: [" + report.getReportMsgId() + "]");
            System.out.println("Stat: [" + report.getReportStat() + "]");
            System.out.println("Submit_time: [" + report.getReportSubmitTime() + "]");
            System.out.println("Done_time: [" + report.getReportDoneTime() + "]");
            System.out.println("Dest_terminal_Id: [" + report.getReportDestTerminalId() + "]");
            System.out.println("SMSC_sequence: " + report.getReportSmscSequence());

            // 打印msgContent的ASCII内容
            System.out.println("MsgContent ASCII: " +
                    new String(report.getMsgContent(), StandardCharsets.ISO_8859_1));
        }
    }

    /**
     * 批量生成状态报告
     */
    public static List<byte[]> batchGenerateReports(List<ReportData> reportList) {
        List<byte[]> result = new ArrayList<>();

        for (ReportData data : reportList) {
            MsgDeliver report = MsgDeliver.createReport(
                    data.getMobile(),
                    data.getSpCode(),
                    data.getSubmitMsgId(),
                    data.getStat(),
                    data.getSubmitTime(),
                    data.getDoneTime()
            );

            result.add(report.generateBytes());
        }

        return result;
    }
}

/**
 * 状态报告数据结构
 */
class ReportData {
    private String mobile;
    private String spCode;
    private long submitMsgId;
    private String stat;
    private String submitTime;
    private String doneTime;

    // 构造方法、getters和setters...

    public ReportData(String mobile, String spCode, long submitMsgId,
                      String stat, String submitTime, String doneTime) {
        this.mobile = mobile;
        this.spCode = spCode;
        this.submitMsgId = submitMsgId;
        this.stat = stat;
        this.submitTime = submitTime;
        this.doneTime = doneTime;
    }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getSpCode() { return spCode; }
    public void setSpCode(String spCode) { this.spCode = spCode; }

    public long getSubmitMsgId() { return submitMsgId; }
    public void setSubmitMsgId(long submitMsgId) { this.submitMsgId = submitMsgId; }

    public String getStat() { return stat; }
    public void setStat(String stat) { this.stat = stat; }

    public String getSubmitTime() { return submitTime; }
    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }

    public String getDoneTime() { return doneTime; }
    public void setDoneTime(String doneTime) { this.doneTime = doneTime; }
}

/**
 * 状态报告状态常量
 */
class DeliverStat {
    public static final String DELIVRD = "DELIVRD";    // 已送达
    public static final String EXPIRED = "EXPIRED";    // 已过期
    public static final String UNDELIV = "UNDELIV";    // 无法送达
    public static final String ACCEPTD = "ACCEPTD";    // 已接受
    public static final String UNKNOWN = "UNKNOWN";    // 未知
    public static final String REJECTD = "REJECTD";    // 被拒绝
    public static final String DTBLACK = "DTBLACK";    // 目的号码是黑名单号码

    /**
     * 根据运营商状态码转换为标准状态
     */
    public static String convertFromOperator(String operatorCode) {
        if (operatorCode == null) {
            return UNKNOWN;
        }

        switch (operatorCode.toUpperCase()) {
            case "DELIVRD":
            case "0":
                return DELIVRD;
            case "EXPIRED":
            case "1":
                return EXPIRED;
            case "UNDELIV":
            case "2":
                return UNDELIV;
            case "ACCEPTD":
            case "3":
                return ACCEPTD;
            case "REJECTD":
            case "4":
                return REJECTD;
            case "DTBLACK":
            case "5":
                return DTBLACK;
            default:
                return UNKNOWN;
        }
    }
}