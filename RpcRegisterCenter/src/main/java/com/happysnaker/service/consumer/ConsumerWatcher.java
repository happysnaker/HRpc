package com.happysnaker.service.consumer;


import com.happysnaker.common.pojo.Service;

/**
 * 消费者监控接口
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public interface ConsumerWatcher {
    enum Event {
        /**
         * 服务下线事件
         */
        SERVICE_OFFLINE,
        /**
         * 服务上线事件
         */
        SERVICE_ONLINE
    }

    /**
     * 用以通知客户端服务上线或下线
     * @param event 事件类型
     * @param service 具体服务
     */
    void notify(Event event, Service service);
}
