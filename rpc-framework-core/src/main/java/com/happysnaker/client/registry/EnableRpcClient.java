package com.happysnaker.client.registry;

import com.happysnaker.client.proxy.RpcReference;
import com.happysnaker.client.rule.LoadBalanceRule;
import com.happysnaker.client.rule.impl.PollLoadBalanceRule;

import java.lang.annotation.*;

/**
 * 标识客户端开启 PRC 功能，此注解是客户端集中式的配置中心
 * @author happysnakers
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableRpcClient {
    /**
     * 要扫描的包，{@link RpcReference} 应该放在此包下，默认会扫描主类所在的包
     * @return
     */
    String packageScan() default "";

    /**
     * 注册中心的 IP 地址
     * @return
     */
    String registerCenterIp();

    /**
     * 注册中心的端口
     * @return
     */
    int registerCenterPort();

    /**
     * 请求 RPC 失败后允许重试的次数
     * @return
     */
    int numberOfRetry() default 0;

    /**
     * 请求 RPC 时指定的等待超时时间，单位毫秒，默认 5s
     * @return
     */
    long timeout() default 5000;

    /**
     * 客户端订阅服务的速率，单位毫秒，默认 10s
     * @return
     */
    long subscribeRate() default 10 * 1000;

    /**
     * 负载均衡策略，默认为轮询
     * @return
     */
    Class<? extends LoadBalanceRule> rule() default PollLoadBalanceRule.class;

    /**
     * <p>
     *     一台物理节点映射的虚拟节点数
     * </p>
     * <p>
     *     使用一致性哈希负载均衡时需要配置此字段
     * </p>
     * @return
     */
    int virtualNodeNum() default 3;
}
