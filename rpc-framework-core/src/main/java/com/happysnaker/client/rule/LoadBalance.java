package com.happysnaker.client.rule;

import com.happysnaker.common.config.ServiceListManager;
import com.happysnaker.common.pojo.RpcRequest;
import com.happysnaker.common.pojo.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public final class LoadBalance {
    private ServiceListManager serviceListManager = ServiceListManager.getInstance();
    private String serviceName;
    private int group;
    private RpcRequest request;

    public LoadBalance(RpcRequest request) {
        this(request.getServiceName(), request.getGroup());
        this.request = request;
    }

    private LoadBalance(String serviceName, int group) {
        this.serviceName = serviceName;
        this.group = group;
    }

    public RpcRequest getRequest() {
        return request;
    }

    /**
     * 获取服务列表
     * @return
     */
    public List<Service> getServices() {
        List<Service> list = serviceListManager.getServiceList(serviceName);
        // group = -1 时，说明查询所有的服务
        if (group == -1) {
            return list;
        }
        // 否则，查询特定的服务分组
        return list.stream().filter((s)-> s.getGroup() == group).collect(Collectors.toList());
    }

    public ServiceListManager getServiceListManager() {
        return serviceListManager;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getGroup() {
        return group;
    }
}
