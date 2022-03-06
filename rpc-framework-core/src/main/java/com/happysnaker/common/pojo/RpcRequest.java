package com.happysnaker.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户端发起的 RPC 请求
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RpcRequest {
    /**
     * 全限定类名，此类名可以标识一个类或者接口
     */
    private String className;

    /**
     * 请求方法
     */
    private String methodName;

    /**
     * 请求参数
     */
    private Object[] parameters;

    /**
     * 请求的服务名
     */
    private String serviceName;

    /**
     * 服务组号
     */
    private int group;

    /**
     * 标识请求的唯一 ID
     */
    private long id;

    /**
     * 标识此请求的一致性哈希，此字段当负载均衡选择为一致性哈希时需要填写，默认的值为客户端 IP 所产生的哈希值
     */
    private int consistentHash;
}
