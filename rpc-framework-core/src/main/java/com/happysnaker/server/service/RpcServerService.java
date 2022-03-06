package com.happysnaker.server.service;

import com.happysnaker.common.pojo.RpcRequest;
import com.happysnaker.common.pojo.RpcResponse;

/**
 *
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public interface RpcServerService {
    /**
     * 解析 RPC 远程调用请求，并返回 RPC 应答
     * @param request
     * @return
     */
    RpcResponse doParseRpcRequest(RpcRequest request);
}
