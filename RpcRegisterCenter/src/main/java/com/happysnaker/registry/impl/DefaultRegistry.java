package com.happysnaker.registry.impl;

import com.happysnaker.common.builder.RpcMessageBuilder;
import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.Service;
import com.happysnaker.registry.AbstractRegistry;
import com.happysnaker.registry.Registry;
import com.happysnaker.service.consumer.ConsumerService;
import com.happysnaker.service.consumer.ConsumerWatcher;
import com.happysnaker.service.consumer.impl.SimpleConsumerService;
import com.happysnaker.service.provider.ProviderService;
import com.happysnaker.service.provider.impl.SimpleProviderService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
@Slf4j
public class DefaultRegistry extends AbstractRegistry {
    static ConsumerService consumerService = new SimpleConsumerService();
    static ProviderService providerService = new SimpleProviderService();
    static volatile Registry registry;

    public static Registry getInstance() {
        if (registry == null) {
            synchronized (DefaultRegistry.class) {
                if (registry == null) {
                    registry = new DefaultRegistry();
                    return registry;
                }
            }
        }
        return registry;
    }

    public long getPipelineId() {
        return (UUID.randomUUID().hashCode() << 32) | UUID.randomUUID().hashCode();
    }

    @Override
    public void register(RpcMessage message, Channel channel) throws CanNotUpdateServiceListException {
        Service service = (Service) message.getBody();

        if (providerService.register(service, channel)) {
            // 一旦成功，为此服务分配一个唯一的 ID
            // 如果没成功，说明此服务已经被保存了
            service.setInstanceId(getPipelineId());

            log.info("服务方 {} 注册服务：{}", channel, service);
            serviceListManager.logServiceList();

            consumerService.notify(ConsumerWatcher.Event.SERVICE_ONLINE, service);
        }
        // 发送一条简单的成功通知消息，消息体为注册中心为此服务分配的唯一实例 ID
        RpcMessage message1 = RpcMessageBuilder.createSuccessMessage(service.getInstanceId(), message.getId());
        channel.writeAndFlush(message1);

    }

    @Override
    public void unRegister(RpcMessage message, Channel channel) throws CanNotUpdateServiceListException {
        Service service = (Service) message.getBody();
        if (providerService.unRegister(service, channel)) {
            consumerService.notify(ConsumerWatcher.Event.SERVICE_OFFLINE, service);
            log.info("服务 {} 主动下线", service);
            serviceListManager.logServiceList();
        }
        // 发送一条简单的成功通知消息
        RpcMessage message1 = RpcMessageBuilder.createSuccessMessage(true, message.getId());
        channel.writeAndFlush(message1);
    }

    @Override
    public void lookup(RpcMessage message, Channel channel) {
        String service = (String) message.getBody();
        List<Service> serviceList = consumerService.lookup(service, channel);
        log.info("客户 {} 查询服务 {}，此服务存在 {} 项实例", channel, service, serviceList.size());

        // 将服务列表发送回客户端，以消息 ID 为唯一标识，消息类型为服务发现
        RpcMessage message1 = RpcMessageBuilder.createSimpleMessage(
                message.getId(), RpcMessageType.SERVICE_DISCOVERY, serviceList);
        channel.writeAndFlush(message1);
    }



    @Override
    public void subscribe(RpcMessage message, Channel channel) {
        String service = (String) message.getBody();
        consumerService.subscribe(service, channel);
        log.info("客户 {} 订阅服务 {}", channel, service);

        // 发送一条简单的成功通知消息
        RpcMessage message1 = RpcMessageBuilder.createSuccessMessage(true, message.getId());
        channel.writeAndFlush(message1);
    }

    @Override
    public void unSubscribe(RpcMessage message, Channel channel) {
        String service = (String) message.getBody();
        consumerService.unSubscribe(service, channel);
        log.info("客户 {} 取消订阅服务 {}", channel, service);

        // 发送一条简单的成功通知消息
        RpcMessage message1 = RpcMessageBuilder.createSuccessMessage(true, message.getId());
        channel.writeAndFlush(message1);
    }

    @Override
    public void notify(Event event, Service service) {
        consumerService.notify(event, service);
    }
}
