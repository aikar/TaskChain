/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.taskchain;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class TaskChainAsyncQueue implements AsyncQueue {
    private final AtomicInteger threadId = new AtomicInteger();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(r -> {
        final Thread thread = new Thread(r);
        thread.setName("TaskChainAsyncQueue Thread " + threadId.getAndIncrement());
        return thread;
    });

    public void postAsync(Runnable runnable) {
        executor.submit(runnable);
    }

    /**
     * Call during game shutdown state
     * @param timeout
     * @param unit
     */
    public void shutdown(int timeout, TimeUnit unit) {
        try {
            executor.setRejectedExecutionHandler((r, executor1) -> r.run());
            executor.shutdown();
            executor.awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
