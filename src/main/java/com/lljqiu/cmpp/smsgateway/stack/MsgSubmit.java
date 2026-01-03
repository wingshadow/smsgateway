/**
 * Project Name smsgateway
 * File Name package-info.java
 * Package Name com.lljqiu.cmpp.smsgateway.stack
 * Create Time 2017年5月12日
 * Create by name：liujie -- email: liujie@lljqiu.com
 */
package com.lljqiu.cmpp.smsgateway.stack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/** 
 * ClassName: MsgSubmit.java <br>
 * Description: Submit消息结构定义<br>
 * Create by: name：liujie <br>email: liujie@lljqiu.com <br>
 * Create Time: 2017年5月13日<br>
 */
public class MsgSubmit extends MsgHead {
    private final int maxMsgLength_short = 140;

    private long      msgId              = 0x00;
    private int       pkTotal            = 0x01;
    private int       pkNumber           = 0x01;
    private int       registeredDelivery = 0x00;
    private int       msgLevel           = 0x01;
    private String    serviceId          = null;
    private int       feeUserType        = 0x00;
    private String    feeTerminalId      = null;
    private int       feeTerminalType    = 0x00;
    private int       tpPId              = 0x00;
    private int       tpUdhi             = 0x00;
    private int       msgFmt             = 0x00;
    private String    msgSrc             = null;
    private String    feeType            = null;
    private String    feeCode            = null;
    private String    valIdTime          = null;
    private String    atTime             = null;
    private String    srcId              = null;
    private int       destUsrTl          = 0x00;
    @SuppressWarnings("rawtypes")
    private List      destTerminalId     = new ArrayList();
    private int       destTerminalType   = 0x00;
    private int       msgLength          = 0x00;
    private byte[]    msgContent         = null;
    private String    linkID             = null;

    /**
     * @return the msgId
     */
    public long getMsgId() {
        return msgId;
    }

    /**
     * @param msgId the msgId to set
     */
    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    /**
     * @return the pkTotal
     */
    public int getPkTotal() {
        return pkTotal;
    }

    /**
     * @param pkTotal the pkTotal to set
     */
    public void setPkTotal(int pkTotal) {
        this.pkTotal = pkTotal;
    }

    /**
     * @return the pkNumber
     */
    public int getPkNumber() {
        return pkNumber;
    }

    /**
     * @param pkNumber the pkNumber to set
     */
    public void setPkNumber(int pkNumber) {
        this.pkNumber = pkNumber;
    }

    /**
     * @return the registeredDelivery
     */
    public int getRegisteredDelivery() {
        return registeredDelivery;
    }

    /**
     * @param registeredDelivery the registeredDelivery to set
     */
    public void setRegisteredDelivery(int registeredDelivery) {
        this.registeredDelivery = registeredDelivery;
    }

    /**
     * @return the msgLevel
     */
    public int getMsgLevel() {
        return msgLevel;
    }

    /**
     * @param msgLevel the msgLevel to set
     */
    public void setMsgLevel(int msgLevel) {
        this.msgLevel = msgLevel;
    }

    /**
     * @return the serviceId
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @param serviceId the serviceId to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * @return the feeUserType
     */
    public int getFeeUserType() {
        return feeUserType;
    }

    /**
     * @param feeUserType the feeUserType to set
     */
    public void setFeeUserType(int feeUserType) {
        this.feeUserType = feeUserType;
    }

    /**
     * @return the feeTerminalId
     */
    public String getFeeTerminalId() {
        return feeTerminalId;
    }

    /**
     * @param feeTerminalId the feeTerminalId to set
     */
    public void setFeeTerminalId(String feeTerminalId) {
        this.feeTerminalId = feeTerminalId;
    }

    /**
     * @return the feeTerminalType
     */
    public int getFeeTerminalType() {
        return feeTerminalType;
    }

    /**
     * @param feeTerminalType the feeTerminalType to set
     */
    public void setFeeTerminalType(int feeTerminalType) {
        this.feeTerminalType = feeTerminalType;
    }

    /**
     * @return the tpPId
     */
    public int getTpPId() {
        return tpPId;
    }

    /**
     * @param tpPId the tpPId to set
     */
    public void setTpPId(int tpPId) {
        this.tpPId = tpPId;
    }

    /**
     * @return the tpUdhi
     */
    public int getTpUdhi() {
        return tpUdhi;
    }

    /**
     * @param tpUdhi the tpUdhi to set
     */
    public void setTpUdhi(int tpUdhi) {
        this.tpUdhi = tpUdhi;
    }

    /**
     * @return the msgFmt
     */
    public int getMsgFmt() {
        return msgFmt;
    }

    /**
     * @param msgFmt the msgFmt to set
     */
    public void setMsgFmt(int msgFmt) {
        this.msgFmt = msgFmt;
    }

    /**
     * @return the msgSrc
     */
    public String getMsgSrc() {
        return msgSrc;
    }

    /**
     * @param msgSrc the msgSrc to set
     */
    public void setMsgSrc(String msgSrc) {
        this.msgSrc = msgSrc;
    }

    /**
     * @return the feeType
     */
    public String getFeeType() {
        return feeType;
    }

    /**
     * @param feeType the feeType to set
     */
    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    /**
     * @return the feeCode
     */
    public String getFeeCode() {
        return feeCode;
    }

    /**
     * @param feeCode the feeCode to set
     */
    public void setFeeCode(String feeCode) {
        this.feeCode = feeCode;
    }

    /**
     * @return the valIdTime
     */
    public String getValIdTime() {
        return valIdTime;
    }

    /**
     * @param valIdTime the valIdTime to set
     */
    public void setValIdTime(String valIdTime) {
        this.valIdTime = valIdTime;
    }

    /**
     * @return the atTime
     */
    public String getAtTime() {
        return atTime;
    }

    /**
     * @param atTime the atTime to set
     */
    public void setAtTime(String atTime) {
        this.atTime = atTime;
    }

    /**
     * @return the srcId
     */
    public String getSrcId() {
        return srcId;
    }

    /**
     * @param srcId the srcId to set
     */
    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    /**
     * @return the destUsrTl
     */
    public int getDestUsrTl() {
        return destUsrTl;
    }

    /**
     * @param destUsrTl the destUsrTl to set
     */
    public void setDestUsrTl(int destUsrTl) {
        this.destUsrTl = destUsrTl;
    }

    /**
     * @return the destTerminalId
     */
    @SuppressWarnings("rawtypes")
    public List getDestTerminalId() {
        return destTerminalId;
    }

    /**
     * @param destTerminalId the destTerminalId to set
     */
    @SuppressWarnings("rawtypes")
    public void setDestTerminalId(List destTerminalId) {
        this.destTerminalId = destTerminalId;
    }

    /**
     * @return the destTerminalType
     */
    public int getDestTerminalType() {
        return destTerminalType;
    }

    /**
     * @param destTerminalType the destTerminalType to set
     */
    public void setDestTerminalType(int destTerminalType) {
        this.destTerminalType = destTerminalType;
    }

    /**
     * @return the msgLength
     */
    public int getMsgLength() {
        return msgLength;
    }

    /**
     * @param msgLength the msgLength to set
     */
    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    /**
     * @return the msgContent
     */
    public byte[] getMsgContent() {
        return msgContent;
    }

    /**
     * @param msgContent the msgContent to set
     */
    public void setMsgContent(byte[] msgContent) {
        this.msgContent = msgContent;
    }

    /**
     * @return the linkID
     */
    public String getLinkID() {
        return linkID;
    }

    /**
     * @param linkID the linkID to set
     */
    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }

    /**
     * @return the maxMsgLength_short
     */
    public int getMaxMsgLength_short() {
        return maxMsgLength_short;
    }

    /** 
     * Description：获取所有接受手机
     * @param destTerminalId
     * @return void
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    @SuppressWarnings("unchecked")
    public void addDestTerminalId(String destTerminalId) {
        if (destTerminalId == null)
            return;
        this.destTerminalId.add(destTerminalId);
        destUsrTl = this.destTerminalId.size();
    }

    /** 
     * Description：获取string 信息内容
     * @return
     * @return String
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public String getStrMsgContent() {
        String strMsgContent = null;
        if (msgContent == null)
            return null;
        try {
            switch (msgFmt) {
                case 0:
                    strMsgContent = new String(msgContent, "US-ASCII");
                    break;
                case 3:
                    strMsgContent = new String(msgContent, "US-ASCII");
                    break;
                case 4:
                    strMsgContent = toPrintableString(msgContent);
                    break;
                case 8:
                    strMsgContent = new String(msgContent, "ISO-10646-UCS-2");
                    break;
                case 15:
                    strMsgContent = new String(msgContent, "GBK");
                    break;
                default:
                    strMsgContent = toPrintableString(msgContent);
            }
        } catch (UnsupportedEncodingException e) {
            strMsgContent = toPrintableString(msgContent);
        }
        return strMsgContent;
    }

    private String toPrintableString(byte[] b) {
        if (b == null)
            return null;
        StringBuffer sb = new StringBuffer();
        byte[] t = new byte[1];
        for (int i = 0; i < b.length; i++) {
            if (b[i] >= 0x20 && b[i] <= 0x7e) {
                t[0] = b[i];
                sb.append(new String(t));
            } else {
                sb.append(".");
            }
        }
        return sb.toString();
    }

}
