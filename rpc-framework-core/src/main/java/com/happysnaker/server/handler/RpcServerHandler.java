package com.happysnaker.server.handler;

import com.happysnaker.common.builder.RpcMessageBuilder;
import com.happysnaker.common.pojo.RpcMessageType;
import com.happysnaker.common.pojo.RpcMessage;
import com.happysnaker.common.pojo.RpcRequest;
import com.happysnaker.common.pojo.RpcResponse;
import com.happysnaker.server.service.RpcServerService;
import com.happysnaker.server.service.impl.DefaultRpcServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * RPC 服务方处理器
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler {
    RpcServerService service = new DefaultRpcServer();
    static AtomicInteger connectNum = new AtomicInteger(0);

    public static int getConnectNum() {
        return connectNum.get();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connectNum.incrementAndGet();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectNum.decrementAndGet();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcMessage message = (RpcMessage) msg;
        if (message.getType().equals(RpcMessageType.RPC_REQUEST)) {
            RpcRequest request = (RpcRequest) message.getBody();
            RpcResponse response = service.doParseRpcRequest(request);
            // 直接发送消息
            ctx.channel().writeAndFlush(RpcMessageBuilder.
                    createSimpleMessage(RpcMessageType.RPC_RESPONSE, response));
        }
    }
}
