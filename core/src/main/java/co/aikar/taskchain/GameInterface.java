/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.taskchain;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public interface GameInterface {
    /**
     * Determines if the current thread is the main thread or not.
     * @return
     */
    boolean isMainThread();

    /**
     * Returns the AsyncQueue instance used by this game implementation
     * @return
     */
    AsyncQueue getAsyncQueue();

    /**
     * Schedule a runnable to run on the main thread
     * @param run
     */
    void postToMain(Runnable run);

    /**
     * Execute the runnable off of the main thread
     * @param run
     */
    void postAsync(Runnable run);

    /**
     * Schedule a task within the games scheduler using its own units
     *
     * IMPLEMENTATION SPECIFIC
     *
     * @param gameUnits
     * @param run
     */
    void scheduleTask(int gameUnits, Runnable run);

    /**
     * Every factory created needs to register a way to automatically shut itself down (On Disable)
     *
     * If its impossible to provide automatic shutdown registry, you should leave this method blank
     * and manually call {@link TaskChainFactory#shutdown()}
     * @param factory
     */
    void registerShutdownHandler(TaskChainFactory factory);

    /**
     * Adds a delay to the chain execution based on real time
     *
     * Method will be ran async from main thread.
     * Chain must {@link TaskChain#abort()} if the delay is interrupted.
     *
     * @param duration Duration to delay
     * @param units Units to delay in
     * @param run Callback to execute once the delay is done.
     */
    default void scheduleTask(int duration, TimeUnit units, Runnable run) {
        postAsync(() -> {
            try {
                Thread.sleep(units.toMillis(duration));
                run.run();
            } catch (InterruptedException e) {
                TaskChain.abort();
            }
        });
    }
}
