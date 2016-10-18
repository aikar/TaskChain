/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
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
