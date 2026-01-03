package com.lljqiu.cmpp.smsgateway.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: smsgateway-master
 * @description:
 * @author: zhb
 * @create: 2026-01-03 15:47
 */
public class MsgIdGenerator {

    private static final AtomicLong SEQ = new AtomicLong(0);

    public static long nextId() {
        long timestamp = System.currentTimeMillis() / 1000; // 秒
        long seq = SEQ.incrementAndGet() & 0xFFFFF;          // 20bit
        return (timestamp << 20) | seq;
    }
}

