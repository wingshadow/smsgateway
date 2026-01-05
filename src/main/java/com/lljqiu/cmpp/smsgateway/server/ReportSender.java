package com.lljqiu.cmpp.smsgateway.server;

import com.alibaba.fastjson.JSON;
import com.lljqiu.cmpp.smsgateway.service.PutMsgService;
import com.lljqiu.cmpp.smsgateway.stack.MsgSubmit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ReportSender {

    private static final Logger logger = LoggerFactory.getLogger(ReportSender.class);

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
    public static void cachePendingReport(Socket socket, MsgSubmit submit, long msgId) {
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
        Socket socket = report.getSocket();
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
    private static void sendOne(Socket socket, MsgSubmit submit, long reportMsgId, String destTerminalId) {
        try {
            if (socket.isClosed() || !socket.isConnected()) {
                logger.warn("socket 已关闭，放弃发送状态报告, dest={}", destTerminalId);
                return;
            }

            byte[] deliver = PutMsgService.buildDeliverReport(
                    submit,
                    reportMsgId,
                    destTerminalId,
                    "DELIVRD"
            );

            synchronized (socket) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(deliver);
                out.flush();
            }

            logger.info("状态报告已发送, dest={}, msgId={}, submitSeq={}",
                    destTerminalId, reportMsgId, submit.getSequenceId());

        } catch (Exception e) {
            logger.error("发送状态报告失败, dest={}, submitSeq={}",
                    destTerminalId, submit.getSequenceId(), e);
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
        private final Socket socket;
        private final MsgSubmit submit;
        private final long msgId;

        public PendingReport(Socket socket, MsgSubmit submit, long msgId) {
            this.socket = socket;
            this.submit = submit;
            this.msgId = msgId;
        }

        public Socket getSocket() { return socket; }
        public MsgSubmit getSubmit() { return submit; }
        public long getMsgId() { return msgId; }
    }
}
