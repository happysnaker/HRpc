package com.happysnaker.client.factory;

import com.happysnaker.client.proxy.RpcProxy;
import com.happysnaker.client.proxy.RpcReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 生成 RPC 代理类的工厂
 * @author Happysnaker
 * @description
 * @date 2022/3/5
 * @email happysnaker@foxmail.com
 */
public class RpcProxyFactory {
    static InvocationHandler handler = new RpcProxy();

    public static<T> T getInstance(Class<T> clazz) throws ClassNotFoundException {
        if (!clazz.isAnnotationPresent(RpcReference.class)) {
            throw new ClassNotFoundException(clazz + " do not have a RpcReference annotation");
        }
        if (!clazz.isInterface()) {
            throw new RuntimeException("RpcReference must be marked on interface class");
        }
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

}
