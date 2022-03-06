package com.happysnaker.common.handler;

import com.happysnaker.common.handler.retry.RetryPolicy;
import com.happysnaker.common.handler.retry.impl.ExponentialBackOffRetry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 断线重连处理器，此处理器只用于客户端
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
@ChannelHandler.Sharable
@Slf4j
public class ReconnectHandler extends ChannelInboundHandlerAdapter {

    private int retries = 0;
    private RetryPolicy retryPolicy;
    private Bootstrap b;

    public ReconnectHandler(Bootstrap b) {
        this.b = b;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        retries = 0;
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        boolean allowRetry = getRetryPolicy().allowRetry(retries);

        if (allowRetry) {
            long sleepTimeMs = getRetryPolicy().getSleepTimeMs(retries);
            System.out.println(String.format("Try to reconnect to the server after %dms. Retry count: %d.", sleepTimeMs, ++retries));

            final EventLoop eventLoop = ctx.channel().eventLoop();
            ScheduledFuture<?> future = eventLoop.schedule(() -> {
                b.connect(ctx.channel().remoteAddress());
            }, sleepTimeMs, TimeUnit.MILLISECONDS);

            future.addListener(new GenericFutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    if (!future.isSuccess()) {
                        channelInactive(ctx);
                    }
                }
            });
        }
        ctx.fireChannelInactive();
    }


    private RetryPolicy getRetryPolicy() {
        if (this.retryPolicy == null) {
            this.retryPolicy = new ExponentialBackOffRetry();
        }
        return this.retryPolicy;
    }

}