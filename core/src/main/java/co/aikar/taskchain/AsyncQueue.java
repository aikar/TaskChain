/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.taskchain;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public interface AsyncQueue {
    /**
     * Task to post async of main thread
     * @param runnable
     */
    void postAsync(Runnable runnable);

    /**
     * Call during game shutdown state
     * @param timeout
     * @param unit
     */
    void shutdown(int timeout, TimeUnit unit);
}
