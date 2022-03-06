package com.happysnaker.client.service;

import com.happysnaker.common.pojo.RpcRequest;
import com.happysnaker.common.pojo.RpcResponse;

import java.util.concurrent.TimeoutException;

/**
 * RPC 客户端服务
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public interface RpcClientService {


    /**
     * 发起 RPC 远程调用
     * @param request
     * @param ip 服务器地址
     * @param port 服务器端口
     * @return
     */
    RpcResponse doRpcRequest(RpcRequest request, String ip, int port) throws InterruptedException, TimeoutException;

    /**
     * 当收到 RPC 回复时调用此方法
     * @param response
     */
    void complete(RpcResponse response);
}
