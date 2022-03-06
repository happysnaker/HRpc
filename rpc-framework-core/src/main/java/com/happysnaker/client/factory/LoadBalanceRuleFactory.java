package com.happysnaker.client.factory;

import com.happysnaker.client.rule.LoadBalanceRule;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public class LoadBalanceRuleFactory {
    private volatile static LoadBalanceRule rule;

    /**
     * 获取负载均衡规则，此规则全局唯一，一旦通过 {@link #getRule(Class)} 构造后，后续无论如何都将返回相同的实例，不论参数
     * @param c 初始化时指定的生成的类的信息
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static LoadBalanceRule getRule(Class c) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (rule == null) {
            synchronized (LoadBalanceRuleFactory.class) {
                if (rule == null) {
                    rule = (LoadBalanceRule) c.getConstructor().newInstance();
                    return rule;
                }
            }
        }
        return rule;
    }
}
