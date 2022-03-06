package com.happysnaker;

import com.happysnaker.config.RpcRegisterCenterStarter;

/**
 * 服务注册中心的主启动类
 * @author Happysnaker
 * @description
 * @date 2022/2/28
 * @email happysnaker@foxmail.com
 */
public class Main {
    public static void main(String[] args) throws Exception {
        int port = 4567;
        long readerIdlTile = 6L;
        if (args != null && args.length >= 1) {
            port = Integer.parseInt(args[0]);
            if (args.length >= 2) {
                readerIdlTile = Long.parseLong(args[1]);
            }
        }
        RpcRegisterCenterStarter.start("localhost", port, readerIdlTile);
    }
}

