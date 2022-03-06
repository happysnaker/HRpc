package com.example.service.impl;

import com.example.service.DogService;
import com.happysnaker.server.registry.RpcService;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
@RpcService(serviceName = "dog", group = 0)
public class ErHaService implements DogService {
    @Override
    public String say() {
        return "我是二哈！";
    }
}
