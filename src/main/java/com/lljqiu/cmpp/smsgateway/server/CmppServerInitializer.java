package com.lljqiu.cmpp.smsgateway.server;

import com.lljqiu.cmpp.smsgateway.handler.CmppFrameDecoder;
import com.lljqiu.cmpp.smsgateway.handler.CmppServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

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
        p.addLast("CmppFrameDecoder",new CmppFrameDecoder());
        p.addLast("cmppHandler", new CmppServerHandler());
    }
}

