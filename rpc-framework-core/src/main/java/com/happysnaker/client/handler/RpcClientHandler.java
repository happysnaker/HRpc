package com.happysnaker.client.handler;

import com.happysnaker.client.service.RpcClientService;
import com.happysnaker.client.service.impl.DefaultRpcClient;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.RpcResponse;
import com.happysnaker.common.utils.PairUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
@ChannelHandler.Sharable
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler {

    private Bootstrap b;
    private EventLoopGroup group;

    private static RpcClientHandler handler;
    private static Map<PairUtil<String, Integer>, Channel> serverChannelMap;
    private static RpcClientService service = new DefaultRpcClient();

    public static RpcClientHandler getInstance(Bootstrap b, EventLoopGroup group) {
        if (handler == null) {
            synchronized (RpcClientHandler.class) {
                if (handler == null) {
                    handler = new RpcClientHandler(b, group);
                    return handler;
                }
            }
        }
        return handler;
    }

    public static RpcClientHandler getRpcClientHandler() {
        return handler;
    }

    private RpcClientHandler(Bootstrap b, EventLoopGroup group) {
        this.b = b;
        this.group = group;
    }

    public void stop() {
        if (group != null) {
            group.shutdownGracefully();
        }
        if (serverChannelMap != null) {
            for (Channel value : serverChannelMap.values()) {
                if (value != null && value.isOpen()) {
                    try {
                        Channel channel = value.closeFuture().sync().channel();
                        log.info("已断开与服务端 {} 的通信", channel.remoteAddress());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcMessage message = (RpcMessage) msg;
        if (message.getBody() instanceof RpcResponse) {
            service.complete((RpcResponse) message.getBody());
        }
    }


    private ChannelFuture connect(String host, int port) {
        return b.connect(host, port);
    }

    /**
     * 连接服务器并发送消息，次方法会自动保存与服务器的 Channel
     * @param host
     * @param port
     * @param message
     * @return
     */
    public ChannelFuture connectAndWrite(String host, int port, RpcMessage message) throws InterruptedException {
        if (serverChannelMap == null) {
            serverChannelMap = new ConcurrentHashMap<>(10);
        }
        PairUtil pair = PairUtil.of(host, port);
        Channel channel;
        if ((channel = serverChannelMap.getOrDefault(pair, null)) != null) {
            if (channel.isActive() && channel.isOpen()) {
                return channel.writeAndFlush(message);
            } else {
                serverChannelMap.remove(pair);
            }
        }
        ChannelFuture connect = connect(host, port);
        connect.sync(); // 同步等待建立连接
        serverChannelMap.put(pair, connect.channel());
        return connectAndWrite(host, port, message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        return;
    }
}
