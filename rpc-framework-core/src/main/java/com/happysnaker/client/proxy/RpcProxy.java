package com.happysnaker.client.proxy;

import com.happysnaker.client.config.RpcClientApplication;
import com.happysnaker.client.factory.LoadBalanceRuleFactory;
import com.happysnaker.client.rule.LoadBalance;
import com.happysnaker.client.rule.LoadBalanceRule;
import com.happysnaker.client.rule.impl.AbstractLoadBalancingRule;
import com.happysnaker.client.rule.impl.ConsistentHashLoadBalanceRule;
import com.happysnaker.client.service.RpcClientService;
import com.happysnaker.client.service.impl.DefaultRpcClient;
import com.happysnaker.common.exception.RpcRuntimeException;
import com.happysnaker.common.pojo.RpcRequest;
import com.happysnaker.common.pojo.RpcResponse;
import com.happysnaker.common.pojo.Service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public class RpcProxy implements InvocationHandler {

    /**
     * 静态字段，全局唯一
     */
    static RpcClientService service = new DefaultRpcClient();

    static LoadBalanceRule rule;

    static {
        try {
            rule = LoadBalanceRuleFactory.getRule(RpcClientApplication.rule);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 此接口即为 RPC 客户端类，在服务端应存在与此接口全限定名相同的类
        Class aClass = getRpcReferenceClass(proxy.getClass());
        if (aClass == null) {
            throw new ClassNotFoundException("未搜索到 RpcReference 注解，请检查注解是否正确标记在接口类上");
        }
        RpcReference reference = (RpcReference) aClass.getAnnotation(RpcReference.class);
        String serviceName = reference.serviceName();
        int group = reference.group();

        RpcRequest request = new RpcRequest();
        request.setServiceName(serviceName);
        request.setGroup(group);
        request.setClassName(aClass.getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setId(UUID.randomUUID().getLeastSignificantBits());

        // 如果选择了一致性哈希的话，则设置请求 HASH
        if (RpcClientApplication.rule.equals(ConsistentHashLoadBalanceRule.class)) {
            int hash = reference.consistentHash() == -1 ?
                    // 默认以客户端 IP 作为请求 hash
                    Math.abs(InetAddress.getLocalHost().getHostAddress().hashCode()) :
                    reference.consistentHash();
            request.setConsistentHash(hash);
        }

        if (!(rule instanceof AbstractLoadBalancingRule)) {
            throw new ClassNotFoundException("Can not found the rule!");
        }

        // 重试次数为 numberOfRetry，那么允许发送 1 + numberOfRetry 次
        for (int i = 0; i < RpcClientApplication.numberOfRetry + 1; i++) {
            try {
                Service service = null;
                // 负载均衡选择服务，必须加锁，因为这里是全局共用一个 rule
                synchronized (rule) {
                    // 注入特定服务信息
                    ((AbstractLoadBalancingRule) rule).setLb(new LoadBalance(request));
                    // 负载均衡，选取服务
                    service = rule.select();
                }
                if (service == null) {
                    throw new ClassNotFoundException("Can not find service for {" + serviceName + ":" + group + "}");
                }
                // 原来的 group 可能是 -1，将选择的服务的 group 设置进去
                request.setGroup(service.getGroup());

                String ip = service.getIp();
                int port = service.getPort();

                // 发起调用，此方法会堵塞直到获取结果或超时
                RpcResponse response = RpcProxy.service.doRpcRequest(request, ip, port);
                if (response.getExceptionMessage() != null) {
                    // 不为 NULL 说明有错误消息，那么就是出错了，抛出异常
                    throw new RpcRuntimeException(response.getExceptionMessage());
                }
                // 都正常那么获取结果
                Object result = response.getResult();

                // 返回类型不一样，还是出错了
                if (!response.getResultType().equals(method.getReturnType())) {
                    throw new RpcRuntimeException(service + " 返回类型与接口类不同");
                }
                return result;
            } catch (RpcRuntimeException e) {
                // 如果抛出的是此异常，说明调用是成功了，抛出的异常是正常运行时抛出的
                // 那么，我们抛出它，并结束循环
                throw e;
            } catch (Exception e) {
                // 如果未达到重试阈值，继续重试
                if (i != RpcClientApplication.numberOfRetry) {
                    continue;
                }
                // 否则应该算是失败了
                // 如果配置了服务降级，那么进入服务降级
                if (!reference.fallbackClass().equals(Class.class)) {
                    Class fallbackClass = reference.fallbackClass();
                    // 这里走单例缓存了
                    return method.invoke(getFallbackObject(fallbackClass.getName(), fallbackClass), args);
                }
                throw new RpcRuntimeException(e);
            }
        }
        return null;
    }

    static Map<String, Object> fallbackObject;

    /**
     * 获取服务降级的单例类
     * @param c 全限定名
     * @param cl 类信息
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Object getFallbackObject(String c, Class cl) throws InstantiationException, IllegalAccessException {
        if (fallbackObject == null) {
            fallbackObject = new ConcurrentHashMap<>();
        }
        if (fallbackObject.get(c) == null) {
            synchronized (RpcProxy.class) {
                if (fallbackObject.get(c) == null) {
                    fallbackObject.put(c, cl.newInstance());
                }
                return fallbackObject.get(c);
            }
        }
        return fallbackObject.get(c);
    }

    /**
     * 获取代理类代理的接口类信息，此接口必须含有 {@link com.happysnaker.client.proxy.RpcReference} 注解
     *
     * @param proxy
     * @return 返回 CLASS 信息，若无，返回 NULL
     */
    private static Class getRpcReferenceClass(Class proxy) {
        if (proxy == null) {
            return null;
        }
        if (proxy.isAnnotationPresent(RpcReference.class)) {
            return proxy;
        }
        Class RpcReference = null;
        for (Class aClass : proxy.getInterfaces()) {
            if ((RpcReference = getRpcReferenceClass(aClass)) != null) {
                return RpcReference;
            }
        }
        return getRpcReferenceClass(proxy.getSuperclass());
    }
}
