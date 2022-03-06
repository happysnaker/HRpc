package com.happysnaker.common.config;

import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import com.happysnaker.common.pojo.Service;
import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此类用以保存服务列表，注册中心和服务消费者共用的管理类
 *
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
@Slf4j
public class ServiceListManager {
    /**
     * 中央服务列表，key 是服务名，val 服务实例
     */
    Map<String, Set<Service>> serviceMap = new ConcurrentHashMap<>();

    /**
     * 此类全局唯一
     */
    private ServiceListManager(){}

    static class SingletonProvider {
        static {
            serviceListManager = new ServiceListManager();
        }

        static ServiceListManager serviceListManager;

        public ServiceListManager getInstance() {
            return serviceListManager;
        }
    }


    public static ServiceListManager getInstance() {
        return SingletonProvider.serviceListManager;
    }

    /**
     * 向服务列表中添加一项服务实例
     *
     * @param service
     * @throws CanNotUpdateServiceListException
     * @return 一旦成功插入，返回 true；失败或列表中已存在都会返回 false
     */
    public boolean addServiceInstance(Service service) throws CanNotUpdateServiceListException {
        if (service == null) {
            throw new CanNotUpdateServiceListException("The service is null");
        }
        String serviceName = service.getServiceName();
        if (StringUtil.isNullOrEmpty(serviceName)) {
            throw new CanNotUpdateServiceListException("The service name is null or empty");
        }
        serviceMap.putIfAbsent(serviceName, new ConcurrentSet<>());
        return serviceMap.get(serviceName).add(service);
    }


    /**
     * 批量插入
     * @param services
     * @throws CanNotUpdateServiceListException
     */
    public void addAll(List<Service> services) throws CanNotUpdateServiceListException {
        for (Service service : services) {
            addServiceInstance(service);
        }
    }

    /**
     * 向服务列表中移除一项服务实例
     * @return 一旦成功删除，返回 true；失败或列表中不存在此项都会返回 false
     * @param service
     * @throws CanNotUpdateServiceListException
     */
    public boolean removeServiceInstance(Service service) throws CanNotUpdateServiceListException {
        if (service == null) {
            throw new CanNotUpdateServiceListException("The service is null");
        }
        String serviceName = service.getServiceName();
        if (StringUtil.isNullOrEmpty(serviceName)) {
            throw new CanNotUpdateServiceListException("The service name is null or empty");
        }

        if (serviceMap.containsKey(serviceName)) {
            return serviceMap.get(serviceName).remove(service);
        }
        return false;
    }

    /**
     * 查询服务列表是否包含一项服务
     *
     * @param serviceName 服务名
     * @return
     */
    public boolean containsService(String serviceName) {
        return serviceMap.containsKey(serviceName);
    }


    /**
     * 查询服务列表是否包含此服务实例
     *
     * @param service 服务实例
     * @return
     */
    public boolean containsService(Service service) {
        return containsService(service.getServiceName()) && getServiceSet(service.getServiceName()).contains(service);
    }

    /**
     * 获取服务实例集合
     *
     * @param serviceName
     * @return
     */
    public Set<Service> getServiceSet(String serviceName) {
        if (serviceMap.containsKey(serviceName)) {
            return serviceMap.get(serviceName);
        }
        return null;
    }


    /**
     * 获取服务实例列表
     *
     * @param serviceName
     * @return 如果查询不到则返回 null
     */
    public List<Service> getServiceList(String serviceName) {
        Set<Service> set = getServiceSet(serviceName);
        if (set != null) {
            return new ArrayList<>(set);
        }
        return null;
    }




    /**
     * 获取所有的服务实例
     * @return 此方法不会返回 null，如果没有任何服务，此方法将返回一个空的列表
     */
    public List<Service> getAllServices() {
        List<Service> ans = new ArrayList<>();
        for (String key : serviceMap.keySet()) {
            ans.addAll(getServiceList(key));
        }
        return ans;
    }


    public void logServiceList() {
        StringBuffer sb = new StringBuffer("当前服务列表：\n");
        for (Service service : getAllServices()) {
            sb.append("- " + service.toString() + "\n");
        }
        log.info(sb.toString());
    }

}
