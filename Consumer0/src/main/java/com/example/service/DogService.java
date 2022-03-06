package com.example.service;

import com.happysnaker.client.proxy.RpcReference;

/**
 * 模拟一个接口多个服务类的情况
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
@RpcReference(serviceName = "dog")
public interface DogService {
    /**
     * 狗叫
     * @return
     */
    String say();
}
