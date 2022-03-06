package com.happysnaker.service.provider;

import com.happysnaker.common.pojo.Service;
import com.happysnaker.common.config.ServiceListManager;
import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import io.netty.channel.Channel;

/**
 * 注册中心为服务提供者提供的具体服务，此服务只对注册中心暴露，由注册中心调用此服务，对外暴露的接口为 {@link com.happysnaker.registry.Registry}
 *
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public interface ProviderService {
    /**
     * 服务列表中心，供子类调用，不允许被修改
     */
    ServiceListManager serviceListManager = ServiceListManager.getInstance();

    /**
     * 服务方请求注册中心注册请求，注册中心将处理此请求
     * @param service 具体的服务
     * @throws CanNotUpdateServiceListException
     * @return 如果已经含有此服务，返回 false
     */
    boolean register(Service service, Channel channel) throws CanNotUpdateServiceListException;


    /**
     * 服务方向注册中心发送下线通知，注册中心将此服务移除服务列表
     * @param service 具体的服务
     *                @throws CanNotUpdateServiceListException
     * @return 如果先前就不存在此服务，返回 false
     */
    boolean unRegister(Service service, Channel channel) throws CanNotUpdateServiceListException;

}
