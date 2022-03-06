package com.happysnaker.client.rule.impl;

import com.happysnaker.client.config.RpcClientApplication;
import com.happysnaker.common.pojo.Service;

import java.util.*;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
public abstract class AbstractConsistentHashLoadBalanceRule extends AbstractLoadBalancingRule{

    public AbstractConsistentHashLoadBalanceRule() {
        virtualNode = new HashMap<>();
        map = new TreeMap<>();
    }

    protected int getRequestHash() {
        return getLb().getRequest().getConsistentHash();
    }

    /**
     * 物理服务所对应虚拟节点的哈希
     */
    protected Map<Service, List<Integer>> virtualNode;

    /**
     * 一致性哈希表
     */
    protected TreeMap<Integer, Service> map;


    /**
     * 获取哈希环中的下一个服务节点
     * @param requestHash
     * @return
     */
    protected Service getNext(int requestHash) {
        // 获取大于等于此 hash 的下一个服务节点
        Map.Entry<Integer, Service> entry = map.ceilingEntry(requestHash);
        if (entry == null) {
            // 如果为 null，说明没有比他更大的，则从头开始
            return map.firstEntry() == null ? null : map.firstEntry().getValue();
        }
        return entry.getValue();
    }

    /**
     * 新增一个节点
     * @param service
     */
    public void addService(Service service) {
        // 已经存在此节点了
        if (virtualNode.containsKey(service)) {
            return;
        }
        // 将一个物理节点映射到多个虚拟节点中
        List<Integer> vNode = new ArrayList<>();
        int count = 0;
        while (count != RpcClientApplication.virtualNodeNum) {
            int hash = Math.abs(Objects.hash(service, count));
            // 哈希冲突
            if (map.containsKey(hash)) {
                continue;
            }
            vNode.add(hash);
            map.put(hash, service);
            count++;
        }
        virtualNode.put(service, vNode);
    }

    /**
     * 删除一个节点
     * @param service
     */
    public void remove(Service service) {
        List<Integer> vNode = virtualNode.getOrDefault(service, new ArrayList<>());
        for (Integer v : vNode) {
            map.remove(v);
        }
        virtualNode.remove(service);
    }

}
