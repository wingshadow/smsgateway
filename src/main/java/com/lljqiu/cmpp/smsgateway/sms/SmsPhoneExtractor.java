package com.lljqiu.cmpp.smsgateway.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提取真实联系电话（手机号 / 座机）
 */
public final class SmsPhoneExtractor {

    /**
     * 1️⃣ 手机号：1[3-9]xxxxxxxxx
     * 2️⃣ 座机：0xx-xxxxxxx / 0xxx-xxxxxxxx
     * 3️⃣ 不允许被数字包裹，防止误伤时间/金额
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)(" +
                    "1[3-9]\\d{9}" +                 // 手机
                    "|" +
                    "0\\d{2,3}-\\d{7,8}" +           // 带横线座机
                    "|" +
                    "0\\d{2,3}\\d{7,8}" +            // 不带横线座机
                    ")(?!\\d)"
    );

    private SmsPhoneExtractor() {
    }

    public static List<String> extract(String text) {

        List<String> list = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return list;
        }

        Matcher matcher = PHONE_PATTERN.matcher(text);
        while (matcher.find()) {
            list.add(matcher.group(1));
        }
        return list;
    }
}

