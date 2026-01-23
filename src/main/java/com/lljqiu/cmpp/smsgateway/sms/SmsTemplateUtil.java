package com.lljqiu.cmpp.smsgateway.sms;

import java.util.List;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-10 12:20
 */
public final class SmsTemplateUtil {

    public static String template(
            String content,
            List<String> pickupCodes,
            List<String> numbers,
            List<String> decimals,
            List<String> urls) {

        String text = content;

        for (String url : urls) {
            text = text.replace(url, "{URL}");
        }

        for (String code : pickupCodes) {
            text = text.replace(code, "{PICKUP_CODE}");
        }

        for (String num : numbers) {
            text = text.replace(num, "{NUM}");
        }

         for (String price : decimals) {
             text = text.replace(price, "{PRICE}");
         }

        return text;
    }
}
