package com.happysnaker.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务健康状况
 * @author Happysnaker
 * @description
 * @date 2022/3/5
 * @email happysnaker@foxmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceHealthStatus {
    /**
     * 服务连接数
     */
    int connectNum;

    /**
     * 服务是否可达
     */
    boolean reachable;
}
