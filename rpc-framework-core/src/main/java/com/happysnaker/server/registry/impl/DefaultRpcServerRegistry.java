package com.happysnaker.server.registry.impl;

import com.happysnaker.common.builder.RpcMessageBuilder;
import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.Service;
import com.happysnaker.common.pojo.ServiceHealthStatus;
import com.happysnaker.server.config.RpcServerApplication;
import com.happysnaker.server.exception.ServiceRegistryException;
import com.happysnaker.server.handler.RpcServerHandler;
import com.happysnaker.server.handler.ServerRegisterHandler;
import com.happysnaker.server.registry.RpcService;
import com.happysnaker.server.registry.ServerRegistry;
import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 服务提供方注册服务的接口
 *
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
public class DefaultRpcServerRegistry implements ServerRegistry {
    /**
     * 存放还未完成的消息
     */
    static Map<Long, CompletableFuture> map = new ConcurrentHashMap<>();
    /**
     * 此实例
     */
    static ServerRegistry serverRegistry;
    /**
     * 用以发送消息的处理器
     */
    static ServerRegisterHandler handler = ServerRegisterHandler.getInstance();
    /**
     * 存放已注册的服务
     */
    static Map<Class, Service> registeredService = new ConcurrentHashMap<>();

    static Integer lock = new Integer(0);

    public static ServerRegistry getInstance() {
        if (serverRegistry == null) {
            synchronized (DefaultRpcServerRegistry.class) {
                if (serverRegistry == null) {
                    serverRegistry = new DefaultRpcServerRegistry();
                    return serverRegistry;
                }
            }
        }
        return serverRegistry;
    }

    /**
     * 此类全局唯一
     */
    private DefaultRpcServerRegistry() {

    }

    /**
     * 生成服务，以著启动类的 IP 端口 为地址
     * @param c
     * @return
     */
    private Service makeService(Class c) {
        RpcService rpcService = (RpcService) c.getAnnotation(RpcService.class);
        String serviceName = rpcService.serviceName();
        int group = rpcService.group();
        String ip = RpcServerApplication.ip;
        int port = RpcServerApplication.port;
        return new Service(serviceName, ip, port, group);
    }

    @Override
    public void registry(Class c) throws ServiceRegistryException {
        System.out.println("注册:" + c.getName());
        if (!c.isAnnotationPresent(RpcService.class)) {
            // 无法注册此服务
            throw new ServiceRegistryException("Can not register the service, beacuse the not have annotation: RpcService");
        }
        Service service = makeService(c);
        RpcMessage msg = RpcMessageBuilder.createSimpleMessage(RpcMessageType.SERVICE_REGISTRATION, service);

        try {
            // 发布注册消息
            handler.sendMessage(msg);

            CompletableFuture<RpcMessage> future = new CompletableFuture();

            // 将此消息注册至等待列表
            map.put(msg.getId(), future);


            // 堵塞等待注册中心的答复
            RpcMessage message = future.get(10000, TimeUnit.MILLISECONDS);
            // 注册中心回报错误
            if (message.getType().equals(RpcMessageType.ERROR)) {
                throw new RuntimeException((String) message.getBody());
            }
            // 将等待消息 ID 移除
            map.remove(message.getId());
            // 将注册中心为此 service 分配的唯一 ID 赋予 service
            service.setInstanceId((Long) message.getBody());
        } catch (InterruptedException e) {
            throw new ServiceRegistryException(e);
        } catch (ExecutionException e) {
            throw new ServiceRegistryException(e);
        } catch (TimeoutException e) {
            throw new ServiceRegistryException(e);
        }
        synchronized (lock) {
            // 成功注册
            registeredService.put(c, service);
            if (registeredService.size() == 1) {
                // 当服务数量由 0 变至 1 时，说明有服务启动，不停的发送心跳检测包
                // 我们总是复用已连接的 channel，channel 失效时应该尝试重新连接
                ping(handler.getChannel(), RpcServerApplication.heartbeatRate);
            }
        }
    }

    /**
     * 向此 channel 发起心跳包
     *
     * @param channel 目标 channel
     * @param second  速率，单位 s
     */
    private void ping(Channel channel, long second) {
        ScheduledFuture<?> future = channel.eventLoop().schedule(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                // 当通道还存活并且注册的服务数目不为 0 时，此时发送心跳包
                if (channel.isActive() && channel.isOpen()
                        && registeredService.size() > 0) {
                    ServiceHealthStatus status = new ServiceHealthStatus(RpcServerHandler.getConnectNum(), true);
                    // 发送心跳包
                    System.out.println("发送心跳包！");
                    handler.sendMessage(RpcMessageBuilder.createSimpleMessage(
                            RpcMessageType.HEARTBEAT_DETECTION, status)
                    );
                } else {
                    throw new RuntimeException();
                }
            }
        }, second, TimeUnit.MILLISECONDS);

        future.addListener(new GenericFutureListener() {
            @Override
            public void operationComplete(io.netty.util.concurrent.Future future) {
                if (future.isSuccess()) {
                    ping(channel, second);
                }
            }
        });
    }

    @Override
    public void unRegistry(Class c) throws ExecutionException, InterruptedException, TimeoutException {
        // 获取对应实例
        Service service = registeredService.get(c);
        if (service == null) {
            return;
        }
        RpcMessage msg = RpcMessageBuilder.createSimpleMessage(RpcMessageType.SERVICE_UN_REGISTRATION, service);
        try {
            handler.sendMessage(msg);
            CompletableFuture<RpcMessage> future = new CompletableFuture();
            // 将此消息注册至等待列表
            map.put(msg.getId(), future);
            // 堵塞等待注册中心的答复
            RpcMessage message = future.get(5000, TimeUnit.MILLISECONDS);
            // 注册中心回报错误，抛出异常
            if (message.getType().equals(RpcMessageType.ERROR)) {
                throw new RuntimeException((String) message.getBody());
            }
            // 将等待消息 ID 移除
            map.remove(message.getId());
        } catch (Exception e) {
            throw e;
        }
        // 移除服务成功，从列表中删除
        map.remove(c);
        return;
    }

    @Override
    public void complete(RpcMessage message) {
        long id = message.getId();
        CompletableFuture future = map.get(id);
        if (future != null) {
            future.complete(message);
        }
    }
}


