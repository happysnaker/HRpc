package com.happysnaker.server.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类成为 RPC服务，客户端可以通过该类的全限定名调用对应的服务，<strong>如果此类存在接口，客户端也可以通过接口调用被标记类的服务，如果一个接口有多个类实现，那么应当以 group 字段以区分它们</strong>
 * <p>注意：如果存在 IP、端口、服务名、组号 都相同的服务类，那么 RPC 调用的行为是不确定的</p>
 * <p><strong>被此注解标注的服务必须提供无参构造函数</strong></p>
 * @author happysnakers
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcService {
    /**
     * 服务名，必填项
     * @return
     */
    String serviceName();

    /**
     * 服务组别
     * @return
     */
    int group() default 0;
}
