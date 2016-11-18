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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * The Main API class of TaskChain. TaskChain's are created by a {@link TaskChainFactory}
 */
@SuppressWarnings({"unused", "FieldAccessedSynchronizedAndUnsynchronized"})
public interface TaskChain <T> {
    /* ======================================================================================== */
    // <editor-fold desc="// API Methods - Getters & Setters">
    /**
     * Called in an executing task, get the current action index.
     * For every action that adds a task to the chain, the action index is increased.
     *
     * Useful in error or done handlers to know where you are in the chain when it aborted or threw exception.
     * @return The current index
     */
    public int getCurrentActionIndex();

    /**
     * Changes the done callback handler for this chain
     * @param doneCallback The handler
     */
    @SuppressWarnings("WeakerAccess")
    public void setDoneCallback(Consumer<Boolean> doneCallback);

    /**
     * @return The current error handler or null
     */
    public BiConsumer<Exception, Task<?, ?>> getErrorHandler();

    /**
     * Changes the error handler for this chain
     * @param errorHandler The error handler
     */
    @SuppressWarnings("WeakerAccess")
    public void setErrorHandler(BiConsumer<Exception, Task<?, ?>> errorHandler);

    BaseTaskChain<T> getImplChain();
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
        return BaseTaskChain.currentChain.get();
    }

    /* ======================================================================================== */

    /**
     * Checks if the chain has a value saved for the specified key.
     * @param key Key to check if Task Data has a value for
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasTaskData(String key);

    /**
     * Retrieves a value relating to a specific key, saved by a previous task.
     *
     * @param key Key to look up Task Data for
     * @param <R> Type the Task Data value is expected to be
     */
    @SuppressWarnings("WeakerAccess")
    public <R> R getTaskData(String key);

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
    public <R> R setTaskData(String key, Object val);

    /**
     * Removes a saved value on the chain.
     *
     * @param key Key to remove from Task Data
     * @param <R> Type the Task Data value is expected to be
     */
    @SuppressWarnings("WeakerAccess")
    public <R> R removeTaskData(String key);

    /**
     * Takes the previous tasks return value, stores it to the specified key
     * as Task Data, and then forwards that value to the next task.
     *
     * @param key Key to store the previous return value into Task Data
     */
    @SuppressWarnings("WeakerAccess")
    public default TaskChain<T> storeAsData(String key) {
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
    public default <R> TaskChain<R> returnData(String key) {
        //noinspection unchecked
        return currentFirst(() -> (R) getTaskData(key));
    }

    /**
     * Returns the chain itself to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public default TaskChain<TaskChain<?>> returnChain() {
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
    public default TaskChain<T> delay(final int gameUnits) {
        //noinspection CodeBlock2Expr
        return currentCallback((input, next) -> {
            getImplChain().getGameImpl().scheduleTask(gameUnits, () -> next.accept(input));
        });
    }

    /**
     * Adds a real time delay to the chain execution.
     * Chain will abort if the delay is interrupted.
     *
     * @param duration duration of the delay before next task
     */
    @SuppressWarnings("WeakerAccess")
    public default TaskChain<T> delay(final int duration, TimeUnit unit) {
        //noinspection CodeBlock2Expr
        return currentCallback((input, next) -> {
            getImplChain().getGameImpl().scheduleTask(duration, unit, () -> next.accept(input));
        });
    }

    // </editor-fold>
    // <editor-fold desc="// API Methods - Abort">
    /**
     * Checks if the previous task return was null.
     *
     * If not null, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public default TaskChain<T> abortIfNull() {
        return abortIfNull(null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public default TaskChain<T> abortIfNull(TaskChainAbortAction<?, ?, ?> action) {
        return abortIf(null, action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public default <A1> TaskChain<T> abortIfNull(TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        //noinspection unchecked
        return abortIf(null, action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    public default <A1, A2> TaskChain<T> abortIfNull(TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        //noinspection unchecked
        return abortIf(null, action, arg1, arg2, null);
    }

    /**
     * Checks if the previous task return was null, and aborts if it was
     * Then executes supplied action handler
     *
     * If not null, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public default <A1, A2, A3> TaskChain<T> abortIfNull(TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        //noinspection unchecked
        return abortIf(null, action, arg1, arg2, arg3);
    }

    /**
     * Checks if the previous task return is the supplied value.
     *
     * If not, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    public default TaskChain<T> abortIf(T ifObj) {
        return abortIf(ifObj, null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<?, ?, ?> action) {
        return abortIf(ifObj, action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    default <A1> TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        return abortIf(ifObj, action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIf(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    default <A1, A2> TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        return abortIf(ifObj, action, arg1, arg2, null);
    }

    /**
     * Checks if the previous task return is the supplied value, and aborts if it was.
     * Then executes supplied action handler
     *
     * If not null, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    default <A1, A2, A3> TaskChain<T> abortIf(T ifObj, TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        return current((obj) -> {
            if (Objects.equals(obj, ifObj)) {
                getImplChain().handleAbortAction(action, arg1, arg2, arg3);
                return null;
            }
            return obj;
        });
    }

    /**
     * Checks if the previous task return is not the supplied value.
     *
     * If it is, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<T> abortIfNot(T ifNotObj) {
        return abortIfNot(ifNotObj, null, null, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<?, ?, ?> action) {
        return abortIfNot(ifNotObj, action, null, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    default <A1> TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<A1, ?, ?> action, A1 arg1) {
        return abortIfNot(ifNotObj, action, arg1, null, null);
    }

    /**
     * {@link TaskChain#abortIfNot(Object, TaskChainAbortAction, Object, Object, Object)}
     */
    @SuppressWarnings("WeakerAccess")
    default <A1, A2> TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<A1, A2, ?> action, A1 arg1, A2 arg2) {
        return abortIfNot(ifNotObj, action, arg1, arg2, null);
    }

    /**
     * Checks if the previous task return is the supplied value, and aborts if it was.
     * Then executes supplied action handler
     *
     * If not null, the previous task return will forward to the next task.
     */
    @SuppressWarnings("WeakerAccess")
    default <A1, A2, A3> TaskChain<T> abortIfNot(T ifNotObj, TaskChainAbortAction<A1, A2, A3> action, A1 arg1, A2 arg2, A3 arg3) {
        return current((obj) -> {
            if (!Objects.equals(obj, ifNotObj)) {
                getImplChain().handleAbortAction(action, arg1, arg2, arg3);
                return null;
            }
            return obj;
        });
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
    default <R> TaskChain<R> syncFirstCallback(AsyncExecutingFirstTask<R> task) {
        //noinspection unchecked
        
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncFirstCallback(AsyncExecutingFirstTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> asyncFirstCallback(AsyncExecutingFirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncFirstCallback(AsyncExecutingFirstTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> currentFirstCallback(AsyncExecutingFirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(null, task);
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
    default <R> TaskChain<R> syncCallback(AsyncExecutingTask<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)}, ran on main thread but no input or output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> syncCallback(AsyncExecutingGenericTask task) {
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> asyncCallback(AsyncExecutingTask<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> asyncCallback(AsyncExecutingGenericTask task) {
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> currentCallback(AsyncExecutingTask<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(null, task);
    }

    /**
     * {@link TaskChain#syncCallback(AsyncExecutingTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> currentCallback(AsyncExecutingGenericTask task) {
        return getImplChain().add0(null, task);
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
    default <R> TaskChain<R> future(CompletableFuture<R> future) {
        return currentFuture((input) -> future);
    }

    /**
     * Takes multiple supplied Futures, and holds processing of the chain until the futures completes.
     * The results of the Futures will be passed until the next task.
     *
     * @param futures The Futures to wait until it is complete on
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings({"WeakerAccess", "unchecked"})
    default <R> TaskChain<List<R>> futures(CompletableFuture<R>... futures) {
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
    default <R> TaskChain<List<R>> futures(List<CompletableFuture<R>> futures) {
        return currentFuture((input) -> getImplChain().getFuture(futures));
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
    default <R> TaskChain<List<R>> syncFutures(Task<List<CompletableFuture<R>>, T> task) {
        return syncFuture((input) -> getImplChain().getFuture(task.run(input)));
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
    default <R> TaskChain<List<R>> asyncFutures(Task<List<CompletableFuture<R>>, T> task) {
        return asyncFuture((input) -> getImplChain().getFuture(task.run(input)));
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
    default <R> TaskChain<List<R>> currentFutures(Task<List<CompletableFuture<R>>, T> task) {
        return currentFuture((input) -> getImplChain().getFuture(task.run(input)));
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
    default <R> TaskChain<List<R>> syncFirstFutures(FirstTask<List<CompletableFuture<R>>> task) {
        return syncFuture((input) -> getImplChain().getFuture(task.run()));
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
    default <R> TaskChain<List<R>> asyncFirstFutures(FirstTask<List<CompletableFuture<R>>> task) {
        return asyncFuture((input) -> getImplChain().getFuture(task.run()));
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
    default <R> TaskChain<List<R>> currentFirstFutures(FirstTask<List<CompletableFuture<R>>> task) {
        return currentFuture((input) -> getImplChain().getFuture(task.run()));
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
    default <R> TaskChain<R> syncFirstFuture(FutureFirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncFirstFuture(FutureFirstTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> asyncFirstFuture(FutureFirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncFirstFuture(FutureFirstTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> currentFirstFuture(FutureFirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(null, task);
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
    default <R> TaskChain<R> syncFuture(FutureTask<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)}, ran on main thread but no input or output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> syncFuture(FutureGenericTask task) {
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> asyncFuture(FutureTask<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> asyncFuture(FutureGenericTask task) {
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> currentFuture(FutureTask<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(null, task);
    }

    /**
     * {@link TaskChain#syncFuture(FutureTask)} but the future provider is ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> currentFuture(FutureGenericTask task) {
        return getImplChain().add0(null, task);
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
    default <R> TaskChain<R> syncFirst(FirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncFirst(FirstTask)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> asyncFirst(FirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncFirst(FirstTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> currentFirst(FirstTask<R> task) {
        //noinspection unchecked
        return getImplChain().add0(null, task);
    }

    /**
     * Execute task on main thread, with the last returned input, returning an output
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> sync(Task<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(false, task);
    }

    /**
     * Execute task on main thread, with no input or output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> sync(GenericTask task) {
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#sync(Task)} but ran off main thread
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> async(Task<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#sync(GenericTask)} but ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> async(GenericTask task) {
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#sync(Task)} but ran on current thread the Chain was created on
     * @param task The task to execute
     * @param <R> Return type that the next parameter can expect as argument type
     */
    @SuppressWarnings("WeakerAccess")
    default <R> TaskChain<R> current(Task<R, T> task) {
        //noinspection unchecked
        return getImplChain().add0(null, task);
    }

    /**
     * {@link TaskChain#sync(GenericTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> current(GenericTask task) {
        return getImplChain().add0(null, task);
    }


    /**
     * Execute task on main thread, with the last output, and no furthur output
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> syncLast(LastTask<T> task) {
        return getImplChain().add0(false, task);
    }

    /**
     * {@link TaskChain#syncLast(LastTask)} but ran off main thread
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> asyncLast(LastTask<T> task) {
        return getImplChain().add0(true, task);
    }

    /**
     * {@link TaskChain#syncLast(LastTask)} but ran on current thread the Chain was created on
     * @param task The task to execute
     */
    @SuppressWarnings("WeakerAccess")
    default TaskChain<?> currentLast(LastTask<T> task) {
        return getImplChain().add0(null, task);
    }

    /**
     * Finished adding tasks, begins executing them.
     */
    @SuppressWarnings("WeakerAccess")
    public default void execute() {
        execute((Consumer<Boolean>) null, null);
    }

    /**
     * Finished adding tasks, begins executing them with a done notifier
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     */
    @SuppressWarnings("WeakerAccess")
    public default void execute(Runnable done) {
        execute((finished) -> done.run(), null);
    }

    /**
     * Finished adding tasks, begins executing them with a done notifier and error handler
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     * @param errorHandler The Error handler to handle exceptions
     */
    @SuppressWarnings("WeakerAccess")
    public default void execute(Runnable done, BiConsumer<Exception, Task<?, ?>> errorHandler) {
        execute((finished) -> done.run(), errorHandler);
    }

    /**
     * Finished adding tasks, with a done notifier
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     */
    @SuppressWarnings("WeakerAccess")
    public default void execute(Consumer<Boolean> done) {
        execute(done, null);
    }

    /**
     * Finished adding tasks, begins executing them, with an error handler
     * @param errorHandler The Error handler to handle exceptions
     */
    public default void execute(BiConsumer<Exception, Task<?, ?>> errorHandler) {
        execute((Consumer<Boolean>) null, errorHandler);
    }

    /**
     * Finished adding tasks, begins executing them with a done notifier and error handler
     * @param done The Callback to handle when the chain has finished completion. Argument to consumer contains finish state
     * @param errorHandler The Error handler to handle exceptions
     */
    public void execute(Consumer<Boolean> done, BiConsumer<Exception, Task<?, ?>> errorHandler);

    // </editor-fold>
}
