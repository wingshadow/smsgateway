package com.lljqiu.cmpp.smsgateway.sms;

import java.util.ArrayList;
import java.util.List;

public class SmsParseResult {

    private String rawText;
    private String normalizedText;

    private List<String> urls = new ArrayList<>();
    private List<String> phones = new ArrayList<>();
    private List<String> pickupCodes = new ArrayList<>();
    private List<String> signs = new ArrayList<>();

    private List<String> dateRanges = new ArrayList<>();

    private String template;

    public List<String> getDateRanges() {
        return dateRanges;
    }

    public void setDateRanges(List<String> dateRanges) {
        this.dateRanges = dateRanges;
    }

    private SmsType finalType;

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public void setNormalizedText(String normalizedText) {
        this.normalizedText = normalizedText;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public List<String> getPickupCodes() {
        return pickupCodes;
    }

    public void setPickupCodes(List<String> pickupCodes) {
        this.pickupCodes = pickupCodes;
    }

    public List<String> getSigns() {
        return signs;
    }

    public void setSigns(List<String> signs) {
        this.signs = signs;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public SmsType getFinalType() {
        return finalType;
    }

    public void setFinalType(SmsType finalType) {
        this.finalType = finalType;
    }
}
