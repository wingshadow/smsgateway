package com.lljqiu.cmpp.smsgateway.stack;

/**
 * @program: smsgateway-master
 * @description:
 * @author: zhb
 * @create: 2026-01-03 14:42
 */
public class MsgDeliver extends MsgHead {

    /** 消息总长度（编码时算） */
    private int totalLength;

    /** CMPP_DELIVER */
    private int commandId;

    /** 序列号 */
    private int sequenceId;

    /** 8字节 Msg_Id */
    private long msgId;

    /** 21字节 SP接收号（一般为 submit.Src_Id） */
    private String destId;

    /** 10字节 业务标识 */
    private String serviceId;

    private int tpPid;
    private int tpUdhi;

    /** 信息格式，一般 0 */
    private int msgFmt;

    /** 32字节：源终端号（用户手机号） */
    private String srcTerminalId;

    /** 0：真实号码 */
    private int srcTerminalType = 0;

    /** 1：状态报告 */
    private int registeredDelivery = 1;

    /** Msg_Content 长度（状态报告固定 60） */
    private int msgLength = 60;

    public byte[] getMoContent() {
        return moContent;
    }

    public void setMoContent(byte[] moContent) {
        this.moContent = moContent;
    }

    // 上行短信（registeredDelivery=0）
    private byte[] moContent;
    /** 状态报告对象 */
    private MsgReport report;

    // getter / setter

    @Override
    public int getTotalLength() {
        return totalLength;
    }

    @Override
    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    @Override
    public int getCommandId() {
        return commandId;
    }

    @Override
    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    @Override
    public int getSequenceId() {
        return sequenceId;
    }

    @Override
    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getTpPid() {
        return tpPid;
    }

    public void setTpPid(int tpPid) {
        this.tpPid = tpPid;
    }

    public int getTpUdhi() {
        return tpUdhi;
    }

    public void setTpUdhi(int tpUdhi) {
        this.tpUdhi = tpUdhi;
    }

    public int getMsgFmt() {
        return msgFmt;
    }

    public void setMsgFmt(int msgFmt) {
        this.msgFmt = msgFmt;
    }

    public String getSrcTerminalId() {
        return srcTerminalId;
    }

    public void setSrcTerminalId(String srcTerminalId) {
        this.srcTerminalId = srcTerminalId;
    }

    public int getSrcTerminalType() {
        return srcTerminalType;
    }

    public void setSrcTerminalType(int srcTerminalType) {
        this.srcTerminalType = srcTerminalType;
    }

    public int getRegisteredDelivery() {
        return registeredDelivery;
    }

    public void setRegisteredDelivery(int registeredDelivery) {
        this.registeredDelivery = registeredDelivery;
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public MsgReport getReport() {
        return report;
    }

    public void setReport(MsgReport report) {
        this.report = report;
    }
}


