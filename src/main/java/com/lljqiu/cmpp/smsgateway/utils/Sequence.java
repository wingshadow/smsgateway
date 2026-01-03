package com.lljqiu.cmpp.smsgateway.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: smsgateway-master
 * @description:
 * @author: zhb
 * @create: 2026-01-03 15:02
 */
public final class Sequence {

    private static final AtomicInteger SEQ =
            new AtomicInteger(0);

    private Sequence() {}

    public static int next() {
        int v = SEQ.incrementAndGet();

        // CMPP Sequence_Id 是 unsigned int
        if (v == Integer.MAX_VALUE) {
            SEQ.compareAndSet(v, 0);
        }
        return v;
    }
}

