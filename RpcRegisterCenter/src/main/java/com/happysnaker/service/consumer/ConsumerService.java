package com.happysnaker.service.consumer;

import com.happysnaker.common.pojo.Service;
import com.happysnaker.common.config.ServiceListManager;
import io.netty.channel.Channel;

import java.util.List;

/**
 * 注册中心为<b>服务消费者<b/>提供的具体服务，此服务只对注册中心暴露，由注册中心调用此服务，对外暴露的接口为 {@link com.happysnaker.registry.Registry}
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public interface ConsumerService extends  ConsumerWatcher {

    /**
     * 服务列表中心，供子类调用，不允许被修改
     */
    ServiceListManager serviceListManager = ServiceListManager.getInstance();

    /**
     * 消费者发现服务，注册中心将返回消费者感兴趣的服务
     * @param service serviceName 和 className 字段指示了消费者感兴趣的服务
     * @param channel 与客户端的连接
     * @return
     */
    List<Service> lookup(String service, Channel channel);

    /**
     * 消费者订阅其感兴趣的服务，此后一旦服务列表状态更新，注册中心都必须告知消费者
     * <p>此方法应该要能够过滤掉客户端重复的订阅信息</p>
     * @param service 消费者感兴趣的服务
     * @param channel 与消费者通信的通道
     */
    void subscribe(String service, Channel channel);


    /**
     * 消费者取消订阅
     * @param service 消费者感兴趣的服务
     * @param channel 与消费者通信的通道
     */
    void unSubscribe(String service, Channel channel);


    /**
     * 用以通知客户端服务上线或下线
     * @param event 事件类型
     * @param service 具体服务
     */
    @Override
    void notify(Event event, Service service);

}
