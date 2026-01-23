package com.lljqiu.cmpp.smsgateway.sms;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 账期 / 日期区间提取
 * 示例：12月1日-12月31日
 */
public final class SmsDateRangeExtractor {

    private static final Pattern DATE_RANGE_PATTERN =
            Pattern.compile("(\\d{1,2})月(\\d{1,2})日\\s*[-~至]\\s*(\\d{1,2})月(\\d{1,2})日");

    private SmsDateRangeExtractor() {}

    public static List<String> extract(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        Matcher matcher = DATE_RANGE_PATTERN.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
}
