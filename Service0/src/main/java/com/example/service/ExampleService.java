package com.example.service;

import com.happysnaker.server.registry.RpcService;

import java.util.UUID;

/**
 * 模拟直接一个服务的情况
 * @author Happysnaker
 * @description
 * @date 2022/3/5
 * @email happysnaker@foxmail.com
 */
@RpcService(serviceName = "service2022")
public class ExampleService {
    public String hello() {
        return "Service0 Hello World! -- " + UUID.randomUUID();
    }
}
