package com.lljqiu.cmpp.smsgateway.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-09 10:24
 */
public class SpConfigHolder {

    // 并发安全 + 读性能极高
    private static final ConcurrentHashMap<String, JSONObject> SP_MAP = new ConcurrentHashMap<>();

    public static void init() {
        JSONArray jsonArray = GatewayConfig.getClientConfig();
        if (jsonArray == null) {
            throw new IllegalStateException("client config is null");
        }

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            if (json == null) {
                continue;
            }
            String spId = json.getString(Constants.SPID);
            if (spId == null) {
                continue;
            }
            SP_MAP.put(spId, json);
        }
    }

    public static JSONObject get(String spId) {
        return SP_MAP.get(spId);
    }

    public static boolean contains(String spId) {
        return SP_MAP.containsKey(spId);
    }
}

