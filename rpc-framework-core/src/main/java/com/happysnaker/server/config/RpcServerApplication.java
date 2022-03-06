package com.happysnaker.server.config;

import com.happysnaker.common.handler.ReconnectHandler;
import com.happysnaker.common.handler.RpcMessageDecoder;
import com.happysnaker.common.handler.RpcMessageEncoder;
import com.happysnaker.server.exception.ServiceRegistryException;
import com.happysnaker.server.handler.RpcServerHandler;
import com.happysnaker.server.handler.ServerRegisterHandler;
import com.happysnaker.server.registry.EnableRpcService;
import com.happysnaker.server.registry.RpcService;
import com.happysnaker.server.registry.ServerRegistry;
import com.happysnaker.server.registry.impl.DefaultRpcServerRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RPC 服务端启动类，此类兼服务端配置中心
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
@Slf4j
public class RpcServerApplication {
    /**
     * 保存与注册中心的连接组，用以安全退出
     */
    public static EventLoopGroup registryGroup;
    /**
     * 服务提供方的 IP 地址
     */
    public static String ip;
    /**
     * 服务提供方监听的断口
     */
    public static int port;
    /**
     * 心跳包发生速率
     */
    public static long heartbeatRate;
    /**
     * 被标识为 RPC 服务的类
     */
    public static List<Class> serviceClasses = new ArrayList<>();
    public static ServerRegistry serverRegistry = DefaultRpcServerRegistry.getInstance();



    /**
     * 等价于 start(InetAddress.getLocalHost().getHostAddress(), port)
     * @see #start(String, int) 
     * @param port
     * @throws Exception
     */
    public static void start(int port) throws Exception {
        start(InetAddress.getLocalHost().getHostAddress(), port);
    }


    /**
     * 服务提供者的启动程序
     * @param ip 服务提供方监听的 IP 地址
     * @param port 服务提供方监听的端口号
     */
    public static void start(String ip, int port) throws Exception, ServiceRegistryException {
        RpcServerApplication.ip = ip;
        RpcServerApplication.port = port;

        // 获取启动类上的 EnableRpcService 注解信息
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        Class mainClass = null;
        for (StackTraceElement element : elements) {
            if (element.getMethodName().equals("main")) {
                mainClass = Class.forName(element.getClassName());
            }
        }

        if (mainClass == null) {
            mainClass = Class.forName(elements[elements.length - 1].getClassName());
        }

        if (!mainClass.isAnnotationPresent(EnableRpcService.class)) {
            throw new ClassNotFoundException("Can not find annotation EnableRpcService");
        }



        EnableRpcService enableRpcService = (EnableRpcService) mainClass.getAnnotation(EnableRpcService.class);
        // 配置
        heartbeatRate = enableRpcService.heartbeatRate();

        // 如果为 null，默认为主启动类所在包
        String packageScan = mainClass.getPackage().getName();
        if (!enableRpcService.packageScan().isEmpty()) {
            packageScan = enableRpcService.packageScan();
        }

        // 启动服务方与注册中心的连接
        startServerRegistryChannel(enableRpcService.registerCenterIp(), enableRpcService.registerCenterPort());

        // 开始扫描包，注册 RpcService
        Reflections reflections = new Reflections(packageScan);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RpcService.class);
        for (Class clazz : classes) {
            RpcService v = (RpcService) clazz.getAnnotation(RpcService.class);
            if (v != null) {
                serviceClasses.add(clazz);
                serverRegistry.registry(clazz);
            }
        }

        registerShutdownHook();

        // 启动服务监听 RPC CLIENT
        startRpcServer();

    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // 服务下线，主动告知注册中心
                try {
                    for (Class serviceClass : serviceClasses) {
                        serverRegistry.unRegistry(serviceClass);
                    }

                    if (registryGroup != null) {
                        registryGroup.shutdownGracefully();
                    }
                } catch (Exception e) {
                    // 出错了就算了
                }
            }
        });
    }

    /**
     * 作为 RPC 服务提供方开始监听客户端的连接
     * @throws InterruptedException
     */
    private static void startRpcServer() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(socketAddress)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new RpcMessageDecoder())
                                    .addLast(new RpcMessageEncoder())
                                    .addLast(new RpcServerHandler());
                        }
                    });

            ChannelFuture f = bootstrap.bind().sync();
            log.info("Rpc server start on " + socketAddress);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
        }
    }

    /**
     * 发起到注册中心的连接
     *
     * @param ip   注册中心 IP
     * @param port 注册中心端口
     * @throws InterruptedException
     */
    private static void startServerRegistryChannel(String ip, int port) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        Bootstrap bootstrap = new Bootstrap();
        ServerRegisterHandler handler = ServerRegisterHandler.getInstance();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(socketAddress)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ReconnectHandler(bootstrap))
                                .addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS))
                                .addLast(new RpcMessageDecoder())
                                .addLast(new RpcMessageEncoder())
                                .addLast(handler);
                    }
                });

        ChannelFuture f = bootstrap.connect().sync();
        System.out.println("已与注册中心建立连接：" + f.channel().remoteAddress());
    }
}
