package com.lljqiu.cmpp.smsgateway.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-10 15:28
 */
public final class SmsPickupCodeExtractor {

    private static final Pattern PICKUP_CODE_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{1,4}(?:-\\d{1,4}){1,3})(?!\\d)");

    public static List<String> extract(String text) {
        List<String> result = new ArrayList<>();
        Matcher m = PICKUP_CODE_PATTERN.matcher(text);
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }
}
