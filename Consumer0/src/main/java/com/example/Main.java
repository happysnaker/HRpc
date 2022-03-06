package com.example;

import com.example.service.DogService;
import com.happysnaker.client.config.RpcClientApplication;
import com.happysnaker.client.factory.RpcProxyFactory;
import com.happysnaker.client.registry.EnableRpcClient;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/3
 * @email happysnaker@foxmail.com
 */
@EnableRpcClient(registerCenterIp = "localhost", registerCenterPort = 4567)
public class Main {
    static {
        try {
            RpcClientApplication.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        var service = RpcProxyFactory.getInstance(DogService.class);
        for (int i = 0; i < 10; i++) {
            try {
                System.out.println(service.say());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}

