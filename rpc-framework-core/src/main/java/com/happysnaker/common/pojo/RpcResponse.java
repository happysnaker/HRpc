package com.happysnaker.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse {
    /**
     * 标识请求的唯一 ID
     */
    private long id;

    /**
     * 服务返回结果
     */
    private Object result;

    /**
     * 结果类型
     */
    private Class resultType;

    /**
     * 错误原因
     */
    private String exceptionMessage;
}
