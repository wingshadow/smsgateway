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
import com.lljqiu.cmpp.smsgateway.utils.CmppEncoder;
import com.lljqiu.cmpp.smsgateway.utils.Sequence;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lljqiu.cmpp.smsgateway.exception.GateWayException;
import com.lljqiu.cmpp.smsgateway.utils.GateWayUtils;

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
                ToStringBuilder.reflectionToString(connectResp));

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

    public static byte[] buildDeliverReport(MsgSubmit submit,
                                            long reportMsgId,
                                            String destTerminalId,
                                            String stat) {

        MsgDeliver deliver = new MsgDeliver();

        // ===== 消息头 =====
        deliver.setCommandId(MsgCommand.CMPP_DELIVER);
        deliver.setSequenceId(Sequence.next());

        /**
         * CMPP3.0 约定：
         * 状态报告时，DELIVER.Msg_Id = 0
         */
        deliver.setMsgId(0L);

        // ===== Deliver 固定字段 =====
        deliver.setDestId(submit.getSrcId());          // SP号
        deliver.setServiceId(submit.getServiceId());
        deliver.setTpPid(submit.getTpPId());
        deliver.setTpUdhi(submit.getTpUdhi());
        deliver.setMsgFmt(submit.getMsgFmt());

        // 源终端：用户手机号（单个）
        deliver.setSrcTerminalId(destTerminalId);
        deliver.setSrcTerminalType(0);

        // 关键：状态报告
        deliver.setRegisteredDelivery(1);

        // 状态报告 Msg_Content 固定 60
        deliver.setMsgLength(60);

        // ===== MsgReport =====
        MsgReport report = new MsgReport();

        /**
         * 这里必须是：
         * ISMG 在 SUBMIT_RESP 中返回的 Msg_Id
         */
        report.setMsgId(reportMsgId);

        /**
         * DELIVRD / UNDELIV / EXPIRED / REJECTD
         * 7字节，右补 0x00
         */
        report.setStat(stat);

        // YYMMDDHHMM
        report.setSubmitTime(now());
        report.setDoneTime(now());

        // 单个目标手机号
        report.setDestTerminalId(destTerminalId);

        /**
         * 一般取 SUBMIT 的 sequenceId
         */
        report.setSmscSequence(submit.getSequenceId());

        deliver.setReport(report);

        return CmppEncoder.encode(deliver);
    }

    private static String now() {
        // CMPP 要求：YYMMDDHHMM
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyMMddHHmm");
        return LocalDateTime.now().format(formatter);
    }


}
