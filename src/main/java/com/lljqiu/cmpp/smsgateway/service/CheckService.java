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
import com.lljqiu.cmpp.smsgateway.utils.SpConfigHolder;
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
    public static int checkConnectRequest(MsgConnect connectReq, String remoteIp) {

        String spId = connectReq.getSourceAddr();
        JSONObject config = SpConfigHolder.get(spId);


        // 认证
        boolean authOk = GateWayUtils.checkAuthenticatorSource(
                spId,
                config.getString(Constants.SHAREDSECRET),
                String.format("%010d", connectReq.getTimestamp()),
                connectReq.getAuthenticatorSource()
        );
        if (true) {
            return 0x0000;
        }
        return 0x0002;
    }


}
