package com.happysnaker.handler;


import com.happysnaker.common.builder.RpcMessageBuilder;
import com.happysnaker.common.config.ServiceListManager;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.Service;
import com.happysnaker.handler.heartbeat.HeartbeatStrategy;
import com.happysnaker.handler.heartbeat.impl.DefaultHeartbeatStrategy;
import com.happysnaker.registry.Registry;
import com.happysnaker.registry.impl.DefaultRegistry;
import com.happysnaker.service.consumer.ConsumerWatcher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
@ChannelHandler.Sharable
@Slf4j
public class RpcRegisterCenterHandler extends SimpleChannelInboundHandler {
    /**
     * 默认的注册中心
     */
    static Registry registry = new DefaultRegistry();

    /**
     * 默认的心跳检测策略
     */
    static HeartbeatStrategy heartbeatStrategy = new DefaultHeartbeatStrategy();

    /**
     * 中心服务列表管理者
     */
    static ServiceListManager serviceListManager = ServiceListManager.getInstance();

    /**
     * Channel 与 Service 之间的关联，key 是弱引用，一旦其他地方都不再使用此 Channel，其将会被自动回收
     */
    static Map<Channel, Set<Service>> channelServiceMap = new ConcurrentHashMap<>();



    /**
     * <p>消息的核心处理类，注意此方法会集中式的捕捉异常，并尝试将异常以及引起此异常的消息 ID 发送回发送方；而成功消息将由 registy 返回告知</p>
     * <p><strong>简言之，无论成功与否，注册中心总是会告知发送方结果，双方以消息 ID 作为唯一标识</strong></p>
     * <p><strong>要注意，registry 中的方法必须要抛出异常而不是捕捉它，异常将在此处统一管理</strong></p>
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcMessage message = (RpcMessage) msg;
        log.info("{} 发送消息：{}", ctx.channel(), message);
        if (message.getBody() instanceof Service) {
            // 保存与服务的通道，用以后续心跳检测功能
            // 不可以在 inActive 时保存，因为我们仅仅只保存服务器端的连接服务
            channelServiceMap.putIfAbsent(ctx.channel(), new ConcurrentSet<>());
            channelServiceMap.get(ctx.channel()).add((Service) message.getBody());
        }
        heartbeatStrategy.heartbeat(ctx.channel());
        try {
            RpcMessageType type = message.getType();
            switch (type) {
                case SERVICE_REGISTRATION:
                    // 服务注册，注册中心注册、心跳监控系统注册
                    registry.register(message, ctx.channel());
                    heartbeatStrategy.register(ctx.channel());
                    break;
                case SERVICE_UN_REGISTRATION:
                    // 服务下线
                    registry.unRegister(message, ctx.channel());
                    heartbeatStrategy.unRegister(ctx.channel());
                    break;
                case SERVICE_DISCOVERY:
                    // 服务发现
                    registry.lookup(message, ctx.channel());
                    break;
                case SUBSCRIPTION_SERVICE:
                    // 服务订阅
                    registry.subscribe(message, ctx.channel());
                    break;
                case UN_SUBSCRIPTION_SERVICE:
                    // 取消订阅
                    registry.unSubscribe(message, ctx.channel());
                    break;
                case HEARTBEAT_DETECTION:
                    // 服务发送心跳包、更新所有服务的状态
                    for (Service service : channelServiceMap.
                            getOrDefault(ctx.channel(), new ConcurrentSet<>())) {
                        service.setStatus(message.getBody());
                    }
                    break;
                default:
                    break;
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
            // 将错误消息发送回
            ctx.channel().writeAndFlush(
                    RpcMessageBuilder.createErrorMessage(e.getMessage(), message.getId())
            );
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel disconnect: {}", ctx.channel());
        // 客户端强迫关闭连接，默认下线
        for (Service service : channelServiceMap.getOrDefault(ctx.channel(), new ConcurrentSet<>())) {
            // 通知客户端此服务下线
            registry.notify(ConsumerWatcher.Event.SERVICE_OFFLINE, service);
            // 断开连接
            ctx.channel().disconnect();
            // 移除此服务
            serviceListManager.removeServiceInstance(service);

            log.info("{} 服务方强行关闭连接，已移除此服务", service);
        }
        if (!ctx.channel().isOpen()) {
            channelServiceMap.remove(ctx.channel());
        }
        serviceListManager.logServiceList();
        super.channelInactive(ctx);
    }

    /**
     * 给定时间内未发送信息给注册中心，注册中心将调用此方法
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE &&
                    heartbeatStrategy.heartStop(ctx.channel())) {
                // 获取与此通道关联的服务列表
                Set<Service> services = channelServiceMap.getOrDefault(
                        ctx.channel(), new ConcurrentSet<>());
                for (Service service : services) {
                    // 通知客户端此服务下线
                    registry.notify(ConsumerWatcher.Event.SERVICE_OFFLINE, service);
                    // 断开连接
                    ctx.channel().close();
                    // 移除此服务
                    serviceListManager.removeServiceInstance(service);

                    log.info("{} 心跳停止，已移除此服务", service);
                }
                // 关闭通道
                System.out.println("ctx.channel() = " + ctx.channel());
                ctx.channel().close();
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
}
