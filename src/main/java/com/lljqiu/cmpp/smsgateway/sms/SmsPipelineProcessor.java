package com.lljqiu.cmpp.smsgateway.sms;

import java.util.List;

public class SmsPipelineProcessor {

    public static SmsParseResult process(String content) {

        SmsParseResult result = new SmsParseResult();
        result.setRawText(content);

        // 1️⃣ Normalize
        String normalized = SmsNormalizeUtil.normalize(content);
        result.setNormalizedText(normalized);

        // 2️⃣ Sign
        result.getSigns().addAll(
                SmsSignExtractor.extractSignatures(content)
        );

        // 3️⃣ URL
        List<String> urls = SmsUrlExtractor.extract(normalized);
        result.getUrls().addAll(urls);
        String text = SmsUrlIsolator.isolate(normalized, urls);

        // 4️⃣ Phone
        List<String> phones = SmsPhoneExtractor.extract(text);
        result.getPhones().addAll(phones);
        text = SmsPlaceholderUtil.replace(text, phones, "{PHONE}");

        // 5️⃣ PickupCode / VerifyCode
        List<String> pickCodes = SmsPickupCodeExtractor.extract(text);
        result.getPickupCodes().addAll(pickCodes);
        text = SmsPlaceholderUtil.replace(text, pickCodes, "{CODE}");

        List<String> dateRanges = SmsDateRangeExtractor.extract(text);
        result.getDateRanges().addAll(dateRanges);
        text = SmsPlaceholderUtil.replace(text, dateRanges, "{DATE_RANGE}");

        // 6️⃣ 数字（仅用于模板，不进 result）
        List<String> numbers = SmsNumberExtractor.extract(text);
        List<String> decimals = SmsDecimalExtractor.extract(text);

        // 7️⃣ 短信级分类（🔥关键）
        boolean hasUrl = !result.getUrls().isEmpty();
        boolean hasPhone = !result.getPhones().isEmpty();

        boolean hasSign = !result.getSigns().isEmpty();

        // ===== 最终业务判定 =====
        // 只有【商品营销 + URL】才算营销，其余全部是签名短信
        if (hasSign && (hasUrl||hasPhone)) {
            result.setFinalType(SmsType.MARKETING);
        } else if (hasSign) {
            result.setFinalType(SmsType.SIGN_ONLY);
        } else {
            result.setFinalType(SmsType.UNKNOWN);
        }

        // 8️⃣ Template
        String template = SmsTemplateUtil.template(
                text, pickCodes, numbers, urls, decimals
        );
        result.setTemplate(template);

        return result;
    }

}
