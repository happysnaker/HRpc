package com.happysnaker.common.builder;



import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.RpcMessage;

import java.util.Date;
import java.util.UUID;

/**
 * HRpcMessage 的建造者
 * @author Happysnaker
 * @description
 * @date 2022/2/28
 * @email happysnaker@foxmail.com
 */
public class RpcMessageBuilder {
    private long id;
    private RpcMessageType type;
    private int priority;
    private Date date;
    private Object plus;
    private Object body;

    public static RpcMessageBuilder getInstance() {
        return new RpcMessageBuilder()
                .addId(UUID.randomUUID().getLeastSignificantBits())
                .addPriority(-1)
                .addDate(new Date());
    }


    public static RpcMessage createSimpleMessage(RpcMessageType type, Object body) {
        return getInstance().addType(type).addBody(body).build();
    }

    public static RpcMessage createSimpleMessage(long id, RpcMessageType type, Object body) {
        return getInstance().addType(type).addBody(body).addId(id).build();
    }

    public static RpcMessage createErrorMessage(Object errorMsg) {
        return getInstance().addType(RpcMessageType.ERROR).addBody(errorMsg).build();
    }

    public static RpcMessage createErrorMessage(Object errorMsg, long id) {
        return getInstance().addType(RpcMessageType.ERROR).addBody(errorMsg).build();
    }

    public static RpcMessage createSuccessMessage(Object successMsg, long id) {
        return getInstance().addType(RpcMessageType.SUCCESS).addBody(successMsg).addId(id).build();
    }


    public RpcMessage build() {
        return new RpcMessage(id, type, priority, date, plus, body);
    }

    public RpcMessageBuilder addId(long id) {
        this.id = id;
        return this;
    }

    public RpcMessageBuilder addType(RpcMessageType type) {
        this.type = type;
        return this;
    }

    public RpcMessageBuilder addPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public RpcMessageBuilder addDate(Date date) {
        this.date = date;
        return this;
    }

    public RpcMessageBuilder addPlus(Object plus) {
        this.plus = plus;
        return this;
    }

    public RpcMessageBuilder addBody(Object body) {
        this.body = body;
        return this;
    }

}
