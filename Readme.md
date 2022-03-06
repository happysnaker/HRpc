[toc]

# HRpc

HRpc 是一款基于 Netty 的简易 RPC 框架，此项目旨在学习，实现了注册中心、服务提供方和服务消费方三个模块，此项目的基本功能及技术体系如下：

- 基于 Kryo 实现序列化，自定义协议防止粘包。
- 通过 Netty 定时任务实现心跳包发送、订阅信息发送。
- 基于反射实现对服务类方法的调用。
- 基于 Netty IdleStateHandler 类实现心跳包检测。
- 实现 Channel 长连接，自动重连策略。
- 基于注解化和扫描包的方式进行配置，使用户对 RPC 无感。
- 采用注解化和动态代理技术自动注入 RPC 服务。
- 基于策略模式实现负载均衡，可选择的策略有：轮询、随机、最少连接 和 一致性哈希算法。
- 使用 CompletableFuture 使代码解耦，无耦合的获取结果。
- 实现服务降级功能，一旦服务不可用，可指定一个服务作为降级。

## 整体架构

项目整体分为三个板块，注册中心、服务提供方以及服务消费方，整体架构如下图所示：

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306161112576.png" alt="image-20220306161112576" style="zoom:50%;" />

## 项目模块结构

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306162903492.png" alt="image-20220306162903492" style="zoom:50%;" />

**core 是核心包**、Consumer 必须依赖于 core 中的 client 和 common 包，Service 必须依赖于 core 中的 server 和 common 包，注册中心 RegisterCenter 必须依赖于 core 中从 common 包。

在 IDEA 中，会自动提示你添加相关模块依赖到项目中，当然，在 release  页面下，我上传了两个 jar 包，一个是 core 核心包，一个是整合了 core.common 的注册中心，注册中心可以通过 `java -jar` 在后台自动运行。



## 注册中心

注册中心的架构并不复杂，如下图所示：

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306164555079.png" alt="image-20220306164555079" style="zoom:50%;" />

它的包结构也十分清晰，完全对应上图架构：

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306164811908.png" alt="image-20220306164811908" style="zoom:50%;" />

当然，还得包括 core 包下的 common 包，common 包是所有板块所依赖的包，含有一些共有的实体类和 utils。



## 服务提供方

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306170115549.png" alt="image-20220306170115549" style="zoom:50%;" />

服务提供方首先要做的是扫描服务类，这通过注解和扫描包的方式实现，一旦服务全部注册完毕，服务器就要启动后台线程，不停的向注册中心发送心跳包，汇报自己的健康状态，随后，服务器就可以启动 RpcServer 处理程序，等待客户端的消息。

当收到客户端消息后，根据 RpcRequest 中的全限定类名、方法名等信息，获取服务类，通过反射的方式调用并返回结果。

包结构如下图所示：registry 包是提供注册的服务，service 是提供解析 RPC 请求并运行相关服务的服务。

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306170614499.png" alt="image-20220306170614499" style="zoom:50%;" />



## 服务消费方

首先，消费方必须要向注册中心订阅相关服务，并且要有一个处理程序接收注册中心传来的推送信息，为防止连接异常断开，消费方也必须定时地、持续地向注册中心发送订阅信息。

主要看看客户端是如何发起 RPC 调用的，流程如下图所示：

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306171825946.png" alt="image-20220306171825946" style="zoom:50%;" />

总体而言，也不算太复杂，包结构如下图所示：

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306171934257.png" alt="image-20220306171934257" style="zoom:50%;" />



proxy 内的 RpcProxy 是 RPC调用的核心流程，rule 是负载均衡策略的多种实现，通过 Factory 生成单例负载均衡策略。



## 源码下载

直接下载源码导入 IDEA 即可，使用 jdk 版本为 11.

相关架构已经介绍过了，可以先参考下面的使用感受以下。

## 使用演示

### 启动注册中心

为了便捷使用，我们通过运行 jar 包的方式在后台运行注册中心，从 release 页面下下载 `RpcRegisterCenter-1.0-SNAPSHOT.jar` 包，在此目录下运行命令：`java -jar RpcRegisterCenter-1.0-SNAPSHOT.jar [端口] [心跳检测的超时时间]`，此命令有两个参数，都具有默认值，端口是注册中心运行的端口，默认为 4567，心跳检测的超时时间是指一旦超过此时间未检测到服务发来心跳包，则认为服务心跳停止，选择特定的策略处理，单位是 s，默认为 6s。

![image-20220306173908889](https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306173908889.png)

现在，注册中心已成功启动！

服务提供方和消费者都必须需要 core 包的依赖，是选择下载源码还是手动导入 jar 包取决于你。

有两种 RPC 调用的方法：

- 第一种是无接口的情况，客户端直接调用该服务。
- 第二种是有多个服务实现了相同的接口，而客户端调用的是接口。

我们将演示第一种方法。

### 服务提供方

我们建立 Service0 模块，在 `com.example.service` 下建立 ExampleService.class：

```java
@RpcService(serviceName = "service2022")
public class ExampleService {
    public String hello() {
        return "Service0 Hello World! -- " + UUID.randomUUID();
    }
}
```

然后为此类添加注解 `@RpcService(serviceName = "service2022")` 以标识此类成为服务类，服务名为 “service2022”，然后再编写主启动类 `com.example.Main`：

```java
@EnableRpcService(registerCenterIp = "localhost", registerCenterPort = 4567)
public class Main {
    public static void main(String[] args) throws Exception {
        RpcServerApplication.start(5678);
    }
}
```

`RpcServerApplication.start(5678);` 意味着 RPC 服务将在端口 5678 运行。

项目结构如下图所示：

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306175112710.png" alt="image-20220306175112710" style="zoom:50%;" />

`@EnableRpcService(registerCenterIp = "localhost", registerCenterPort = 4567)` 注解标识启动 RPC 服务，这两个字段是注册中心的 IP 和 端口，此注解有很多可配置的字段，这里就不演示了，如果不配置扫描包，默认将扫描主类所在的包，即 com.example。

**按照同样的方法再建立一个服务提供者 Service1**，现在我们有两个服务提供者了，Service1 中的 ExampleService#hello 方法返回 `"Service1 Hello World! -- " + UUID.randomUUID()` 以区分 service0。

**Service1 中的监听端口记得更改，不要与 Service0 冲突。**

启动它们，如无意外，将会看到如下信息：

![image-20220306175559401](https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306175559401.png)



### 服务消费者

现在来编写服务消费者代码，消费者包结构如下：

![image-20220306175829241](https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306175829241.png)

**RPC 服务必须以接口的形式出现在消费者端，并且此接口的全限定类名必须要和服务方类或服务方类实现的接口完全一致**，此处，ExampleService 所在的包为 com.example.service，与服务方相同，这个接口只要包含对应方法就好了：

```java
@RpcReference(serviceName = "service2022", fallbackClass = FallbackService.class)
public interface ExampleService {
    String hello();
}
```

`@RpcReference(serviceName = "service2022", fallbackClass = FallbackService.class)` 注解标识此接口为 RPC 引用接口，引用服务为 "service2022"，服务降级由 FallbackService.class 提供：

```java
public class FallbackService implements ExampleService {
    @Override
    public String hello() {
        return "服务降级";
    }
}
```

一旦服务不可用，将会触发服务降级，那么就会调用 FallbackService 类返回结果，服务降级类必须继承接口类。

接下来我们来编写主启动类：

```java
@EnableRpcClient(registerCenterIp = "localhost", registerCenterPort = 4567)
public class Main {
    static {
        try {
            RpcClientApplication.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ExampleService service = RpcProxyFactory.getInstance(ExampleService.class);
        for (int i = 0; i < 10; i++) {
            try {
                System.out.println(service.hello());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}
```

`@EnableRpcClient(registerCenterIp = "localhost", registerCenterPort = 4567)` 注解标识启动 RPC 客户端，两个参数分别是注册中心 IP 与 端口，还可以配置如重试次数、负载均衡策略、超时时间等信息，Rpc 客户端必须首先运行 `RpcClientApplication.start();` 方法，可以将他们放在静态构初始化方法内。

然后我们就可以开始运行我们业务代码了，要获取一个 RPC 服务，只需调用 `RpcProxyFactory.getInstance` 方法，并传递相关接口类信息即可获取到 RPC 服务，例如，示例中将成功获取 ExampleService，运行示例代码：

<img src="https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306181421972.png" alt="image-20220306181421972" style="zoom:50%;" />

代码成功运行，默认为轮询的负载均衡！

现在在客户端运行的过程中我们关闭掉服务方：

![image-20220306181555917](https://happysnaker-1306579962.cos.ap-nanjing.myqcloud.com/img/typora/image-20220306181555917.png)

客户端收到提示，并且自动尝试重连服务器，最终无法连接，触发服务降级。









