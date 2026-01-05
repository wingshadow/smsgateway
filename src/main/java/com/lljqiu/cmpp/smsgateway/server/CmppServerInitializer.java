package com.lljqiu.cmpp.smsgateway.server;

import com.lljqiu.cmpp.smsgateway.handler.CmppServerHandler;
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

        // CMPP 是 length-field-based
        p.addLast(new LengthFieldBasedFrameDecoder(
                1024 * 1024, // maxFrameLength
                0,           // lengthFieldOffset
                4,           // lengthFieldLength
                -4,          // lengthAdjustment
                4            // ⭐ strip 掉 Total_Length
        ));


        p.addLast(new CmppServerHandler());
    }
}
