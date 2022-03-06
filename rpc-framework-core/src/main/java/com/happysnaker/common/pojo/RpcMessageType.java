package com.happysnaker.common.pojo;

/**
 * @author happysnakers
 */
public enum RpcMessageType {

    /**
     * 服务提供者发送注册服务的消息类型
     */
    SERVICE_REGISTRATION,

    /**
     * 服务提供者发送下线消息
     */
    SERVICE_UN_REGISTRATION,

    /**
     * 注册中心收到心跳检测包的消息
     */
    HEARTBEAT_DETECTION,

    /**
     * 消费者搜寻服务，即服务发现，此类型将用于请求和应答
     */
    SERVICE_DISCOVERY,

    /**
     * 消费者订阅服务信息
     */
    SUBSCRIPTION_SERVICE,

    /**
     * 消费者取消订阅服务信息
     */
    UN_SUBSCRIPTION_SERVICE,

    /**
     * 注册中心向消费者推送服务下线消息
     */
    SERVICE_OFF_LINE,

    /**
     * 注册中心向消费者推送服务上线消息
     */
    SERVICE_ON_LINE,

    /**
     * RPC 请求消息
     */
    RPC_REQUEST,

    /**
     * RPC 回复消息
     */
    RPC_RESPONSE,

    /**
     * 错误消息，当消息为此类型时，body 将指示错误原因
     */
    ERROR,

    /**
     * 成功消息，当消息无任何载荷或者无任何意义时，而仅仅标识消息成功，可使用此类型
     */
    SUCCESS,
}
