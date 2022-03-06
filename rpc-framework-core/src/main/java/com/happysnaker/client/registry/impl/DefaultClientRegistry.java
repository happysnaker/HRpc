package com.happysnaker.client.registry.impl;

import com.happysnaker.client.config.RpcClientApplication;
import com.happysnaker.client.factory.LoadBalanceRuleFactory;
import com.happysnaker.client.handler.ClientRegisterHandler;
import com.happysnaker.client.registry.ClientRegistry;
import com.happysnaker.client.rule.LoadBalanceRule;
import com.happysnaker.client.rule.impl.AbstractConsistentHashLoadBalanceRule;
import com.happysnaker.common.builder.RpcMessageBuilder;
import com.happysnaker.common.config.ServiceListManager;
import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.Service;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/3
 * @email happysnaker@foxmail.com
 */
public class DefaultClientRegistry implements ClientRegistry {
    /**
     * 存放还未完成的消息
     */
    static Map<Long, CompletableFuture> map = new ConcurrentHashMap<>();
    /**
     * 用以发送消息的处理器
     */
    static ClientRegisterHandler handler = ClientRegisterHandler.getInstance();
    /**
     * 存放服务列表
     */
    static ServiceListManager serviceListManager = ServiceListManager.getInstance();

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    static volatile DefaultClientRegistry registry;

    /**
     * 全局唯一
     *
     * @return
     */
    public static DefaultClientRegistry getInstance() {
        if (registry == null) {
            synchronized (DefaultClientRegistry.class) {
                if (registry == null) {
                    return registry = new DefaultClientRegistry();
                }
            }
        }
        return registry;
    }

    /**
     * 将消息发送给注册中心后，堵塞等待注册中心的答复，此方法默认 5 秒的超时时长
     *
     * @param msgId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    private RpcMessage waitForResponse(long msgId) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture future = new CompletableFuture();
        map.put(msgId, future);
        return (RpcMessage) future.get(RpcClientApplication.timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void lookup(String service) throws InterruptedException, TimeoutException, CanNotUpdateServiceListException, ExecutionException {
        RpcMessage message = RpcMessageBuilder.createSimpleMessage(RpcMessageType.SERVICE_DISCOVERY, service);
        try {
            handler.sendMessage(message);
            List<Service> services = (List<Service>) waitForResponse(message.getId()).getBody();
            if (services != null && !services.isEmpty()) {
                // 添加服务
                serviceListManager.addAll(services);
                serviceListManager.logServiceList();

                // 如果使用了一致性哈希规则的话，则我们还需要维护一致性哈希表
                if (getConsistentHashLoadBalanceRule() != null) {
                    for (Service service1 : services) {
                        getConsistentHashLoadBalanceRule().addService(service1);
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw e;
        } catch (CanNotUpdateServiceListException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void subscribe(String service) {
        Channel channel = handler.getChannel();
        /**
         * 虽然客户端发送订阅后注册中心就会自动推送消息，但是我们仍然需要考虑特殊的情况
         * 如果注册中心与客户端断开了连接，而客户端还傻傻地以为注册中心会推送服务
         * 所以，客户端需要类似于心跳包的机制告知检测并告知注册中心自己感兴趣的服务
         * 在此，客户端以 10s 一次的速率定时发送订阅消息，此速率在启动类上通过注解配置
         */
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(new Runnable() {
            @lombok.SneakyThrows
            @Override
            public void run() {
                // 当通道还存活并且注册的服务数目不为 0 时，此时发送心跳包
                if (channel.isActive() && channel.isOpen()) {
                    try {
                        // 发送心跳包
                        System.out.println("发送订阅信息！" + service);
                        doSubscribe(service);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new RuntimeException();
                }
            }
        }, 0L, RpcClientApplication.subscribeRate, TimeUnit.MILLISECONDS);

    }

    public void doSubscribe(String service) throws InterruptedException, ExecutionException, TimeoutException {
        RpcMessage message = RpcMessageBuilder.createSimpleMessage(RpcMessageType.SUBSCRIPTION_SERVICE, service);
        try {
            handler.sendMessage(message);
            RpcMessage response = waitForResponse(message.getId());
            if (response.getType() == RpcMessageType.ERROR) {
                throw new RuntimeException((String) response.getBody());
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e;
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void unSubscribe(String service) {

    }

    @Override
    public void complete(RpcMessage message) {
        try {
            switch (message.getType()) {
                // 服务上线推送，更新服务
                case SERVICE_ON_LINE: {
                    Service service = (Service) message.getBody();
                    System.out.println("服务上线：" + service);
                    serviceListManager.addServiceInstance(service);

                    // 如果使用了一致性哈希规则的话，则我们还需要维护一致性哈希表
                    System.out.println("getConsistentHashLoadBalanceRule() = " + getConsistentHashLoadBalanceRule());
                    if (getConsistentHashLoadBalanceRule() != null) {
                        getConsistentHashLoadBalanceRule().addService(service);
                    }
                    break;
                }
                // 服务下线推送，更新服务
                case SERVICE_OFF_LINE: {
                    Service service = (Service) message.getBody();
                    System.out.println("服务下线：" + service);
                    serviceListManager.removeServiceInstance(service);

                    // 如果使用了一致性哈希规则的话，则我们还需要维护一致性哈希表
                    if (getConsistentHashLoadBalanceRule() != null) {
                        getConsistentHashLoadBalanceRule().remove(service);
                    }
                    break;
                }
                // 其他情况下为注册中心的应答，解除之前发送消息的堵塞
                default: {
                    long id = message.getId();
                    CompletableFuture future = map.get(id);
                    if (future != null) {
                        future.complete(message);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取一致性负载均衡实例，如果为空，返回 NULL
     *
     * @return
     */
    private AbstractConsistentHashLoadBalanceRule getConsistentHashLoadBalanceRule() {
        try {
            LoadBalanceRule rule = LoadBalanceRuleFactory.getRule(null);
            if (rule == null ||
                    !(rule instanceof AbstractConsistentHashLoadBalanceRule)) {
                return null;
            }
            return (AbstractConsistentHashLoadBalanceRule) rule;
        } catch (Exception e) {
            return null;
        }
    }
}
