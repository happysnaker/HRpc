package com.happysnaker.common.pojo;

import lombok.Getter;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * 保存服务的实体类
 *
 * @author Happysnaker
 * @description
 * @date 2022/2/28
 * @email happysnaker@foxmail.com
 */
@Setter
@Getter
public class Service {
    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务器唯一的 ID
     */
    private long instanceId;

    /**
     * 此服务的 IP 地址
     */
    private String ip;

    /**
     * 此服务的端口号
     */
    private int port;

    /**
     * 此字段有两种意义：
     * <p>1. 如果一个接口有多个服务，可使用此字段区分</p>
     * <p>2. 如果一台主机上运行了多台服务，可使用此字段区分</p>
     * 此字段的值必须大于等于 0
     */
    private int group;

    /**
     * 服务的 URL，通过此字段，可以获取更多有效的信息，例如协议、版本等
     */
    private URL url;

    /**
     * 服务的健康状态，服务发送心跳包时应报告此状态，此字段由注册中心更新
     * <p>此字段类型为 {@link ServiceHealthStatus}，由于 Kryo 无法识别非 java 包下的类型，因此使用 Object 存储</p>
     */
    private Object status;

    /**
     * 一些其他的、额外的信息
     */
    private Object plus;

    /**
     * 怎样才能标识服务的唯一性？也许只要服务方 IP 端口 服务名相同，则认定服务是相同的，但这样依然会存在一些问题，例如一台主机上运行了两架 JVM 同时部署了相同的服务；又比如一个服务接口可能会有多个实现，它们服务名、IP、端口完全相同，但行为不同。
     * <p>因此，我们还需要 group 字段唯一的标识</p>
     * <p>由于网络问题，服务可能会重复提交注册，因此，保证服务的唯一性是很重要的！</p>
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return port == service.port && group == service.group && Objects.equals(serviceName, service.serviceName) && Objects.equals(ip, service.ip);
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceName, ip, port, group);
    }


    public Service(String serviceName, String ip, int port) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
        try {
            this.url = new URL("http://" + ip + ":" + port);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Service(String serviceName, String ip, int port, int group) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
        this.group = group;
        try {
            this.url = new URL("http://" + ip + ":" + port);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Service() {

    }


    public Service(String serviceName) {
        this.serviceName = serviceName;
    }

    public Service(Service service) {
        this(service.serviceName, service.instanceId, service.ip, service.port, service.group, service.url, service.plus);
    }

    public Service(String serviceName, long instanceId, String ip, int port, int group, URL url, Object plus) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.ip = ip;
        this.group = group;
        this.port = port;
        this.url = url;
        this.plus = plus;
    }


    @Override
    public String toString() {
        return "Service{" +
                "serviceName='" + serviceName + '\'' +
                ", instanceId=" + instanceId +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", group=" + group +
                ", url=" + url +
                ", status=" + status +
                ", plus=" + plus +
                '}';
    }
}
