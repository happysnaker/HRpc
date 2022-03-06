package com.happysnaker.client.rule.impl;

import com.happysnaker.common.pojo.Service;

import java.security.NoSuchProviderException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public class PollLoadBalanceRule extends AbstractLoadBalancingRule {

    AtomicInteger idx = new AtomicInteger(-1);

    @Override
    public Service select() throws NoSuchProviderException {
        while (true) {
            List<Service> servers = getServers();
            if (servers == null || servers.isEmpty()) {
                throw new NoSuchProviderException("没有查询到相关服务: " + getLb().getServiceName() + ":" + getLb().getGroup());
            }
            int cur = idx.get();
            int next = (cur + 1) % servers.size();
            if (idx.compareAndSet(cur, next)) {
                return servers.get(next);
            }
        }
    }
}
