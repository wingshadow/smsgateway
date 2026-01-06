package com.lljqiu.cmpp.smsgateway.server;

import com.lljqiu.cmpp.smsgateway.handler.CmppServerHandler;
import com.lljqiu.cmpp.smsgateway.handler.WriteMonitor;
import com.lljqiu.cmpp.smsgateway.utils.GateWayUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-05 09:35
 */
public class CmppServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
//        p.addLast("rawPrinter", new ChannelInboundHandlerAdapter() {
//            @Override
//            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                if (msg instanceof ByteBuf) {
//                    ByteBuf buf = (ByteBuf) msg;
//
//                    // 复制数据，打印完整原始数据
//                    byte[] data = new byte[buf.readableBytes()];
//                    buf.getBytes(buf.readerIndex(), data);
//                    System.out.println("原始数据（未解码）: " + GateWayUtils.toHex(data));
//
//                    // 传递给下一个 handler
//                    ctx.fireChannelRead(buf.retain());
//                } else {
//                    ctx.fireChannelRead(msg);
//                }
//            }
//        });

        p.addLast("frameDecoder",
                new LengthFieldBasedFrameDecoder(
                        8 * 1024,
                        0,
                        4,
                        -4,
                        4
                )
        );
        p.addLast("cmppHandler", new CmppServerHandler());
    }
}

