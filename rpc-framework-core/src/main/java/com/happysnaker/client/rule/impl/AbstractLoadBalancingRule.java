package com.happysnaker.client.rule.impl;

import com.happysnaker.client.rule.LoadBalance;
import com.happysnaker.client.rule.LoadBalanceRule;
import com.happysnaker.common.pojo.Service;

import java.security.NoSuchProviderException;
import java.util.List;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public abstract class AbstractLoadBalancingRule implements LoadBalanceRule {

    private LoadBalance lb;

    public void setLb(LoadBalance lb) {
        this.lb = lb;
    }

    public LoadBalance getLb() {
        return lb;
    }

    /**
     * 获取服务列表，子类可调用此方法获取服务列表，服务列表已提前注入
     * @return
     */
    protected List<Service> getServers() {
        return lb.getServices();
    }

    /**
     * 负载均衡，选取服务
     * @return
     */
    @Override
    public abstract Service select() throws NoSuchProviderException;
}
