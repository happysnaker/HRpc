package com.happysnaker.handler.heartbeat;

import io.netty.channel.Channel;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
public interface HeartbeatStrategy {

    /**
     * 注册一个通道，表面将对此通道进行心跳检测
     * @param channel
     */
    void register(Channel channel);

    /**
     * 取消注册通道
     * @param channel
     */
    void unRegister(Channel channel);

    /**
     * 当一个通道心跳停止时调用此方法
     * @param channel 注意，此通道可能是任意通道，不一定是注册的通道
     * @return true 表示应该移除此通道；false 表示应该继续给予此通道一次机会
     */
    boolean heartStop(Channel channel);

    /**
     * 指示此 channel 心跳跳动，此方法只对已注册的 channel 感兴趣
     * @param channel
     */
    void heartbeat(Channel channel);
}
