package com.happysnaker.client.rule.impl;

import com.happysnaker.common.pojo.Service;
import com.happysnaker.common.pojo.ServiceHealthStatus;

import java.security.NoSuchProviderException;
import java.util.List;

/**
 * 最少连接优先
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
public class LeastConnectedLoadBalanceRule extends AbstractLoadBalancingRule {
    @Override
    public Service select() throws NoSuchProviderException {
        List<Service> servers = getServers();
        if (servers == null || servers.isEmpty()) {
            throw new NoSuchProviderException("没有查询到相关服务: " + getLb().getServiceName() + ":" + getLb().getGroup());
        }
        Service ans = null;
        int connect = Integer.MAX_VALUE;
        for (Service server : servers) {
            if (server.getStatus() != null && server.getStatus() instanceof ServiceHealthStatus) {
                ServiceHealthStatus status = (ServiceHealthStatus) server.getStatus();
                if (ans == null || status.getConnectNum() < connect) {
                    connect = status.getConnectNum();
                    ans = server;
                }
            }
        }
        return ans;
    }
}
