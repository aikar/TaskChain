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

/*
 * TaskChain for Minecraft Plugins
 *
 * Written by Aikar <aikar@aikar.co>
 * https://aikar.co
 * https://starlis.com
 *
 * @license MIT
 */

package co.aikar.taskchain;

import co.aikar.taskchain.TaskChainTasks.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * The Main API class of TaskChain. TaskChain's are created by a {@link TaskChainFactory}
 */
@SuppressWarnings({"unused", "FieldAccessedSynchronizedAndUnsynchronized"})
public class TaskChain <T> {
    private static final ThreadLocal<TaskChain<?>> currentChain = new ThreadLocal<>();

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
    private BiConsumer<Exception, Task<?, ?>> errorHandler;

    /* ======================================================================================== */
    TaskChain(TaskChainFactory factory) {
        this.factory = factory;
        this.impl = factory.getImplementation();
    }
    /* ======================================================================================== */
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
    public BiConsumer<Exception, Task<?, ?>> getErrorHandler() {
        return errorHandler;
    }

    /**
     * Changes the error handler for this chain
     * @param errorHandler The error handler
     */
    @SuppressWarnings("WeakerAccess")
    public void setErrorHandler(BiConsumer<Exception, Task<?, ?>> errorHandler) {
        this.errorHandler = errorHandler;
    }
    // </editor-fold>
    /* ======================================================================================== */
    // <editor-fold desc="// API Methods - Data Wrappers">

    /**
     * Creates a data wrapper to return multiple objects from a task
     */
    public static <D1, D2> TaskChainDataWrappers.Data2<D1, D2> multi(D1 var1, D2 var2) {
        return new TaskChainDataWrappers.Data2<>(var1, var2);
    }

    /**
     * Creates a data wrapper to return multiple objects from a task
     */
    public static <D1, D2, D3> TaskChainDataWrappers.Data3<D1, D2, D3> multi(D1 var1, D2 var2, D3 var3) {
        return new TaskChainDataWrappers.Data3<>(var1, var2, var3);
    }

    /**
     * Creates a data wrapper to return multiple objects from a task
     */
    public static <D1, D2, D3, D4> TaskChainDataWrappers.Data4<D1, D2, D3, D4> multi(D1 var1, D2 var2, D3 var3, D4 var4) {
        return new TaskChainDataWrappers.Data4<>(var1, var2, var3, var4);
    }

    /**
     * Creates a data wrapper to return multiple objects from a task
     */
    public static <D1, D2, D3, D4, D5> TaskChainDataWrappers.Data5<D1, D2, D3, D4, D5> multi(D1 var1, D2 var2, D3 var3, D4 var4, D5 var5) {
        return new TaskChainDataWrappers.Data5<>(var1, var2, var3, var4, var5);
    }

    /**
     * Creates a data wrapper to return multiple objects from a task
     */
    public static <D1, D2, D3, D4, D5, D6> TaskChainDataWrappers.Data6<D1, D2, D3, D4, D5, D6> multi(D1 var1, D2 var2, D3 var3, D4 var4, D5 var5, D6 var6) {
        return new TaskChainDataWrappers.Data6<>(var1, var2, var3, var4, var5, var6);
    }
    // </editor-fold>
    /* ======================================================================================== */

    // <editor-fold desc="// API Methods - Base">
    /**
     * Call to abort execution of the chain. Should be called inside of an executing task.
     */
    @SuppressWarnings("WeakerAccess")
    public static void abort() {
        TaskChainUtil.sneakyThrows(new AbortChainException());
    }

    /**
     * Usable only inside of an executing Task or Chain Error/Done handlers
     *
     * Gets the current chain that is executing this Task or Error/Done handler
     * This method should only be called on the same thread that is executing the method.
     *
     * In an AsyncExecutingTask or a FutureTask, You must call this method BEFORE passing control to another thread.
     */
    @SuppressWarnings("WeakerAccess")
    public static TaskChain<?> getCurrentChain() {
        return currentChain.get();
    }

    /* ======================================================================================== */

    /**
     * Allows you to call a callback to insert tasks into the chain without having to break the fluent interface
     *
     * Example: Plugin.newChain().sync(some::task).configure(chain -> {
     *     chain.async(some::foo);
     *     chain.sync(other::bar);
     * }).async(other::task).execute();
     *
     * @param configure Instance of the current chain.
     * @return The same chain
     */
    public TaskChain<T> configure(Consumer<TaskChain<T>> configure) {
        configure.accept(this);
        return this;
    }

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
     * Takes the previous tasks return value, stores it to the specified key
     * as Task Data, and then forwards that value to the next task.
     *
     * @param key Key to store the previous return value into Task Data
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> storeAsData(String key) {
        return current((val) -> {
            setTaskData(key, val);
            return val;
        });
    }

    /**
     * Reads the specified key from Task Data, and passes it to the next task.
     *
     * Will need to pass expected type such as chain.&lt;Foo&gt;returnData("key")
     *
     * @param key Key to retrieve from Task Data and pass to next task
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> returnData(String key) {
        //noinspection unchecked
        return currentFirst(() -> (R) getTaskData(key));
    }

    /**
     * Returns the chain itself to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<TaskChain<?>> returnChain() {
        return currentFirst(() -> this);
    }


    /**
     * IMPLEMENTATION SPECIFIC!!
     * Consult your application implementation to understand how long 1 unit is.
     *
     * For example, in Minecraft it is a tick, which is roughly 50 milliseconds, but not guaranteed.
     *
     * Adds a delay to the chain execution.
     *
     * @param gameUnits # of game units to delay before next task
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> delay(final int gameUnits) {
        //noinspection CodeBlock2Expr
        return currentCallback((input, next) -> {
            impl.scheduleTask(gameUnits, () -> next.accept(input));
        });
    }

    /**
     * Adds a real time delay to the chain execution.
     * Chain will abort if the delay is interrupted.
     *
     * @param duration duration of the delay before next task
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> delay(final int duration, TimeUnit unit) {
        //noinspection CodeBlock2Expr
        return currentCallback((input, next) -> {
            impl.scheduleTask(duration, unit, () -> next.accept(input));
        });
    }

    // </editor-fold>
    // <editor-fold desc="// API Methods - Abort">

    /**
     * Aborts the chain once this step is reached. This is primarily useful when you are
     * dynamically building the chains steps (such as .configure()) and want to conditionally stop the
     * chain from proceeding.
     *
     * @return Chain
     */
    public TaskChain<?> abortChain() {
        if (executed) {
            TaskChain.abort();
            return this;
        } else {
            return current(TaskChain::abort);
        }
    }

    /**
     * Checks if the previous task return was null.
     *
     * If not null, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIfNull() {
        return abortIfNull(null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIfNull(TaskChainAbortAction<?, ?, ?> action) {
        return abortIf(Predicate.isEqual(null), action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1> TaskChain<T> abortIfNull(TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        //noinspection unchecked
        return abortIf(Predicate.isEqual(null), action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2> TaskChain<T> abortIfNull(TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        //noinspection unchecked
        return abortIf(Predicate.isEqual(null), action, arg1, arg2, null);
    }

    /**
     * Checks if the previous task return was null, and aborts if it was
     * Then executes supplied action handler
     *
     * If not null, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2, A3> TaskChain<T> abortIfNull(TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        //noinspection unchecked
        return abortIf(Predicate.isEqual(null), action, arg1, arg2, arg3);
    }

    /**
     * Checks if the previous task return is the supplied value.
     *
     * If not, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIf(T ifObj) {
        return abortIf(ifObj, null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<?, ?, ?> action) {
        return abortIf(ifObj, action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1> TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        return abortIf(ifObj, action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2> TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        return abortIf(ifObj, action, arg1, arg2, null);
    }

    /**
     * {@link TaskChain#abortIf(Predicate, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2, A3> TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        return abortIf(Predicate.isEqual(ifObj), action, arg1, arg2, arg3);
    }
    
    /**
     * Checks if the previous task return matches the supplied predicate, and aborts if it was.
     * 
     * If predicate does not match, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIf(Predicate<T> predicate) {
        return abortIf(predicate, null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIf(Predicate<T> predicate, TaskChainAbortAction<?, ?, ?> action) {
        return abortIf(predicate, action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1> TaskChain<T> abortIf(Predicate<T> predicate, TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        return abortIf(predicate, action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2> TaskChain<T> abortIf(Predicate<T> predicate, TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        return abortIf(predicate, action, arg1, arg2, null);
    }

    /**
     * Checks if the previous task return matches the supplied predicate, and aborts if it was.
     * Then executes supplied action handler
     * 
     * If predicate does not match, the previous task return will forward to the next task.
     */
    public <A1, A2, A3> TaskChain<T> abortIf(Predicate<T> predicate, TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        return current((obj) -> {
           if (predicate.test(obj)) {
               handleAbortAction(action, arg1, arg2, arg3);
               return null;
           }
           return obj;
        });
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIfNot(T ifNotObj) {
        return abortIfNot(ifNotObj, null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<?, ?, ?> action) {
        return abortIfNot(ifNotObj, action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1> TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        return abortIfNot(ifNotObj, action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2> TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        return abortIfNot(ifNotObj, action, arg1, arg2, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Predicate, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2, A3> TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        return abortIfNot(Predicate.<T>isEqual(ifNotObj), action, arg1, arg2, arg3);
    }
    
    /**
     * Checks if the previous task return does NOT match the supplied predicate, and aborts if it does not match.
     * 
     * If predicate matches, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIfNot(Predicate<T> ifNotPredicate) {
        return abortIfNot(ifNotPredicate, null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<T> abortIfNot(Predicate<T> ifNotPredicate, TaskChainAbortAction<?, ?, ?> action) {
        return abortIfNot(ifNotPredicate, action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1> TaskChain<T> abortIfNot(Predicate<T> ifNotPredicate, TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        return abortIfNot(ifNotPredicate, action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2> TaskChain<T> abortIfNot(Predicate<T> ifNotPredicate, TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        return abortIfNot(ifNotPredicate, action, arg1, arg2, null);
    }

    /**
     * Checks if the previous task return does NOT match the supplied predicate, and aborts if it does not match.
     * Then executes supplied action handler
     * 
     * If predicate matches, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public <A1, A2, A3> TaskChain<T> abortIfNot(Predicate<T> ifNotPredicate, TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        return abortIf(ifNotPredicate.negate(), action, arg1, arg2, arg3);
    }

    // </editor-fold>
    // <editor-fold desc="// API Methods - Async Executing">
    /* ======================================================================================== */
    // Async Executing Tasks
    /* ======================================================================================== */

    /**
     * Execute a task on the main thread, with no previous input, and a callback to return the response to.
     *
     * It's important you don't perform blocking operations in this method. Only use this if
     * the task will be scheduling a different sync operation outside of the TaskChains scope.
     *
     * Usually you could achieve the same design with a blocking API by switching to an async task
     * for the next task and running it there.
     *
     * This method would primarily be for cases where you need to use an API that ONLY provides
     * a callback style API.
     *
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> syncFirstCallback(AsyncExecutingFirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncFirstCallback(AsyncExecutingFirstTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> asyncFirstCallback(AsyncExecutingFirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncFirstCallback(AsyncExecutingFirstTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> currentFirstCallback(AsyncExecutingFirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, null, task));
    }

    /**
     * Execute a task on the main thread, with the last output, and a callback to return the response to.
     *
     * It's important you don't perform blocking operations in this method. Only use this if
     * the task will be scheduling a different sync operation outside of the TaskChains scope.
     *
     * Usually you could achieve the same design with a blocking API by switching to an async task
     * for the next task and running it there.
     *
     * This method would primarily be for cases where you need to use an API that ONLY provides
     * a callback style API.
     *
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> syncCallback(AsyncExecutingTask<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)}, ran on main thread but no input or output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> syncCallback(AsyncExecutingGenericTask task) {
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> asyncCallback(AsyncExecutingTask<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> asyncCallback(AsyncExecutingGenericTask task) {
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> currentCallback(AsyncExecutingTask<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, null, task));
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> currentCallback(AsyncExecutingGenericTask task) {
        return add0(new TaskHolder<>(this, null, task));
    }

    // </editor-fold>
    // <editor-fold desc="// API Methods - Future">
    /* ======================================================================================== */
    // Future Tasks
    /* ======================================================================================== */

    /**
     * Takes a supplied Future, and holds processing of the chain until the future completes.
     * The value of the Future will be passed until the next task.
     *
     * @param future The Future to wait until it is complete on
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> future(CompletableFuture<R> future) {
        return currentFuture((input) -> future);
    }

    /**
     * Takes multiple supplied Futures, and holds processing of the chain until the futures completes.
     * The results of the Futures will be passed until the next task.
     *
     * @param futures The Futures to wait until it is complete on
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SafeVarargs
    @SuppressWarnings("WeakerAccess")
    public final <R> TaskChain<List<R>> futures(CompletableFuture<R>... futures) {
        List<CompletableFuture<R>> futureList = new ArrayList<>(futures.length);
        Collections.addAll(futureList, futures);
        return futures(futureList);
    }

    /**
     * Takes multiple supplied Futures, and holds processing of the chain until the futures completes.
     * The results of the Futures will be passed until the next task.
     *
     * @param futures The Futures to wait until it is complete on
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<List<R>> futures(List<CompletableFuture<R>> futures) {
        return currentFuture((input) -> getFuture(futures));
    }

    /**
     * Executes a Task on the Main thread that provides a list of Futures, and holds processing
     * of the chain until all of the futures completes.
     *
     * The response of every future will be passed to the next task as a List, in the order
     * the futures were supplied.
     *
     * @param task The Futures Provider Task
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<List<R>> syncFutures(Task<List<CompletableFuture<R>>, T> task) {
        return syncFuture((input) -> getFuture(task.run(input)));
    }

    /**
     * Executes a Task off the Main thread that provides a list of Futures, and holds processing
     * of the chain until all of the futures completes.
     *
     * The response of every future will be passed to the next task as a List, in the order
     * the futures were supplied.
     *
     * @param task The Futures Provider Task
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<List<R>> asyncFutures(Task<List<CompletableFuture<R>>, T> task) {
        return asyncFuture((input) -> getFuture(task.run(input)));
    }

    /**
     * Executes a Task on the current thread that provides a list of Futures, and holds processing
     * of the chain until all of the futures completes.
     *
     * The response of every future will be passed to the next task as a List, in the order
     * the futures were supplied.
     *
     * @param task The Futures Provider Task
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<List<R>> currentFutures(Task<List<CompletableFuture<R>>, T> task) {
        return currentFuture((input) -> getFuture(task.run(input)));
    }

    /**
     * Executes a Task on the Main thread that provides a list of Futures, and holds processing
     * of the chain until all of the futures completes.
     *
     * The response of every future will be passed to the next task as a List, in the order
     * the futures were supplied.
     *
     * @param task The Futures Provider Task
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<List<R>> syncFirstFutures(FirstTask<List<CompletableFuture<R>>> task) {
        return syncFuture((input) -> getFuture(task.run()));
    }

    /**
     * Executes a Task off the Main thread that provides a list of Futures, and holds processing
     * of the chain until all of the futures completes.
     *
     * The response of every future will be passed to the next task as a List, in the order
     * the futures were supplied.
     *
     * @param task The Futures Provider Task
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<List<R>> asyncFirstFutures(FirstTask<List<CompletableFuture<R>>> task) {
        return asyncFuture((input) -> getFuture(task.run()));
    }

    /**
     * Executes a Task on the current thread that provides a list of Futures, and holds processing
     * of the chain until all of the futures completes.
     *
     * The response of every future will be passed to the next task as a List, in the order
     * the futures were supplied.
     *
     * @param task The Futures Provider Task
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<List<R>> currentFirstFutures(FirstTask<List<CompletableFuture<R>>> task) {
        return currentFuture((input) -> getFuture(task.run()));
    }

    /**
     * Execute a task on the main thread, with no previous input, that will return a Future to signal completion
     *
     * It's important you don't perform blocking operations in this method. Only use this if
     * the task will be scheduling a different async operation outside of the TaskChains scope.
     *
     *
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> syncFirstFuture(FutureFirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncFirstFuture(FutureFirstTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> asyncFirstFuture(FutureFirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncFirstFuture(FutureFirstTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> currentFirstFuture(FutureFirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, null, task));
    }

    /**
     * Execute a task on the main thread, with the last output as the input to the future provider,
     * that will return a Future to signal completion.
     *
     * It's important you don't perform blocking operations in this method. Only use this if
     * the task will be scheduling a different async operation outside of the TaskChains scope.
     *
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> syncFuture(FutureTask<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)}, ran on main thread but no input or output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> syncFuture(FutureGenericTask task) {
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> asyncFuture(FutureTask<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> asyncFuture(FutureGenericTask task) {
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> currentFuture(FutureTask<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, null, task));
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> currentFuture(FutureGenericTask task) {
        return add0(new TaskHolder<>(this, null, task));
    }

    // </editor-fold>
    // <editor-fold desc="// API Methods - Normal">
    /* ======================================================================================== */
    // Normal Tasks
    /* ======================================================================================== */

    /**
     * Execute task on main thread, with no input, returning an output
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> syncFirst(FirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncFirst(FirstTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> asyncFirst(FirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncFirst(FirstTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> currentFirst(FirstTask<R> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, null, task));
    }

    /**
     * Execute task on main thread, with the last returned input, returning an output
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> sync(Task<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * Execute task on main thread, with no input or output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> sync(GenericTask task) {
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#sync(Task)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> async(Task<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#sync(GenericTask)} but ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> async(GenericTask task) {
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#sync(Task)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    public <R> TaskChain<R> current(Task<R, T> task) {
        //noinspection unchecked
        return add0(new TaskHolder<>(this, null, task));
    }

    /**
     * {@link TaskChain#sync(GenericTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> current(GenericTask task) {
        return add0(new TaskHolder<>(this, null, task));
    }


    /**
     * Execute task on main thread, with the last output, and no furthur output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> syncLast(LastTask<T> task) {
        return add0(new TaskHolder<>(this, false, task));
    }

    /**
     * {@link TaskChain#syncLast(LastTask)} but ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> asyncLast(LastTask<T> task) {
        return add0(new TaskHolder<>(this, true, task));
    }

    /**
     * {@link TaskChain#syncLast(LastTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    public TaskChain<?> currentLast(LastTask<T> task) {
        return add0(new TaskHolder<>(this, null, task));
    }

    /**
     * Finished adding tasks, begins executing them.
     */
    @SuppressWarnings("WeakerAccess")
    public void execute() {
        execute((Consumer<Boolean>) null, null);
    }

    /**
     * Finished adding tasks, begins executing them with a done notifier
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     */
    @SuppressWarnings("WeakerAccess")
    public void execute(Runnable done) {
        execute((finished) -> done.run(), null);
    }

    /**
     * Finished adding tasks, begins executing them with a done notifier and error handler
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     * @param errorHandler The Error handler to handle exceptions
     */
    @SuppressWarnings("WeakerAccess")
    public void execute(Runnable done, BiConsumer<Exception, Task<?, ?>> errorHandler) {
        execute((finished) -> done.run(), errorHandler);
    }

    /**
     * Finished adding tasks, with a done notifier
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     */
    @SuppressWarnings("WeakerAccess")
    public void execute(Consumer<Boolean> done) {
        execute(done, null);
    }

    /**
     * Finished adding tasks, begins executing them, with an error handler
     * @param errorHandler The Error handler to handle exceptions
     */
    public void execute(BiConsumer<Exception, Task<?, ?>> errorHandler) {
        execute((Consumer<Boolean>) null, errorHandler);
    }

    /**
     * Finished adding tasks, begins executing them with a done notifier and error handler
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     * @param errorHandler The Error handler to handle exceptions
     */
    public void execute(Consumer<Boolean> done, BiConsumer<Exception, Task<?, ?>> errorHandler) {
        if (errorHandler == null) {
            errorHandler = factory.getDefaultErrorHandler();
        }
        this.doneCallback = done;
        this.errorHandler = errorHandler;
        execute0();
    }

    // </editor-fold>
    /* ======================================================================================== */
    // <editor-fold desc="// Implementation Details">
    private <A1, A2, A3> void handleAbortAction(TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
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
        abort();
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

    @SuppressWarnings({"rawtypes", "WeakerAccess"})
    protected TaskChain add0(TaskHolder<?,?> task) {
        synchronized (this) {
            if (this.executed) {
                throw new RuntimeException("TaskChain is executing");
            }
        }

        this.chainQueue.add(task);
        return this;
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

    private void handleError(Throwable throwable, Task<?, ?> task) {
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

    private void abortExecutingChain() {
        this.previous = null;
        this.chainQueue.clear();
        this.done(false);
    }

    private <R> CompletableFuture<List<R>> getFuture(List<CompletableFuture<R>> futures) {
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
                        TaskChain.this.handleError(e, TaskChain.this.currentHolder.task);
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
    private class TaskHolder<R, A> {
        private final TaskChain<?> chain;
        private final Task<R, A> task;
        final Boolean async;

        private boolean executed = false;
        private boolean aborted = false;
        private final int actionIndex;

        private TaskHolder(TaskChain<?> chain, Boolean async, Task<R, A> task) {
            this.actionIndex = TaskChain.this.actionIndex++;
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
            TaskChain.this.currentActionIndex = this.actionIndex;
            final R res;
            final TaskChain<?> prevChain = currentChain.get();
            try {
                currentChain.set(this.chain);
                if (this.task instanceof FutureTask) {
                    //noinspection unchecked
                    final CompletableFuture<R> future = ((FutureTask<R, A>) this.task).runFuture((A) arg);
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
                } else if (this.task instanceof AsyncExecutingTask) {
                    //noinspection unchecked
                    ((AsyncExecutingTask<R, A>) this.task).runAsync((A) arg, this::next);
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
            this.chain.abortExecutingChain();
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

            this.chain.async = !TaskChain.this.impl.isMainThread(); // We don't know where the task called this from.
            this.chain.previous = resp;
            this.chain.nextTask();
        }
    }
    // </editor-fold>
}
