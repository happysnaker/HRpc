package com.happysnaker.common.handler.retry.impl;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/6
 * @email happysnaker@foxmail.com
 */

import com.happysnaker.common.handler.retry.RetryPolicy;

import java.util.Random;

/**
 * <p>重试策略，重试之间的睡眠时间随重试次数增加</p>
 * @author happysnakers
 */
public class ExponentialBackOffRetry implements RetryPolicy {

    /**
     * 默认最大重试次数限制
     */
    private static final int DEFAULT_MAX_RETRIES_LIMIT = 10;
    /**
     * 默认最大睡眠时间，单位为 ms
     */
    private static final int DEFAULT_MAX_SLEEP_MS = Integer.MAX_VALUE;

    private final Random random = new Random();
    /**
     * 最大重试次数，默认为 10
     */
    private final int maxRetries;
    /**
     * 最大睡眠时间，单位 ms，默认 {@link Integer#MAX_VALUE}
     */
    private final int maxSleepMs;

    public ExponentialBackOffRetry() {
        this(DEFAULT_MAX_RETRIES_LIMIT, DEFAULT_MAX_SLEEP_MS);
    }

    public ExponentialBackOffRetry(int maxRetries, int maxSleepMs) {
        this.maxRetries = maxRetries;
        this.maxSleepMs = maxSleepMs;
    }

    /**
     * 如果不超过最大重试次数，则允许重试
     * @param retryCount – 到目前为止重试的次数（第一次为 0）
     * @return
     */
    @Override
    public boolean allowRetry(int retryCount) {
        if (retryCount < maxRetries) {
            return true;
        }
        return false;
    }

    @Override
    public long getSleepTimeMs(int retryCount) {
        if (retryCount < 0) {
            throw new IllegalArgumentException("retries count must greater than 0.");
        }
        if (retryCount > maxRetries) {
            throw new IllegalArgumentException("retries has exceeded the limit");
        }
        /**
         * 在 100ms ~ 2^retryCount * 10 中选择睡眠时间，线程因至少消息 0.1s，否则频繁地陷入休眠会使线程开销过大，乘以一个常数因子保证休眠时间不会过短
         */
        long sleepMs = 10 * (random.nextInt(1 << retryCount) + 10);
        if (sleepMs > maxSleepMs) {
            sleepMs = maxSleepMs;
        }
        return sleepMs;
    }
}