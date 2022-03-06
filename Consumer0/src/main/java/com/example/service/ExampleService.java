package com.example.service;


import com.happysnaker.client.proxy.RpcReference;
import com.example.FallbackService;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
@RpcReference(serviceName = "service2022", fallbackClass = FallbackService.class)
public interface ExampleService {
    /**
     * RPC 远程调用
     */
    String hello();
}
