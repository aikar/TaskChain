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
