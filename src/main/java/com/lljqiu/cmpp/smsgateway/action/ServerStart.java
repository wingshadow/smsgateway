/**
 * Project Name smsgateway
 * File Name ServerStart.java
 * Package Name com.lljqiu.cmpp.smsgateway.action
 * Create Time 2017年5月13日
 * Create by name：liujie -- email: liujie@lljqiu.com
 */
package com.lljqiu.cmpp.smsgateway.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lljqiu.cmpp.smsgateway.cache.EhCache;
import com.lljqiu.cmpp.smsgateway.server.NettyCmppServer;
import com.lljqiu.cmpp.smsgateway.server.SMSServer;
import com.lljqiu.cmpp.smsgateway.utils.Constants;
import com.lljqiu.cmpp.smsgateway.utils.GatewayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * ClassName: ServerStart.java <br>
 * Description: <br>
 * Create by: name：liujie <br>email: liujie@lljqiu.com <br>
 * Create Time: 2017年5月13日<br>
 */
public class ServerStart {
    private static Logger logger = LoggerFactory.getLogger(ServerStart.class);
    public static void main(String[] args) {
//        SMSServer server = new SMSServer();
        JSONArray jsonArray = GatewayConfig.getClientConfig();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = (JSONObject) jsonArray.get(i);
            EhCache.put(EhCache.CACHE_NAME, json.get(Constants.SPID), json);
        }
//        server.start();

        int port = GatewayConfig.getGatewayPort();
        NettyCmppServer server = new NettyCmppServer();

        try {
            logger.info("Starting Netty CMPP Server on port {}", port);
            server.start(port);
        } catch (InterruptedException e) {
            logger.error("Netty CMPP Server start interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

}
