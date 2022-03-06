package com.happysnaker.server.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author happysnakers
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableRpcService {
    /**
     * 要扫描的包，RpcService 应该放在此包下，默认会扫描主类所在的包
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
     * 心跳包发送的速率，单位 ms
     * @return
     */
    long heartbeatRate() default 5 * 1000;
}
