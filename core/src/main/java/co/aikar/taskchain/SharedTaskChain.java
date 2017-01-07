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


import co.aikar.taskchain.TaskChainTasks.Task;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class SharedTaskChain<R> extends TaskChain<R> {
    private final String name;
    private final Map<String, Queue<SharedTaskChain>> sharedChains;
    private Queue<SharedTaskChain> queue;
    private volatile boolean isPending;
    private volatile boolean canExecute = true;

    SharedTaskChain(String name, TaskChainFactory factory) {
        super(factory);
        this.sharedChains = factory.getSharedChains();
        this.name = name;

        synchronized (this.sharedChains) {
            this.queue = sharedChains.get(this.name);
            if (this.queue == null) {
                this.queue = new ConcurrentLinkedQueue<>();
                this.sharedChains.put(this.name, this.queue);
            }
            this.queue.add(this);
        }
    }

    @Override
    public void execute(Consumer<Boolean> done, BiConsumer<Exception, Task<?, ?>> errorHandler) {
        this.setErrorHandler(errorHandler);
        this.setDoneCallback((finished) -> {
            this.setDoneCallback(done);
            this.done(finished);
            processQueue();
        });

        boolean shouldExecute;
        synchronized (this.sharedChains) {
            this.isPending = this.queue.peek() != this;
            shouldExecute = !this.isPending && this.canExecute;
            if (shouldExecute) {
                this.canExecute = false;
            }
        }
        if (shouldExecute) {
            execute0();
        }
    }

    /**
     * Launches the next TaskChain in the queue if it is ready, or cleans up the queue if nothing left to do.
     */
    private void processQueue() {
        this.queue.poll(); // Remove self
        final SharedTaskChain next;
        synchronized (this.sharedChains) {
            next = this.queue.peek();
            if (next == null) {
                this.sharedChains.remove(this.name);
                return;
            }
            if (!next.isPending) {
                // Created but wasn't executed yet. Wait until the chain executes itself.
                return;
            }
            this.canExecute = false;
        }

        next.execute0();
    }
}
