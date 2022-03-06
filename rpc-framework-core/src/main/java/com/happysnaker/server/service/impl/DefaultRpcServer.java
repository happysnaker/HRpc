package com.happysnaker.server.service.impl;

import com.happysnaker.common.exception.RpcRuntimeException;
import com.happysnaker.common.pojo.RpcRequest;
import com.happysnaker.common.pojo.RpcResponse;
import com.happysnaker.server.service.AbstractRpcServer;

import java.lang.reflect.Method;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public class DefaultRpcServer extends AbstractRpcServer {
    @Override
    public RpcResponse doParseRpcRequest(RpcRequest request) {
        RpcResponse response = new RpcResponse();
        response.setId(request.getId());
        try {
            String className = request.getClassName();
            int group = request.getGroup();
            Class aClass = getServiceClass(className, group);

            if (aClass == null) {
                throw new RpcRuntimeException("Can not find class " + aClass);
            }
            String methodName = request.getMethodName();
            Object[] parameters = request.getParameters();
            Object invoke = null;
            Method method = null;
            if (parameters == null) {
                // 无参函数
                method = aClass.getMethod(methodName);
                invoke = method.invoke(getServiceInstance(className, group));
            } else {
                Class[] types = new Class[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    types[i] = parameters[i].getClass();
                }
                method = aClass.getMethod(methodName, types);
                invoke = method.invoke(getServiceInstance(className, group), parameters);
            }


            response.setResultType(method.getReturnType());
            response.setResult(invoke);
        } catch (Exception e) {
            String em = e.getMessage();
            System.out.println("em = " + em);
            if (em == null) {
                em = e.getCause().toString();
            }
            response.setExceptionMessage(em);
        }
        return response;
    }
}
