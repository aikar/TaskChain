/*
 * Copyright (c) 2016 Daniel Ennis (Aikar) - MIT License
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TaskChainFactory {
    private final GameInterface impl;
    private final AsyncQueue asyncQueue;
    private final Map<String, TaskChain<?>> sharedChains = new HashMap<>();
    volatile boolean shutdown = false;

    @SuppressWarnings("WeakerAccess")
    public TaskChainFactory(GameInterface impl) {
        this.impl = impl;
        this.asyncQueue = impl.getAsyncQueue();
        impl.registerShutdownHandler(this);
    }

    /**
     * Creates a new chain.
     * @return
     */
    public <T> TaskChain<T> newChain() {
        return new TaskChain<>(this);
    }

    /**
     * Allows re-use of a Chain by giving it a name. This lets you keep adding Tasks to
     * an already executing chain. This allows you to assure a sequence of events to only
     * execute one at a time, but may be registered and executed from multiple execution points
     * or threads.
     *
     * Task Data is not shared between chains of the same name. The only thing that is shared
     * is execution order, in that 2 sequences of events can not run at the same time.
     *
     * If 2 chains are created at same time under same name, the first chain will execute fully before the 2nd chain will start, no matter how long
     *
     * @param name
     * @param <T>
     * @return
     */
    public synchronized <T> TaskChain<T> newSharedChain(String name) {
        TaskChain<?> chain;
        synchronized (sharedChains) {
            chain = sharedChains.get(name);

            if (chain != null) {
                //noinspection NestedSynchronizedStatement
                synchronized (chain) {
                    if (chain.done) {
                        chain = null;
                    }
                }
            }

            if (chain == null) {
                chain = new TaskChain<>(this, true, name);
                sharedChains.put(name, chain);
            }
        }

        return new SharedTaskChain<>(this, (TaskChain<T>) chain);
    }

    /**
     * Shuts down the TaskChain system, forcing all tasks to run on current threads and finish.
     * @param duration
     * @param units
     */
    public void shutdown(int duration, TimeUnit units) {
        shutdown = true;
        asyncQueue.shutdown(duration, units);
    }

    GameInterface getImplementation() {
        return impl;
    }

    void removeSharedChain(String name) {
        synchronized (sharedChains) {
            sharedChains.remove(name);
        }
    }
}
