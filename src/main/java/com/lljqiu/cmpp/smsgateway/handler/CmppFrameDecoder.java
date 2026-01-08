package com.lljqiu.cmpp.smsgateway.handler;

import com.lljqiu.cmpp.smsgateway.service.ChannelCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.TooLongFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-06 21:45
 */
public class CmppFrameDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CmppFrameDecoder.class);

    private ByteBuf buffer = Unpooled.buffer();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        buffer.writeBytes(in);
        in.release();

        while (true) {
            // 数据不够 4 字节长度字段就退出
            if (buffer.readableBytes() < 4) {
                return;
            }

            buffer.markReaderIndex(); // 标记当前位置

            int totalLength = buffer.readInt();

            // 长度非法处理
            if (totalLength < 12 || totalLength > 10 * 1024 * 1024) {
                ctx.fireExceptionCaught(new TooLongFrameException("非法消息长度: " + totalLength));
                buffer.clear(); // 丢掉缓存
                return;
            }

            // 数据不够完整就等下一次
            if (buffer.readableBytes() < totalLength - 4) {
                buffer.resetReaderIndex();
                return;
            }

            // 读完整消息
            ByteBuf frame = buffer.readRetainedSlice(totalLength - 4);
            // 交给下一个 handler 处理
            ctx.fireChannelRead(frame);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelCache.getInstance().add(ctx.channel());
        InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("[CMPP] Client connected: " + remote.getAddress().getHostAddress() + ":" + remote.getPort());
        log.info("[CMPP] 当前活跃连接数: " + ChannelCache.getInstance().size());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
        ChannelCache.getInstance().remove(ctx.channel());
        log.info("[CMPP] 当前活跃连接数: " + ChannelCache.getInstance().size());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof TooLongFrameException) {
            System.err.println("丢弃非法消息: " + cause.getMessage());
        } else {
            cause.printStackTrace();
        }
    }
}

