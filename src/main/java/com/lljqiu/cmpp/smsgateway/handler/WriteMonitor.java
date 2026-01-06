package com.lljqiu.cmpp.smsgateway.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-05 16:43
 */
// 在服务端添加Channel写操作的监控
public class WriteMonitor extends ChannelOutboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WriteMonitor.class);
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 记录写入的线程
        String threadName = Thread.currentThread().getName();
        boolean inEventLoop = ctx.executor().inEventLoop();

        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            logger.info("[WriteMonitor] 写入数据 - 线程:{} (EventLoop线程:{}), 字节数:{}, 十六进制:{}",
                    threadName, inEventLoop, buf.readableBytes(),
                    ByteBufUtil.hexDump(buf, 0, Math.min(16, buf.readableBytes())));
        }

        if (!inEventLoop) {
            logger.error("❌ 线程安全问题: 写入操作在非EventLoop线程中!");
            // 打印堆栈跟踪
            new Exception("非EventLoop线程写操作").printStackTrace();
        }

        super.write(ctx, msg, promise);
    }
}

