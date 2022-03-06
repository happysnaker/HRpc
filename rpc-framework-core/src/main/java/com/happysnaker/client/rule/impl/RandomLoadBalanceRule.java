package com.happysnaker.client.rule.impl;

import com.happysnaker.common.pojo.Service;

import java.security.NoSuchProviderException;
import java.util.List;
import java.util.UUID;

/**
 * 随机负载均衡
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
public class RandomLoadBalanceRule extends AbstractLoadBalancingRule {
    @Override
    public Service select() throws NoSuchProviderException {
        List<Service> servers = getServers();
        if (servers == null || servers.isEmpty()) {
            throw new NoSuchProviderException("没有查询到相关服务: " + getLb().getServiceName() + ":" + getLb().getGroup());
        }
        int len = servers.size();
        return servers.get(UUID.randomUUID().hashCode() % len);
    }
}
