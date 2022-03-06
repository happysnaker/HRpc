package com.happysnaker.service.provider.impl;

import com.happysnaker.common.pojo.Service;
import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import com.happysnaker.service.provider.ProviderService;
import io.netty.channel.Channel;

/**
 * 默认的服务实现者
 *
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public class SimpleProviderService implements ProviderService {
    @Override
    public boolean register(Service service, Channel channel) throws CanNotUpdateServiceListException {
        try {
            return serviceListManager.addServiceInstance(service);
        } catch (CanNotUpdateServiceListException e) {
            throw e;
        } catch (Exception e) {
            throw new CanNotUpdateServiceListException(e);
        }
    }

    @Override
    public boolean unRegister(Service service, Channel channel) throws CanNotUpdateServiceListException {
        try {
            return serviceListManager.removeServiceInstance(service);
        } catch (CanNotUpdateServiceListException e) {
            throw e;
        } catch (Exception e) {
            throw new CanNotUpdateServiceListException(e);
        }
    }
}
