package com.example;

import com.example.service.ExampleService;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/5
 * @email happysnaker@foxmail.com
 */
public class FallbackService implements ExampleService {
    @Override
    public String hello() {
        return "服务降级";
    }
}
