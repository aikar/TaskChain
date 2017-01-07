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

/**
 * Defines actions to perform when a chain is used with .abortIfNull
 * Override desired arguments needed to provide actions
 *
 * @deprecated Use {@link TaskChainAbortAction} instead
 * @param <A1>
 * @param <A2>
 * @param <A3>
 */
@SuppressWarnings("WeakerAccess")
@Deprecated
public interface TaskChainNullAction <A1, A2, A3> extends TaskChainAbortAction<A1, A2, A3> {
    default void onNull(TaskChain<?> chain, A1 arg1) {}
    default void onNull(TaskChain<?> chain, A1 arg1, A2 arg2) {
        onNull(chain, arg1);
    }
    default void onNull(TaskChain<?> chain, A1 arg1, A2 arg2, A3 arg3) {
        onNull(chain, arg1, arg2);
    }

    @Override
    default void onAbort(TaskChain<?> chain, A1 arg1) {
        onNull(chain, arg1);
    }

    @Override
    default void onAbort(TaskChain<?> chain, A1 arg1, A2 arg2) {
        onNull(chain, arg1, arg2);
    }

    @Override
    default void onAbort(TaskChain<?> chain, A1 arg1, A2 arg2, A3 arg3) {
        onNull(chain, arg1, arg2, arg3);
    }
}
