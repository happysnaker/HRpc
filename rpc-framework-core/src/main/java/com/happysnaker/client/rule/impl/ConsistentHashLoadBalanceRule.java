package com.happysnaker.client.rule.impl;

import com.happysnaker.common.pojo.Service;

import java.security.NoSuchProviderException;

/**
 * 一致性哈希算法
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
public class ConsistentHashLoadBalanceRule extends AbstractConsistentHashLoadBalanceRule {
    @Override
    public Service select() throws NoSuchProviderException {
        if (map == null || map.isEmpty()) {
            for (Service server : getServers()) {
                addService(server);
            }
        }
//        remove();
        Service next = getNext(getLb().getRequest().getConsistentHash());
        return next;
    }
}
