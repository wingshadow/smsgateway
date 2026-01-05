package com.lljqiu.cmpp.smsgateway.handler;

import com.lljqiu.cmpp.smsgateway.service.PutMsgService;
import com.lljqiu.cmpp.smsgateway.stack.MsgSubmit;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NettyReportSender {

    private static final Logger logger = LoggerFactory.getLogger(NettyReportSender.class);

    // 线程池发送状态报告
    private static final ScheduledExecutorService EXECUTOR =
            Executors.newScheduledThreadPool(4, r -> {
                Thread t = new Thread(r, "cmpp-report-sender");
                t.setDaemon(true);
                return t;
            });

    // 待发送状态报告队列，key = submitSeq
    private static final Map<Integer, PendingReport> pendingReports = new ConcurrentHashMap<>();

    /**
     * 缓存待发送状态报告
     */
    public static void cachePendingReport(Channel socket, MsgSubmit submit, long msgId) {
        if (socket == null || submit == null) {
            return;
        }

        PendingReport report = new PendingReport(socket, submit, msgId);
        pendingReports.put(submit.getSequenceId(), report);
        logger.info("状态报告已缓存，等待发送, seq={}, msgId={}", submit.getSequenceId(), msgId);
    }

    /**
     * 提交待发送状态报告（短信下发或 deliver 事件触发）
     */
    public static void submitPendingReports(int submitSeq) {
        PendingReport report = pendingReports.get(submitSeq);
        if (report == null) {
            return;
        }
        MsgSubmit submit = report.getSubmit();
        Channel socket = report.getSocket();
        long msgId = report.getMsgId();
        List<String> destList = submit.getDestTerminalId();
        if (destList == null || destList.isEmpty()) {
            return;
        }

        int delay = 0;
        for (String dest : destList) {
            logger.info("发送, seq={}, msgId={}", submit.getSequenceId(), msgId);
            EXECUTOR.schedule(() -> sendOne(socket, submit, msgId, dest),
                    delay, TimeUnit.MILLISECONDS);
            delay += 200;
        }
    }

    /**
     * 发送单条状态报告
     */
    private static void sendOne(
            Channel channel,
            MsgSubmit submit,
            long reportMsgId,
            String destTerminalId) {

        try {
            // 1️⃣ Netty Channel 状态判断
            if (channel == null || !channel.isActive()) {
                logger.warn("channel 未激活，放弃发送状态报告, dest={}", destTerminalId);
                return;
            }

            // 2️⃣ 构建 DELIVER 状态报告
            byte[] deliver = PutMsgService.buildDeliverReport(
                    submit,
                    reportMsgId,
                    destTerminalId,
                    "DELIVRD"
            );

            // 3️⃣ Netty 写出（线程安全，不需要 synchronized）
            channel.writeAndFlush(Unpooled.wrappedBuffer(deliver))
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            logger.info("状态报告已发送, dest={}, msgId={}, submitSeq={}",
                                    destTerminalId,
                                    reportMsgId,
                                    submit.getSequenceId());
                        } else {
                            logger.error("状态报告发送失败, dest={}, submitSeq={}",
                                    destTerminalId,
                                    submit.getSequenceId(),
                                    future.cause());
                        }
                    });

        } catch (Exception e) {
            logger.error("发送状态报告异常, dest={}, submitSeq={}",
                    destTerminalId,
                    submit.getSequenceId(),
                    e);
        }
    }


    /**
     * 服务关闭时调用
     */
    public static void shutdown() {
        logger.info("ReportSender shutdown...");
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 待发送状态报告封装类
     */
    private static class PendingReport {
        private final Channel socket;
        private final MsgSubmit submit;
        private final long msgId;

        public PendingReport(Channel socket, MsgSubmit submit, long msgId) {
            this.socket = socket;
            this.submit = submit;
            this.msgId = msgId;
        }

        public Channel getSocket() { return socket; }
        public MsgSubmit getSubmit() { return submit; }
        public long getMsgId() { return msgId; }
    }
}
