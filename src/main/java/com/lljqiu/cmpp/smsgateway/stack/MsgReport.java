package com.lljqiu.cmpp.smsgateway.stack;

/**
 * @program: smsgateway-master
 * @description:
 * @author: zhb
 * @create: 2026-01-03 14:41
 */
public class MsgReport {

    /** 8字节，与CMPP_SUBMIT的Msg_Id一致 */
    private long msgId;

    /** 7字节：DELIVRD / UNDELIV */
    private String stat;

    /** 10字节：yyMMddHHmm */
    private String submitTime;

    /** 10字节：yyMMddHHmm */
    private String doneTime;

    /** 32字节：目标终端MSISDN（单个手机号） */
    private String destTerminalId;

    /** 4字节：取自CMPP_SUBMIT.sequenceId */
    private int smscSequence;

    // getter / setter

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public String getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(String doneTime) {
        this.doneTime = doneTime;
    }

    public String getDestTerminalId() {
        return destTerminalId;
    }

    public void setDestTerminalId(String destTerminalId) {
        this.destTerminalId = destTerminalId;
    }

    public int getSmscSequence() {
        return smscSequence;
    }

    public void setSmscSequence(int smscSequence) {
        this.smscSequence = smscSequence;
    }
}


