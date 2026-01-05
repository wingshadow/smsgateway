package com.lljqiu.cmpp.smsgateway.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: smsgateway
 * @description:
 * @author: zhb
 * @create: 2026-01-05 09:33
 */
public class NettyCmppServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyCmppServer.class);

    public void start(int port) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new CmppServerInitializer());

            ChannelFuture f = b.bind(port).sync();
            logger.info("CMPP Netty Server started on port {}", port);

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

