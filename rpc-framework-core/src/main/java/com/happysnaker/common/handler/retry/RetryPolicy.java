package com.happysnaker.common.handler.retry;

/**
 * 重试策略
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */
public interface RetryPolicy {

    /**
     * 当操作由于某种原因失败时调用。此方法应返回 true 以进行另一次尝试。
     * @param retryCount – 到目前为止重试的次数
     * @return
     */
    boolean allowRetry(int retryCount);

    /**
     * 以毫秒为单位获取当前重试计数的睡眠时间。
     *
     * @param retryCount 当前重试次数
     * @return 睡眠的时间
     */
    long getSleepTimeMs(int retryCount);
}
