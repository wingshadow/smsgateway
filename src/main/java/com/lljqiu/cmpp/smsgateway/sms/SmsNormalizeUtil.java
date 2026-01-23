package com.lljqiu.cmpp.smsgateway.sms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SmsNormalizeUtil {

    private SmsNormalizeUtil() {}

    // ========================= Pattern 预编译 =========================
    private static final Pattern FULL_WIDTH_DIGIT = Pattern.compile("[０-９]");
    private static final Pattern HTTP_SPLIT =
            Pattern.compile("h\\s*[x]*\\s*t\\s*t\\s*p\\s*s?", Pattern.CASE_INSENSITIVE);

    private static final Pattern URL_CHAR_SPLIT =
            Pattern.compile("(?<=[a-z0-9])\\s+(?=[a-z0-9])");

    // ========================= 入口 =========================
    public static String normalize(String content) {
        if (content == null) {
            return "";
        }

        String text = content;

        // ==================================================
        // 1️⃣ 全角符号 → 半角（只做等价替换）
        // ==================================================
        text = normalizePunctuation(text);

        // ==================================================
        // 2️⃣ 全角数字 → 半角
        // ==================================================
        text = normalizeFullWidthDigits(text);

        // ==================================================
        // 3️⃣ 中文 / 圈 数字
        // ==================================================
        text = normalizeChineseDigits(text);

        // ==================================================
        // 4️⃣ 英文小写
        // ==================================================
        text = text.toLowerCase();

        // ==================================================
        // 5️⃣ http / hxxp 拆分修复
        // ==================================================
        text = normalizeHttp(text);

        // ==================================================
        // 6️⃣ URL / 电话 连续性修复
        // ==================================================
        text = URL_CHAR_SPLIT.matcher(text).replaceAll("");

        // ==================================================
        // 7️⃣ 空白收敛
        // ==================================================
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    // ========================= 子能力 =========================

    private static String normalizePunctuation(String text) {
        return text.replace('【', '[').replace('】', ']')
                .replace('（', '(').replace('）', ')')
                .replace('！', '!')
                .replace('￥', '¥')
                .replace('。', '.')
                .replace('，', ',')
                .replace('：', ':')
                .replace('／', '/')
                .replace('．', '.');
    }

    private static String normalizeFullWidthDigits(String text) {
        Matcher matcher = FULL_WIDTH_DIGIT.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            char full = matcher.group().charAt(0);
            char half = (char) ('0' + (full - '０'));
            matcher.appendReplacement(sb, String.valueOf(half));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String normalizeChineseDigits(String text) {
        return text.replace("零","0").replace("一","1").replace("二","2")
                .replace("三","3").replace("四","4").replace("五","5")
                .replace("六","6").replace("七","7").replace("八","8")
                .replace("九","9")
                .replace("①","1").replace("②","2").replace("③","3")
                .replace("④","4").replace("⑤","5").replace("⑥","6")
                .replace("⑦","7").replace("⑧","8").replace("⑨","9");
    }

    private static String normalizeHttp(String text) {
        Matcher matcher = HTTP_SPLIT.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String fixed = matcher.group()
                    .replace("x", "")
                    .replaceAll("\\s+", "");
            matcher.appendReplacement(sb, fixed);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
