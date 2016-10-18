/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.taskchain;

import java.util.function.Consumer;

public class TaskChainTasks {
    /**
     * Generic task with synchronous return (but may execute on any thread)
     * @param <R>
     * @param <A>
     */
    @SuppressWarnings("WeakerAccess")
    public interface Task <R, A> {
        /**
         * Gets the current chain that is executing this task. This method should only be called on the same thread
         * that is executing the task.
         * @return
         */
        public default TaskChain<?> getCurrentChain() {
            return TaskChain.getCurrentChain();
        }

        R run(A input) throws Exception;
    }

    @SuppressWarnings("WeakerAccess")
    public interface AsyncExecutingTask<R, A> extends Task<R, A> {
        /**
         * Gets the current chain that is executing this task. This method should only be called on the same thread
         * that is executing the task.
         *
         * Since this is an AsyncExecutingTask, You must call this method BEFORE passing control to another thread.
         * @return
         */
        default TaskChain<?> getCurrentChain() {
            return TaskChain.getCurrentChain();
        }

        @Override
        default R run(A input) throws Exception {
            // unused
            return null;
        }

        void runAsync(A input, Consumer<R> next) throws Exception;
    }

    @SuppressWarnings("WeakerAccess")
    public interface FirstTask <R> extends Task<R, Object> {
        @Override
        default R run(Object input) throws Exception {
            return run();
        }

        R run() throws Exception;
    }

    @SuppressWarnings("WeakerAccess")
    public interface AsyncExecutingFirstTask<R> extends AsyncExecutingTask<R, Object> {
        @Override
        default R run(Object input) throws Exception {
            // Unused
            return null;
        }

        @Override
        default void runAsync(Object input, Consumer<R> next) throws Exception {
            run(next);
        }

        void run(Consumer<R> next) throws Exception;
    }

    @SuppressWarnings("WeakerAccess")
    public interface LastTask <A> extends Task<Object, A> {
        @Override
        default Object run(A input) throws Exception {
            runLast(input);
            return null;
        }
        void runLast(A input) throws Exception;
    }

    @SuppressWarnings("WeakerAccess")
    public interface GenericTask extends Task<Object, Object> {
        @Override
        default Object run(Object input) throws Exception {
            runGeneric();
            return null;
        }
        void runGeneric() throws Exception;
    }

    @SuppressWarnings("WeakerAccess")
    public interface AsyncExecutingGenericTask extends AsyncExecutingTask<Object, Object> {
        @Override
        default Object run(Object input) throws Exception {
            return null;
        }
        @Override
        default void runAsync(Object input, Consumer<Object> next) throws Exception {
            run(() -> next.accept(null));
        }

        void run(Runnable next) throws Exception;
    }
}
