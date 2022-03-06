package com.example;

import com.happysnaker.server.config.RpcServerApplication;
import com.happysnaker.server.registry.EnableRpcService;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/5
 * @email happysnaker@foxmail.com
 */
@EnableRpcService(registerCenterIp = "localhost", registerCenterPort = 4567)
public class Main {
    public static void main(String[] args) throws Exception {
        RpcServerApplication.start(6789);
    }
}
