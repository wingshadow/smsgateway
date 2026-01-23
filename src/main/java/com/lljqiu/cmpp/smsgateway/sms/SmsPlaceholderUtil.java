package com.lljqiu.cmpp.smsgateway.sms;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 实体占位替换工具
 *
 * 示例：
 *  text = "请拨打电话0531-88122234"
 *  replace(text, ["0531-88122234"], "{PHONE}")
 *  => "请拨打电话{PHONE}"
 */
public final class SmsPlaceholderUtil {

    private SmsPlaceholderUtil() {
    }

    public static String replace(String text, List<String> entities, String placeholder) {

        if (text == null || text.isEmpty() ||
                entities == null || entities.isEmpty()) {
            return text;
        }

        String result = text;
        for (String entity : entities) {
            if (entity == null || entity.isEmpty()) {
                continue;
            }
            String safe = Pattern.quote(entity);
            result = result.replaceAll(safe, placeholder);
        }
        return result;
    }
}
