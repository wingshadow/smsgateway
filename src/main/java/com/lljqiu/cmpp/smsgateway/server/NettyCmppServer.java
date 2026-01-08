package com.lljqiu.cmpp.smsgateway.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.WriteBufferWaterMark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CMPP Netty Server with optional EPOLL and non-blocking startup
 */
public class NettyCmppServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyCmppServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    private volatile boolean stopped = false;

    private final int port;

    public NettyCmppServer(int port) {
        this.port = port;
    }

    /**
     * 启动后台线程
     */
    public void startAsync() {
        Thread nettyThread = new Thread(this::start, "CMPP-Netty-Server-Thread");
        nettyThread.setDaemon(false);
        nettyThread.start();
    }

    /**
     * 阻塞启动服务器
     */
    private void start() {
        boolean useEpoll = Epoll.isAvailable();
        Class<? extends ServerChannel> channelClass;

        if (useEpoll) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
            channelClass = EpollServerSocketChannel.class;
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
            channelClass = NioServerSocketChannel.class;
        }

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(channelClass)
                    .childHandler(new CmppServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 256 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 256 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(32 * 1024, 128 * 1024))
                    .childOption(ChannelOption.SO_REUSEADDR, true);

            ChannelFuture f = b.bind(port).sync();
            serverChannel = f.channel();

            logger.info("CMPP Netty Server started on port {} using {}", port, useEpoll ? "EPOLL" : "NIO");

            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("CMPP Server thread interrupted", e);
        } catch (Exception e) {
            logger.error("CMPP Server failed", e);
        } finally {
            stop();
        }
    }

    /**
     * 优雅关闭
     */
    public void stop() {
        if (stopped) return;
        stopped = true;

        logger.info("Stopping CMPP Netty Server...");

        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
                serverChannel = null;
            }
        } catch (Exception e) {
            logger.warn("Exception while closing serverChannel", e);
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            bossGroup = null;
        }

        logger.info("CMPP Netty Server stopped");
    }

    public static void main(String[] args) {
        int port = 7891;
        NettyCmppServer server = new NettyCmppServer(port);
        server.startAsync();

        // 模拟主线程其他逻辑
        try {
            Thread.sleep(60_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 停服
        server.stop();
    }
}
