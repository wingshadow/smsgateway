/**
 * Project Name smsgateway
 * File Name package-info.java
 * Package Name com.lljqiu.cmpp.smsgateway.utils
 * Create Time 2017年5月13日
 * Create by name：liujie -- email: liujie@lljqiu.com
 * Copyright © 2015, 2017, www.lljqiu.com. All rights reserved.
 */
package com.lljqiu.cmpp.smsgateway.utils;

import java.util.ResourceBundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/** 
 * ClassName: GatewayConfig.java <br>
 * Description: 用于获取短信接口配置参数<br>
 * Create by: name：liujie <br>email: liujie@lljqiu.com <br>
 * Create Time: 2017年5月13日<br>
 */
public class GatewayConfig {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("smsgateway");

    public static String get(String key) {
        return resourceBundle.getString(key);
    }


    /**
     * 获取互联网短信网关端口号
     * @return
     */
    public static int getGatewayPort() {
        return Integer.parseInt(GatewayConfig.get("gatewayPort"));
    }
    /** 
     * Description：获取短信网关版本
     * @return
     * @return int
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static int getGatewayVersion() {
        return Integer.parseInt(GatewayConfig.get("gatewayVersion"));
    }

    /** 
     * Description：获取客户端配置
     * @return
     * @return List<JSONObject>
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static JSONArray getClientConfig(){
        String clientConfig = GatewayConfig.get("clientConfig");
        JSONArray jsonArray = JSONArray.parseArray(clientConfig);
        return jsonArray;
    }
    
    public static void main(String[] args) {
        JSONArray jsonArray = GatewayConfig.getClientConfig();
        System.out.println(jsonArray);
        for (int i = 0; i < jsonArray.size(); i++) {
            System.out.println(jsonArray.get(i));
            JSONObject json = (JSONObject) jsonArray.get(i);
            System.out.println(json.get(Constants.SPIP));
        }
    }
}
