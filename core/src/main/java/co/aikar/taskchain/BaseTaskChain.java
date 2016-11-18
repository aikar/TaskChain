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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BaseTaskChain<T> implements TaskChain<T> {
    static final ThreadLocal<TaskChain<?>> currentChain = new ThreadLocal<>();

    private final GameInterface impl;
    private final TaskChainFactory factory;
    private final Map<String, Object> taskMap = new HashMap<>(0);
    private final ConcurrentLinkedQueue<TaskHolder<?,?>> chainQueue = new ConcurrentLinkedQueue<>();

    private int currentActionIndex = 0;
    private int actionIndex = 0;
    private boolean executed = false;
    private boolean async = false;
    private boolean done = false;

    private Object previous;
    private TaskHolder<?, ?> currentHolder;
    private Consumer<Boolean> doneCallback;
    private BiConsumer<Exception, TaskChainTasks.Task<?, ?>> errorHandler;

    /* ======================================================================================== */
    BaseTaskChain(TaskChainFactory factory) {
        this.factory = factory;
        this.impl = factory.getImplementation();
    }

    /* ======================================================================================== */

    @Override
    public BaseTaskChain<T> getImplChain() {
        return this;
    }

    GameInterface getGameImpl() {
        return impl;
    }

    // <editor-fold desc="// API Methods - Getters & Setters">
    /**
     * Called in an executing task, get the current action index.
     * For every action that adds a task to the chain, the action index is increased.
     *
     * Useful in error or done handlers to know where you are in the chain when it aborted or threw exception.
     * @return The current index
     */
    public int getCurrentActionIndex() {
        return currentActionIndex;
    }

    /**
     * Changes the done callback handler for this chain
     * @param doneCallback The handler
     */
    @SuppressWarnings("WeakerAccess")
    public void setDoneCallback(Consumer<Boolean> doneCallback) {
        this.doneCallback = doneCallback;
    }

    /**
     * @return The current error handler or null
     */
    public BiConsumer<Exception, TaskChainTasks.Task<?, ?>> getErrorHandler() {
        return errorHandler;
    }

    /**
     * Changes the error handler for this chain
     * @param errorHandler The error handler
     */
    @SuppressWarnings("WeakerAccess")
    public void setErrorHandler(BiConsumer<Exception, TaskChainTasks.Task<?, ?>> errorHandler) {
        this.errorHandler = errorHandler;
    }
    // </editor-fold>

    /* ======================================================================================== */

    /**
     * Checks if the chain has a value saved for the specified key.
     * @param key Key to check if Task Data has a value for
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasTaskData(String key) {
        return taskMap.containsKey(key);
    }

    /**
     * Retrieves a value relating to a specific key, saved by a previous task.
     *
     * @param key Key to look up Task Data for
     * @param <R> Type the Task Data value is expected to be
     */
    @SuppressWarnings("WeakerAccess")
    public <R> R getTaskData(String key) {
        //noinspection unchecked
        return (R) taskMap.get(key);
    }

    /**
     * Saves a value for this chain so that a task furthur up the chain can access it.
     *
     * Useful for passing multiple values to the next (or furthur) tasks.
     *
     * @param key Key to store in Task Data
     * @param val Value to store in Task Data
     * @param <R> Type the Task Data value is expected to be
     */
    @SuppressWarnings("WeakerAccess")
    public <R> R setTaskData(String key, Object val) {
        //noinspection unchecked
        return (R) taskMap.put(key, val);
    }

    /**
     * Removes a saved value on the chain.
     *
     * @param key Key to remove from Task Data
     * @param <R> Type the Task Data value is expected to be
     */
    @SuppressWarnings("WeakerAccess")
    public <R> R removeTaskData(String key) {
        //noinspection unchecked
        return (R) taskMap.remove(key);
    }

    /**
     * Finished adding tasks, begins executing them with a done notifier and error handler
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     * @param errorHandler The Error handler to handle exceptions
     */
    public void execute(Consumer<Boolean> done, BiConsumer<Exception, TaskChainTasks.Task<?, ?>> errorHandler) {
        if (errorHandler == null) {
            errorHandler = factory.getDefaultErrorHandler();
        }
        this.doneCallback = done;
        this.errorHandler = errorHandler;
        execute0();
    }

    /* ======================================================================================== */
    // <editor-fold desc="// Implementation Details">
    <A1, A2, A3> void handleAbortAction(TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        if (action != null) {
            final TaskChain<?> prev = currentChain.get();
            try {
                currentChain.set(this);
                action.onAbort(this, arg1, arg2, arg3);
            } catch (Exception e) {
                TaskChainUtil.logError("TaskChain Exception in Abort Action handler: " + action.getClass().getName());
                TaskChainUtil.logError("Current Action Index was: " + currentActionIndex);
                e.printStackTrace();
            } finally {
                currentChain.set(prev);
            }
        }
        TaskChain.abort();
    }

    void execute0() {
        synchronized (this) {
            if (this.executed) {
                throw new RuntimeException("Already executed");
            }
            this.executed = true;
        }
        async = !impl.isMainThread();
        nextTask();
    }

    void done(boolean finished) {
        this.done = true;
        if (this.doneCallback != null) {
            final TaskChain<?> prev = currentChain.get();
            try {
                currentChain.set(this);
                this.doneCallback.accept(finished);
            } catch (Exception e) {
                this.handleError(e, null);
            } finally {
                currentChain.set(prev);
            }
        }
    }

    <R> TaskChain<R> add0(Boolean async, TaskChainTasks.Task task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, async, task));
    }

    @SuppressWarnings({"rawtypes", "WeakerAccess"})
    <R> TaskChain<R> add0(TaskHolder<R,?> task) {
        synchronized (this) {
            if (this.executed) {
                throw new RuntimeException("TaskChain is executing");
            }
        }

        this.chainQueue.add(task);
        //noinspection unchecked
        return (TaskChain<R>) this;
    }

    /**
     * Fires off the next task, and switches between Async/Sync as necessary.
     */
    private void nextTask() {
        synchronized (this) {
            this.currentHolder = this.chainQueue.poll();
            if (this.currentHolder == null) {
                this.done = true; // to ensure its done while synchronized
            }
        }

        if (this.currentHolder == null) {
            this.previous = null;
            // All Done!
            this.done(true);
            return;
        }

        Boolean isNextAsync = this.currentHolder.async;
        if (isNextAsync == null || factory.shutdown) {
            this.currentHolder.run();
        } else if (isNextAsync) {
            if (this.async) {
                this.currentHolder.run();
            } else {
                impl.postAsync(() -> {
                    this.async = true;
                    this.currentHolder.run();
                });
            }
        } else {
            if (this.async) {
                impl.postToMain(() -> {
                    this.async = false;
                    this.currentHolder.run();
                });
            } else {
                this.currentHolder.run();
            }
        }
    }

    private void handleError(Throwable throwable, TaskChainTasks.Task<?, ?> task) {
        Exception e = throwable instanceof Exception ? (Exception) throwable : new Exception(throwable);
        if (errorHandler != null) {
            final TaskChain<?> prev = currentChain.get();
            try {
                currentChain.set(this);
                errorHandler.accept(e, task);
            } catch (Exception e2) {
                TaskChainUtil.logError("TaskChain Exception in the error handler!" + e2.getMessage());
                TaskChainUtil.logError("Current Action Index was: " + currentActionIndex);
                e.printStackTrace();
            } finally {
                currentChain.set(prev);
            }
        } else {
            TaskChainUtil.logError("TaskChain Exception on " + (task != null ? task.getClass().getName() : "Done Hander") + ": " + e.getMessage());
            TaskChainUtil.logError("Current Action Index was: " + currentActionIndex);
            e.printStackTrace();
        }
    }

    private void abortChain() {
        this.previous = null;
        this.chainQueue.clear();
        this.done(false);
    }

    <R> CompletableFuture<List<R>> getFuture(List<CompletableFuture<R>> futures) {
        CompletableFuture<List<R>> onDone = new CompletableFuture<>();
        CompletableFuture<?>[] futureArray = new CompletableFuture<?>[futures.size()];
        CompletableFuture.allOf((CompletableFuture<?>[]) futures.toArray(futureArray)).whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                onDone.completeExceptionally(throwable);
            } else {
                boolean[] error = {false};
                final List<R> results = futures.stream().map(f -> {
                    try {
                        return f.join();
                    } catch (Exception e) {
                        error[0] = true;
                        BaseTaskChain.this.handleError(e, BaseTaskChain.this.currentHolder.task);
                        return null;
                    }
                }).collect(Collectors.toList());
                if (error[0]) {
                    onDone.completeExceptionally(new Exception("Future Dependant had an exception"));
                } else {
                    onDone.complete(results);
                }
            }
        });
        return onDone;
    }

    // </editor-fold>
    /* ======================================================================================== */
    // <editor-fold desc="// TaskHolder">
    /**
     * Provides foundation of a task with what the previous task type should return
     * to pass to this and what this task will return.
     * @param <R> Return Type
     * @param <A> Argument Type Expected
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private static class TaskHolder<R, A> {
        private final BaseTaskChain<A> chain;
        private final TaskChainTasks.Task<R, A> task;
        final Boolean async;

        private boolean executed = false;
        private boolean aborted = false;
        private final int actionIndex;

        TaskHolder(BaseTaskChain<A> chain, Boolean async, TaskChainTasks.Task<R, A> task) {
            this.actionIndex = chain.actionIndex++;
            this.task = task;
            this.chain = chain;
            this.async = async;
        }

        /**
         * Called internally by Task Chain to facilitate executing the task and then the next task.
         */
        private void run() {
            final Object arg = this.chain.previous;
            this.chain.previous = null;
            this.chain.currentActionIndex = this.actionIndex;
            final TaskChain<?> prevChain = currentChain.get();
            try {
                currentChain.set(this.chain);
                if (this.task instanceof TaskChainTasks.FutureTask) {
                    //noinspection unchecked
                    final CompletableFuture<R> future = ((TaskChainTasks.FutureTask<R, A>) this.task).runFuture((A) arg);
                    if (future == null) {
                        throw new NullPointerException("Must return a Future");
                    }
                    future.whenComplete((r, throwable) -> {
                        if (throwable != null) {
                            this.chain.handleError(throwable, this.task);
                            this.abort();
                        } else {
                            this.next(r);
                        }
                    });
                } else if (this.task instanceof TaskChainTasks.AsyncExecutingTask) {
                    //noinspection unchecked
                    ((TaskChainTasks.AsyncExecutingTask<R, A>) this.task).runAsync((A) arg, this::next);
                } else {
                    //noinspection unchecked
                    next(this.task.run((A) arg));
                }
            } catch (Throwable e) {
                //noinspection ConstantConditions
                if (e instanceof AbortChainException) {
                    this.abort();
                    return;
                }
                this.chain.handleError(e, this.task);
                this.abort();
            } finally {
                if (prevChain != null) {
                    currentChain.set(prevChain);
                } else {
                    currentChain.remove();
                }
            }
        }

        /**
         * Abort the chain, and clear tasks for GC.
         */
        private synchronized void abort() {
            this.aborted = true;
            this.chain.abortChain();
        }

        /**
         * Accepts result of previous task and executes the next
         */
        private void next(Object resp) {
            synchronized (this) {
                if (this.aborted) {
                    this.chain.done(false);
                    return;
                }
                if (this.executed) {
                    this.chain.done(false);
                    throw new RuntimeException("This task has already been executed.");
                }
                this.executed = true;
            }

            this.chain.async = !this.chain.impl.isMainThread(); // We don't know where the task called this from.
            this.chain.previous = resp;
            this.chain.nextTask();
        }
    }
    // </editor-fold>
}
