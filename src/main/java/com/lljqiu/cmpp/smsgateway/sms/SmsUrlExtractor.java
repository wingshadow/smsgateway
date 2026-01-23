package com.lljqiu.cmpp.smsgateway.sms;

import java.util.*;
import java.util.regex.*;

public final class SmsUrlExtractor {

    private SmsUrlExtractor() {}

    /**
     * 短信 URL 匹配：
     * - http / https
     * - 短链：xxx.cn/Ab3
     * - 无协议域名
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?i)\\b(" +
                    "(?:https?://)?" +                 // 可选协议
                    "(?:[a-z0-9-]+\\.)+" +              // 域名
                    "(?:cn|com|net|cc|io|top|vip)" +    // 常见后缀
                    "(?:/[a-z0-9_\\-./?=&%]*)?" +       // 路径参数
                    ")"
    );

    public static List<String> extract(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> urls = new ArrayList<>();
        Matcher matcher = URL_PATTERN.matcher(content);

        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }
}
