package com.happysnaker.handler.heartbeat.impl;

import com.happysnaker.handler.heartbeat.HeartbeatStrategy;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认的策略，给定一个阈值，此值必须大于 0，初始时通道有一个与此阈值相等的检测值，一旦通道死亡，此检测值减半；而当通道复活时，此值增加 1，但心跳值不可超过阈值。
 * <p>当检测值为 0 时，认定服务彻底死亡</p>
 * <strong>正所谓，乘法减小，加法增大</strong>
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
public class DefaultHeartbeatStrategy implements HeartbeatStrategy {

    int check;

    Map<Channel, Integer> map = new HashMap<>();

    public DefaultHeartbeatStrategy() {
        // 默认情况下最多两次机会
        this.check = 2;
    }

    @Override
    public void register(Channel channel) {
        map.put(channel, check);
    }

    @Override
    public void unRegister(Channel channel) {
        map.remove(channel);
    }

    @Override
    public boolean heartStop(Channel channel) {
        if (!map.containsKey(channel)) {
            return false;
        }
        map.put(channel, map.get(channel) / 2);
        boolean v = map.get(channel) <= 0;
        if (v) {
            unRegister(channel);
        }
        return v;
    }

    @Override
    public void heartbeat(Channel channel) {
        if (!map.containsKey(channel)) {
            return;
        }
        map.put(channel, Math.max(map.getOrDefault(channel, 0) + 1, check));
    }
}
