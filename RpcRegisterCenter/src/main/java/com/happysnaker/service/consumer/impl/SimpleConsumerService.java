package com.happysnaker.service.consumer.impl;

import com.happysnaker.common.builder.RpcMessageBuilder;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.Service;
import com.happysnaker.service.consumer.ConsumerService;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
@Slf4j
public class
SimpleConsumerService implements ConsumerService {

    public static Map<String, Set<Channel>> subscribeMap;

    @Override
    public List<Service> lookup(String service, Channel channel) {
        List<Service> serviceList = serviceListManager.getServiceList(service);
        return serviceList == null ? new ArrayList<>() : serviceList;
    }

    @Override
    public void subscribe(String service, Channel channel) {
        if (subscribeMap == null) {
            subscribeMap = new ConcurrentHashMap<>(16);
        }
        subscribeMap.putIfAbsent(service, new ConcurrentSet<>());
        subscribeMap.get(service).add(channel);
        log.info(channel.toString() + ": subscribe service: {}", service);
    }

    @Override
    public void unSubscribe(String service, Channel channel) {
        if (subscribeMap != null && subscribeMap.containsKey(service)) {
            subscribeMap.get(service).remove(channel);
            log.info(channel.toString() + ": cancel subscribe service: {}", service);
        }
    }

    @Override
    public void notify(Event event, Service service) {
        if (subscribeMap == null || subscribeMap.isEmpty() ||
                !subscribeMap.containsKey(service.getServiceName())) {
            return;
        }


        Set<Channel> channels = subscribeMap.get(service.getServiceName());
        RpcMessageType type = null;
        switch (event) {
            case SERVICE_ONLINE:
                type = RpcMessageType.SERVICE_ON_LINE;
                break;
            case SERVICE_OFFLINE:
                type = RpcMessageType.SERVICE_OFF_LINE;
                break;
            default:
                log.info("No such event!");
                return;
        }


        Service s = new Service(service);

        RpcMessage message = RpcMessageBuilder.createSimpleMessage(type, s);

        for (Channel channel : subscribeMap.get(service.getServiceName())) {
            channel.writeAndFlush(message);
        }
        log.info("{} 事件触发，已通知客户端", event);
    }
}
