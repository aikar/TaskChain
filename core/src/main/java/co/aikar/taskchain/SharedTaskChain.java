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


import co.aikar.taskchain.TaskChainTasks.Task;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

class SharedTaskChain<R> extends TaskChain<R> {
    private final TaskChain<R> backingChain;
    SharedTaskChain(TaskChainFactory factory, TaskChain<R> backingChain) {
        super(factory);
        this.backingChain = backingChain;
    }

    @Override
    public void execute(Consumer<Boolean> done, BiConsumer<Exception, Task<?, ?>> errorHandler) {
        synchronized (backingChain) {
            // This executes SharedTaskChain.execute(Runnable), which says execute
            // my wrapped chains queue of events, but pass a done callback for when its done.
            // We then use the backing chain callback method to not execute the next task in the
            // backing chain until the current one is fully done.
            backingChain.currentCallback((next) -> {
                this.setErrorHandler(errorHandler);
                this.setDoneCallback((finished) -> {
                    if (done != null) {
                        try {
                            done.accept(finished);
                        } catch (Exception e) {
                            if (this.getErrorHandler() != null) {
                                this.getErrorHandler().accept(e, null);
                            }
                        }
                    }
                    next.run();
                });
                this.execute0();
            });
            backingChain.execute();
        }
    }
}
