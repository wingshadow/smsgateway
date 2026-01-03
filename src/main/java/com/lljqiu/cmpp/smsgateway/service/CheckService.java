/**
 * Project Name smsgateway
 * File Name CheckService.java
 * Package Name com.lljqiu.cmpp.smsgateway.service
 * Create Time 2017年5月13日
 * Create by name：liujie -- email: liujie@lljqiu.com
 * Copyright © 2015, 2017, www.lljqiu.com. All rights reserved.
 */
package com.lljqiu.cmpp.smsgateway.service;

import com.lljqiu.cmpp.smsgateway.utils.GateWayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.lljqiu.cmpp.smsgateway.cache.EhCache;
import com.lljqiu.cmpp.smsgateway.exception.GateWayException;
import com.lljqiu.cmpp.smsgateway.stack.MsgConnect;
import com.lljqiu.cmpp.smsgateway.utils.Constants;
import com.lljqiu.cmpp.smsgateway.utils.GatewayConfig;

/** 
 * ClassName: CheckService.java <br>
 * Description: 校验请求<br>
 * Create by: name：liujie <br>email: liujie@lljqiu.com <br>
 * Create Time: 2017年5月13日<br>
 */
public class CheckService {
    private static Logger logger = LoggerFactory.getLogger(CheckService.class);

    public static void checkConnectRequest(MsgConnect connectReq, String remoteIp) {

        String spId = connectReq.getSourceAddr();

        logger.info("CMPP CONNECT spId={}, remoteIp={}, version={}",
                spId, remoteIp, connectReq.getVersion());

        JSONObject json = (JSONObject) EhCache.get(EhCache.CACHE_NAME, spId);
        logger.info("json:{}",json.toJSONString());
        GateWayException.checkCondition(json == null, 0x0002, "SPID不存在");



        // 认证
        boolean authOk = GateWayUtils.checkAuthenticatorSource(
                spId,
                json.getString(Constants.SHAREDSECRET),
                String.format("%010d", connectReq.getTimestamp()),
                connectReq.getAuthenticatorSource()
        );
        // todo 注释掉以后再调
        GateWayException.checkCondition(!authOk, 0x0003, "认证失败");

        // 版本
        GateWayException.checkCondition(
                connectReq.getVersion() > GatewayConfig.getGatewayVersion(),
                0x0004,
                "协议版本过高"
        );
    }



}
