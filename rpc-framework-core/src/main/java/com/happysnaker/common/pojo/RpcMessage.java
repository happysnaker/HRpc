package com.happysnaker.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * HRpc 发送消息的通用实体类
 * @author Happysnaker
 * @description
 * @date 2022/2/28
 * @email happysnaker@foxmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RpcMessage {
    /**
     * 消息的 id
     */
    private long id;

    /**
     * 指示消息的类型
     */
    private RpcMessageType type;


    /**
     * 此消息的优先级
     */
    private int priority;

    /**
     * 消息发送的时间
     */
    private Date date;

    /**
     * 其他附加的信息
     */
    private Object plus;

    /**
     * 消息的实体
     */
    private Object body;
}
