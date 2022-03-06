package com.happysnaker.client.registry;

import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import com.happysnaker.common.pojo.RpcMessage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
public interface ClientRegistry {
    /**
     * 消费者发现服务，注册中心将返回消费者感兴趣的服务，此方法应该将返回的服务注册到服务中心列表中
     *
     * @param service serviceName 和 className 字段指示了消费者感兴趣的服务
     */
    void lookup(String service) throws InterruptedException, TimeoutException, CanNotUpdateServiceListException, ExecutionException;

    /**
     * 消费者订阅其感兴趣的服务，此后一旦服务列表状态更新，注册中心都必须告知消费者
     *
     * @param service 消费者感兴趣的服务
     */
    void subscribe(String service);


    /**
     * 消费者取消订阅
     *
     * @param service 消费者感兴趣的服务
     */
    void unSubscribe(String service);

    /**
     * 注册中心回复时调用此方法
     *
     * @param message
     */
    void complete(RpcMessage message);

    /**
     * 调用
     * {@link com.happysnaker.client.registry.ClientRegistry#lookup(String)} 和
     * {@link com.happysnaker.client.registry.ClientRegistry#subscribe(String)}
     * 的快捷方法
     *
     * @param service
     * @return
     */
    default void lookupAndSubscribe(String service) throws TimeoutException, CanNotUpdateServiceListException, ExecutionException, InterruptedException {
        try {
            lookup(service);
        } catch (InterruptedException e) {
            throw e;
        } catch (TimeoutException e) {
            throw e;
        } catch (CanNotUpdateServiceListException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e;
        }
        subscribe(service);
    }

}
