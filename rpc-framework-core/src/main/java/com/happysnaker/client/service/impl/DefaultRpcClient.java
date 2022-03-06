package com.happysnaker.client.service.impl;

import com.happysnaker.client.handler.RpcClientHandler;
import com.happysnaker.client.service.RpcClientService;
import com.happysnaker.common.builder.RpcMessageBuilder;
import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.RpcRequest;
import com.happysnaker.common.pojo.RpcResponse;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public class DefaultRpcClient implements RpcClientService {
    static RpcClientHandler handler;

    static Map<Long, CompletableFuture<RpcResponse>> map = new ConcurrentHashMap<>();


    private RpcResponse waitForResponse(long id) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<RpcResponse> future = new CompletableFuture();
        map.put(id, future);
        return  future.get(5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public RpcResponse doRpcRequest(RpcRequest request, String ip, int port) throws InterruptedException, TimeoutException {
        RpcMessage message = RpcMessageBuilder.createSimpleMessage(RpcMessageType.RPC_REQUEST, request);
        if (handler == null) {
            handler = RpcClientHandler.getRpcClientHandler();
        }

        try {
            handler.connectAndWrite(ip, port, message);
            return waitForResponse(request.getId());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    @Override
    public void complete(RpcResponse response) {
        CompletableFuture<RpcResponse> future = map.get(response.getId());
        if (future != null) {
            future.complete(response);
        }
    }
}
