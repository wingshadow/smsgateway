package com.lljqiu.cmpp.smsgateway.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-10 12:19
 */
public final class SmsNumberExtractor {

    private static final Pattern NUM_PATTERN =
            Pattern.compile("(?<!\\d)\\d{2,13}(?!\\d)");

    public static List<String> extract(String content) {
        List<String> list = new ArrayList<>();
        Matcher m = NUM_PATTERN.matcher(content);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }
}

