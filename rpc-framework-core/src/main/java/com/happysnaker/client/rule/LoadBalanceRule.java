package com.happysnaker.client.rule;

import com.happysnaker.client.rule.impl.AbstractLoadBalancingRule;
import com.happysnaker.common.pojo.Service;

import java.security.NoSuchProviderException;

/**
 * 负载均衡规则，子类应该继承 {@link AbstractLoadBalancingRule} 而不是直接实现此接口
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public interface LoadBalanceRule {
    /**
     * 选取服务，负载均衡
     * @return 选取的服务
     */
    Service select() throws NoSuchProviderException;
}
