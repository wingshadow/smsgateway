package com.lljqiu.cmpp.smsgateway.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-10 15:14
 */
public final class SmsDecimalExtractor {

    private static final Pattern DECIMAL_PATTERN =
            Pattern.compile("(?<!\\d)\\d+\\.\\d{1,2}(?!\\d)");

    public static List<String> extract(String content) {
        List<String> list = new ArrayList<>();
        Matcher m = DECIMAL_PATTERN.matcher(content);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }
}
