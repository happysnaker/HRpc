package com.happysnaker.server.registry;

import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.server.exception.ServiceRegistryException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
public interface ServerRegistry {

    /**
     * 注册服务
     * @param service 服务类
     */
    void registry(Class service) throws ServiceRegistryException;

    /**
     * 取消注册
     * @param service 服务类
     */
    void unRegistry(Class service) throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * 注册中心回送的信息，告知 ServerRegistry 注册结果
     * @param message
     */
    void complete(RpcMessage message);
}
