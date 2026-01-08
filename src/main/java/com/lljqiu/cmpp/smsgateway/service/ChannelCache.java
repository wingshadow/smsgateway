package com.lljqiu.cmpp.smsgateway.service;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放终端SocketChannel
 *
 * @author hawk
 * @version 1.0.0 2016年12月23日 上午10:33:05
 */

public class ChannelCache {

    private static volatile ChannelCache instance;
    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    private ChannelCache() {
    }

    public static ChannelCache getInstance() {
        if (instance == null) {
            synchronized (ChannelCache.class) {
                if (instance == null) {
                    instance = new ChannelCache();
                }
            }
        }
        return instance;
    }

    public void add(Channel channel) {
        channelMap.put(channel.id().asLongText(),channel);
    }

    public void remove(Channel channel) {
        channelMap.remove(channel.id().asLongText());
    }

    public Channel get(String channelId) {
        return channelMap.get(channelId);
    }

    public int size(){
        return channelMap.size();
    }

}

