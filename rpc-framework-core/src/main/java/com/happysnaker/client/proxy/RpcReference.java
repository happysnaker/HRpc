package com.happysnaker.client.proxy;

import java.lang.annotation.*;

/**
 * 标记一个接口类成为 RPC 客户端
 * <p><strong>此注解必须标注在接口上</strong></p>
 * @author happysnakers
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RpcReference {
    /**
     * 欲使用的服务名称
     * @return
     */
    String serviceName();

    /**
     * 欲使用的服务组号，默认将查询所有的组别
     * @return
     */
    int group() default -1;

    /**
     * <p>服务降级将调用的类，此类必须实现 RpcReference 接口，当 RPC 调用失败时，将调用此类作为降级服务</p>
     * <p>如果为了严谨，在此注解上应该还要提供超时时间，要捕捉的异常类型等元素，但考虑到此项目仅为学习项目，为图方便，所有配置都在主启动类上，只有当耗尽所有重试次数后，才会陷入服务降级
     * </p>
     * <p>以一个特殊的值 {@link Class} 以表示关闭服务降级</p>
     * @return
     */
    Class fallbackClass() default Class.class;


    /**
     * 标识此请求的一致性哈希，此字段当负载均衡选择为一致性哈希时需要填写，默认的值为客户端 IP 所产生的哈希值，此字段必须大于等于 0
     */
    int consistentHash() default -1;
}
