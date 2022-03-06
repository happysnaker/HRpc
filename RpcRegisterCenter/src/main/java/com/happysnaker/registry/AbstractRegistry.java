package com.happysnaker.registry;

import com.happysnaker.common.config.ServiceListManager;

/**
 * 抽象的注册中心
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public abstract class AbstractRegistry implements Registry {
    /**
     * 服务列表中心，供子类调用，不允许被修改
     */
    protected ServiceListManager serviceListManager = ServiceListManager.getInstance();
}
