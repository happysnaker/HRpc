package com.happysnaker.server.handler;

import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.server.registry.ServerRegistry;
import com.happysnaker.server.registry.impl.DefaultRpcServerRegistry;
import io.netty.channel.*;

/**
 * 此处理程序用以处理与注册中心通信
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
@ChannelHandler.Sharable
public class ServerRegisterHandler extends SimpleChannelInboundHandler {
    private ServerRegisterHandler(){
    };

    public static ServerRegisterHandler getInstance() {
        return serverRegisterHandler;
    }

    static ServerRegisterHandler serverRegisterHandler = new ServerRegisterHandler();
    ChannelHandlerContext ctx;
    ServerRegistry serverRegistry = DefaultRpcServerRegistry.getInstance();

    public Channel getChannel() {
        return ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    /**
     * 复用通道发送消息，服务端与注册中心的通信总是复用此连接
     * <p>当连接断开时，此方法会自动尝试重新连接</p>
     * @param message
     * @throws InterruptedException
     */
    public ChannelFuture sendMessage(RpcMessage message) throws InterruptedException {
        if (!ctx.channel().isOpen() || !ctx.channel().isActive()) {
            ctx.channel().connect(null).sync();
        }
        ChannelFuture future = ctx.channel().writeAndFlush(message);
        return future;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Object msg) throws Exception {
        RpcMessage message = (RpcMessage) msg;
        // 收到注册中心回复，告知服务
        serverRegistry.complete(message);
    }
}
