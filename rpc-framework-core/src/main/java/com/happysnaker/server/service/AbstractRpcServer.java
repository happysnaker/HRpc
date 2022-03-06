package com.happysnaker.server.service;

import com.happysnaker.common.utils.PairUtil;
import com.happysnaker.server.config.RpcServerApplication;
import com.happysnaker.server.registry.RpcService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public abstract class AbstractRpcServer implements RpcServerService {
    /**
     * <p>服务 Class 缓存，key 是全限定类名与组号，val 是提供服务的类信息与服务实例</p>
     * <p>你不可以使用 {@link java.lang.Class#forName(String)} 以搜索服务类，因为全限定类名不仅仅是被 {@link com.happysnaker.server.registry.RpcService} 所标记的类，也可以是被标记类实现的接口</p>
     *
     * @see com.happysnaker.server.registry.RpcService
     */
    private static Map<PairUtil<String, Integer>, PairUtil<Class, Object>> serviceMap;


    static {
        try {
            serviceMap = new ConcurrentHashMap<>();
            for (Class aClass : RpcServerApplication.serviceClasses) {
                RpcService annotation = (RpcService) aClass.getAnnotation(RpcService.class);
                int group = annotation.group();
                Object obj = aClass.getConstructor().newInstance();
                serviceMap.put(PairUtil.of(aClass.getName(), group), PairUtil.of(aClass, obj));

                // 将此类实现的接口一并放入缓存
                for (Class anInterface : aClass.getInterfaces()) {
                    PairUtil pair = PairUtil.of(anInterface.getName(), group);
                    serviceMap.put(pair, PairUtil.of(aClass, obj));
                }
            }
            System.out.println("serviceMap = " + serviceMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回提供服务的具体的类信息
     *
     * @param className
     * @param group
     * @return
     * @throws
     */
    protected Class getServiceClass(String className, int group) {
        PairUtil<Class, Object> pairUtil = serviceMap.get(PairUtil.of(className, group));
        return pairUtil == null ? null : pairUtil.getFirst();
    }


    /**
     * 返回提供服务的具体的类实例
     *
     * @param className
     * @param group
     * @return
     * @throws NullPointerException 如果搜索不到抛出
     */
    protected Object getServiceInstance(String className, int group) throws NullPointerException {
        return serviceMap.get(PairUtil.of(className, group)).getSecond();
    }
}
