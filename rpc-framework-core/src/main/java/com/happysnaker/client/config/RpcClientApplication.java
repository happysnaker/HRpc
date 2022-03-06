package com.happysnaker.client.config;

import com.happysnaker.client.handler.ClientRegisterHandler;
import com.happysnaker.client.handler.RpcClientHandler;
import com.happysnaker.client.proxy.RpcReference;
import com.happysnaker.client.registry.ClientRegistry;
import com.happysnaker.client.registry.EnableRpcClient;
import com.happysnaker.client.registry.impl.DefaultClientRegistry;
import com.happysnaker.client.rule.LoadBalanceRule;
import com.happysnaker.common.exception.CanNotUpdateServiceListException;
import com.happysnaker.common.handler.ReconnectHandler;
import com.happysnaker.common.handler.RpcMessageDecoder;
import com.happysnaker.common.handler.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.reflections.Reflections;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 客户端启动类，兼客户端配置中心
 * @author Happysnaker
 * @description
 * @date 2022/3/3
 * @email happysnaker@foxmail.com
 */
public class RpcClientApplication {
    public static EventLoopGroup registerCenterGroup;
    public static String registerCenterIp;
    public static int registerCenterPort;
    public static String packageScan;
    public static int numberOfRetry;
    public static long timeout;
    public static long subscribeRate;
    public static Class<? extends LoadBalanceRule> rule;
    public static int virtualNodeNum;

    public static void initConfig(EnableRpcClient e) {
        registerCenterIp = e.registerCenterIp();
        registerCenterPort = e.registerCenterPort();
        packageScan = e.packageScan();
        numberOfRetry = e.numberOfRetry();
        timeout = e.timeout();
        subscribeRate = e.subscribeRate();
        rule = e.rule();
        virtualNodeNum = e.virtualNodeNum();
    }

    static ClientRegistry registry = DefaultClientRegistry.getInstance();

    /**
     * 启动 RPC 客户端，订阅相关服务，此方法不会堵塞用户进程
     * @throws Exception
     */
    public static void start() throws Exception {

        // 获取启动类上的 Enable 注解信息
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
        if (!mainClass.isAnnotationPresent(EnableRpcClient.class)) {
            throw new ClassNotFoundException("Can not find annotation EnableRpcClient");
        }
        EnableRpcClient client = (EnableRpcClient) mainClass.getAnnotation(EnableRpcClient.class);

        // 初始化配置
        initConfig(client);

        if (client.packageScan().isEmpty()) {
            packageScan = mainClass.getPackageName();
        }

        // 启动与注册中心的连接
        startClientRegistry(registerCenterIp, registerCenterPort);

        // 订阅服务
        for (String s : getServiceNames()) {
            registry.lookupAndSubscribe(s);
        }

        // 启动客户端，创建与服务方的通信
        startRpcClient();

        // 注册关闭钩子
        registerShutdownHook();
    }


    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // 取消订阅
                for (String s : getServiceNames()) {
                    registry.unSubscribe(s);
                }
                // 关闭与注册中心的通道组
                if (registerCenterGroup != null) {
                    registerCenterGroup.shutdownGracefully();
                }
                // 关闭与服务端的连接
                RpcClientHandler.getRpcClientHandler().stop();
            }
        });
    }

    /**
     * 创建客户端 Netty，负责与 RPC 服务提供方通信
     */
    private static void startRpcClient() {
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        RpcClientHandler handler = RpcClientHandler.getInstance(b, group);
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ReconnectHandler(b))
                                .addLast(new RpcMessageDecoder())
                                .addLast(new RpcMessageEncoder())
                                .addLast(handler);
                    }
                });
    }


    /**
     * 发起到注册中心的连接，并订阅服务
     *
     * @param ip   注册中心 IP
     * @param port 注册中心端口
     * @throws InterruptedException
     */
    private static void startClientRegistry(String ip, int port) throws InterruptedException, CanNotUpdateServiceListException, ExecutionException, TimeoutException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        Bootstrap bootstrap = new Bootstrap();
        ClientRegisterHandler handler = ClientRegisterHandler.getInstance();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(socketAddress)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new RpcMessageDecoder())
                                .addLast(new RpcMessageEncoder())
                                .addLast(handler);
                    }
                });

        ChannelFuture f = bootstrap.connect().sync();
        registerCenterGroup = group;
    }

    /**
     * 扫描包获取客户端内需要引用的所有服务名
     * @return
     */
    private static List<String> getServiceNames() {
        List ans = new LinkedList();
        Reflections reflections = new Reflections(packageScan);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RpcReference.class);
        for (Class clazz : classes) {
            RpcReference reference = (RpcReference) clazz.getAnnotation(RpcReference.class);
            if (reference != null) {
                ans.add(reference.serviceName());
            }
        }
        return ans;
    }
}
