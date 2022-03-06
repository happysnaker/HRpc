package com.happysnaker.factory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public class ThreadPollFactory {
    volatile static ScheduledExecutorService executor = null;

    public static ScheduledExecutorService getInstance() {
        if (executor == null) {
            synchronized (ThreadPollFactory.class) {
                if (executor == null) {
                    executor = new ScheduledThreadPoolExecutor(4);
                    return executor;
                }
            }
        }
        return executor;
    }
}
