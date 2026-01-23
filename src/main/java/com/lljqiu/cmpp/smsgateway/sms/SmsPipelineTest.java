package com.lljqiu.cmpp.smsgateway.sms;

import java.util.Arrays;
import java.util.List;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-10 12:21
 */
public class SmsPipelineTest {

    public static void main(String[] args) {

        List<String> testSms = Arrays.asList(
                "【盒马】新年新意满79返30！砂糖橘950g9.9元，五花肉700g21.9元→ hmxs.cn/A_2gRgnZ 拒收请回复R",
                "【泰康泰生活】验证码：884532，您正在登录泰生活，有效期5分钟，请妥善保管。",
                "【中国联通】您2025年12月份通信账单信息：用户号码：15628765001；计费周期：12月1日-12月31日；账单查询详情请点击：http://u.10010.cn/tAE3W 。回复TDZD退订本短信。",
                "【兔喜生活】您有包裹已到达历山吉第店，取件码为7-3-1873，地址:历山吉第兔喜超市",
                "【账单提醒】您2025年12月份通信账单信息：用户号码：15628765001；计费周期：12月1日-12月31日；账单查询详情请点击： http://u.10010.cn/tAE3W 。回复TDZD退订本短信。【中国联通】",
                "尊敬的客户，元启新程，旦愿美好。尊敬的客户，感谢您在过去的一年对工商银行的鼎力支持！新年伊始，愿您光风霁月，坦途万里，四季从容，自由灿烂！祝您元旦快乐！【工商银行】【刘草环:15610177236】如需退订此类信息，回复TDXS#EFKG至95588。【工商银行】",
                "【达美乐】送您一张9“免费比萨心意券DQQDOTLG4IHLCND以表送餐晚到歉意。官网微信APP可用，详见官网，本订单完成后60日内有效",
                "【爱回收】您于2025-10-26 16:25:08投递0.05千克，获得0.03元。如需获取更多信息，请微信搜索“爱回收”关注公众号",
                "【人寿】请拨打电话0531-88122234"

        );
        for(String content : testSms){
            SmsParseResult r = SmsPipelineProcessor.process(content);

            System.out.println("====== 原文 ======");
            System.out.println(content);

            System.out.println("Normalized: " + r.getNormalizedText());
            System.out.println("Signs: " + r.getSigns());
            System.out.println("Phones: " + r.getPhones());
            System.out.println("URLs: " + r.getUrls());
            System.out.println("Template: " + r.getTemplate());
            System.out.println("FinalType: " + r.getFinalType());
        }

    }
}

