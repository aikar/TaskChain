/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    default void postAsync(Runnable run) {
        getAsyncQueue().postAsync(run);
    };

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
     * and manually call {@link TaskChainFactory#shutdown(int, TimeUnit)}
     * @param factory Factory to shutdown
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
