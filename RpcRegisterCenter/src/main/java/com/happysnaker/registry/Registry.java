package com.happysnaker.registry;

import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import com.happysnaker.service.consumer.ConsumerWatcher;
import io.netty.channel.Channel;

/**
 * 服务注册的接口，注册中心为客户端提供的服务 API，此接口用于接收到客户端请求时调用
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public interface Registry extends ConsumerWatcher {

    /**
     * 服务方请求注册中心注册请求，注册中心将处理此请求
     * @param message 客户端调用发来的 RPC Message
     * @param channel 与客户端的连接
     * @throws CanNotUpdateServiceListException
     */
    void register(RpcMessage message, Channel channel) throws CanNotUpdateServiceListException;


    /**
     * 服务方向注册中心发送下线通知，注册中心将此服务移除服务列表
     * @param message 客户端调用发来的 RPC Message
     *                 @param channel 与客户端的连接
     *                @throws CanNotUpdateServiceListException
     */
    void unRegister(RpcMessage message, Channel channel) throws CanNotUpdateServiceListException;

    /**
     * 消费者发现服务，注册中心将返回消费者感兴趣的服务
     *  @param message 客户端调用发来的 RPC Message
     * @param channel 与客户端的连接
     */
    void lookup(RpcMessage message, Channel channel);

    /**
     * 消费者订阅其感兴趣的服务，此后一旦服务列表状态更新，注册中心都必须告知消费者
     *  @param message 客户端调用发来的 RPC Message
     * @param channel 与消费者通信的通道
     */
    void subscribe(RpcMessage message, Channel channel);


    /**
     * 消费者取消订阅
     *  @param message 客户端调用发来的 RPC Message
     * @param channel 与消费者通信的通道
     */
    void unSubscribe(RpcMessage message, Channel channel);
}
