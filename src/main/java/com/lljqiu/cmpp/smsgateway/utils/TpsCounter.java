package com.lljqiu.cmpp.smsgateway.utils;

import com.lljqiu.cmpp.smsgateway.handler.CmppServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-06 15:58
 */
public final class TpsCounter {

    private static final Logger log = LoggerFactory.getLogger(TpsCounter.class);
    /**
     * 总请求数
     */
    private static final LongAdder TOTAL = new LongAdder();

    /**
     * 上一秒请求数
     */
    private static final AtomicLong LAST_TOTAL = new AtomicLong(0);

    /**
     * 启动时间
     */
    private static final long START_TIME = System.currentTimeMillis();

    /**
     * 定时打印线程
     */
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "TPS-REPORTER");
                t.setDaemon(true);
                return t;
            });

    static {
        // 每 1 秒统计一次
        SCHEDULER.scheduleAtFixedRate(() -> {
            long nowTotal = TOTAL.sum();
            long lastTotal = LAST_TOTAL.getAndSet(nowTotal);

            long tps = nowTotal - lastTotal;
            long uptime = (System.currentTimeMillis() - START_TIME) / 1000;

            // ⚠️ 建议 DEBUG 或 INFO（压测时可关）
            log.info(
                    "[TPS] current=" + tps +
                            ", total=" + nowTotal +
                            ", uptime=" + uptime + "s"
            );
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 记录一次
     */
    public static void mark() {
        TOTAL.increment();
    }

    private TpsCounter() {
    }
}

