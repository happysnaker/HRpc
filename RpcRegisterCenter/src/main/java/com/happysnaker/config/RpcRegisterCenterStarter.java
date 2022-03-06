package com.happysnaker.config;

import com.happysnaker.common.handler.RpcMessageDecoder;
import com.happysnaker.common.handler.RpcMessageEncoder;
import com.happysnaker.handler.RpcRegisterCenterHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
@Slf4j
public class RpcRegisterCenterStarter {
    /**
     * @see #start(String, int, long)
     * @param port
     * @throws InterruptedException
     */
    public static void start(int port) throws InterruptedException {
        start(null, port, 6L);
    }

    /**
     * @see #start(String, int, long)
     * @param port
     * @param readerIdleTime
     * @throws InterruptedException
     */
    public static void start(int port, long readerIdleTime) throws InterruptedException {
        start(null, port, readerIdleTime);
    }

    /**
     * 启动服务
     * @param ip
     * @param port
     * @param readerIdleTime 心跳检查超时时长，单位秒，默认 6s
     * @throws InterruptedException
     */
    public static void start(String ip, int port, long readerIdleTime) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        SocketAddress socketAddress = ip != null ?
                new InetSocketAddress(ip, port) : new InetSocketAddress(port);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(socketAddress)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(readerIdleTime, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new RpcMessageDecoder())
                                    .addLast(new RpcMessageEncoder())
                                    .addLast(new RpcRegisterCenterHandler());
                        }
                    });

            ChannelFuture f = bootstrap.bind().sync();
            log.info("Register centre start on " + socketAddress);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
        }
    }
}
