package com.lljqiu.cmpp.smsgateway.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-10 12:18
 */
public final class SmsSignExtractor {

    // 匹配所有【…】或[…]
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("[【\\[](.+?)[】\\]]");

    /**
     * 提取短信中的所有签名
     * @param sms 短信内容
     * @return 所有签名列表，顺序与短信中出现顺序一致
     */
    public static List<String> extractSignatures(String sms) {
        List<String> signatures = new ArrayList<>();
        if (sms == null || sms.isEmpty()) {
            return signatures;
        }

        Matcher matcher = SIGNATURE_PATTERN.matcher(sms);
        while (matcher.find()) {
            signatures.add(matcher.group(1).trim());
        }
        return signatures;
    }
}

