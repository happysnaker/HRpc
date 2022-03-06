package com.happysnaker.common.config;

import io.netty.util.AttributeKey;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public class RpcConfig {
    /**
     * 幻数
     */
    public static final short PHANTOM_NUMBER = (short) 0XAF55;

    public static final AttributeKey MODE = AttributeKey.valueOf("test");
}
