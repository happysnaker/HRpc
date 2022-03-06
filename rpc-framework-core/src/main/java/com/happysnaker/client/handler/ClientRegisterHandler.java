package com.happysnaker.client.handler;

import com.happysnaker.client.registry.ClientRegistry;
import com.happysnaker.client.registry.impl.DefaultClientRegistry;
import com.happysnaker.common.pojo.RpcMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 此处理程序用以处理与注册中心通信
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
public class ClientRegisterHandler extends SimpleChannelInboundHandler {
    private ClientRegisterHandler(){
    };

    public static ClientRegisterHandler getInstance() {
        return serverRegisterHandler;
    }

    static ClientRegisterHandler serverRegisterHandler = new ClientRegisterHandler();

    ChannelHandlerContext ctx;

    ClientRegistry clientRegistry = DefaultClientRegistry.getInstance();

    public Channel getChannel() {
        return ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * 复用通道发送消息，客户端与注册中心的通信总是复用此连接
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
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Object msg) throws Exception {
        RpcMessage message = (RpcMessage) msg;
        clientRegistry.complete(message);
    }
}
