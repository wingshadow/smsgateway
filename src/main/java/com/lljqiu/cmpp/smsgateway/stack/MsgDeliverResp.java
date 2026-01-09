package com.lljqiu.cmpp.smsgateway.stack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-09 17:16
 */
public class MsgDeliverResp extends MsgHead{
    private long          msgId;
    private int           result;

    public byte[] toByteArry() {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotalLength());
            dous.writeInt(this.getCommandId());
            dous.writeInt(this.getSequenceId());
            dous.writeLong(msgId);
            dous.writeInt(result);
            dous.close();
        } catch (IOException e) {
        }
        return bous.toByteArray();
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
