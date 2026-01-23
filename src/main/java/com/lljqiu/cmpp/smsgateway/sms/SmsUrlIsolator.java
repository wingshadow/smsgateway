package com.lljqiu.cmpp.smsgateway.sms;

import java.util.List;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-10 15:20
 */
public final class SmsUrlIsolator {

    private SmsUrlIsolator() {}

    public static String isolate(String content, List<String> urls) {
        String text = content;
        for (String url : urls) {
            text = text.replace(url, "{URL}");
        }
        return text;
    }
}

