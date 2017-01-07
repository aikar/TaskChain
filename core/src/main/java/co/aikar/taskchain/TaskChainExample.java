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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TaskChainExample {
    /**
     * A useless example of registering multiple task signatures and states
     */
    public static void example(TaskChainFactory factory) {
        TaskChainUtil.log("Starting example");
        final AsyncQueue asyncQueue = factory.getImplementation().getAsyncQueue();

        CompletableFuture<Integer> f1 = new CompletableFuture<>(); // 5
        CompletableFuture<Integer> f2 = new CompletableFuture<>(); // 3
        CompletableFuture<Integer> f3 = new CompletableFuture<>(); // 8

        TaskChain<?> chain = factory.newSharedChain("TEST");
        chain
            .delay(20 * 3)
            .sync(() -> {
                chain.setTaskData("test", 1);
                TaskChainUtil.log("==> 1st test - completing f3 with 8");
                f3.complete(8);
            })
            .delay(20)
            .async(() -> {
                Object test = chain.getTaskData("test");
                TaskChainUtil.log("==> 2nd test: " + test + " = should be 1");
            })
            .sync(TaskChain::abort)
            .execute((finished) -> TaskChainUtil.log("first test finished: " + finished));

        CompletableFuture<String> falready = new CompletableFuture<>();
        falready.complete("FOO!");
        factory.newChain()
                .current(() -> TaskChainUtil.log("Starting Future task"))
                .futures(f1, f2, f3)
                .syncFutures((lists) -> {
                    final Integer sum = lists.stream().collect(Collectors.summingInt(i -> i));
                    TaskChainUtil.log("Future complete! got " + lists.size() + " answers that sum to (expect 16): " + sum);
                    List<CompletableFuture<String>> ret = new ArrayList<>();
                    CompletableFuture<String> other = new CompletableFuture<>();
                    asyncQueue.postAsync(() -> other.complete("Result: " + sum));
                    ret.add(falready);
                    ret.add(other);
                    return ret;
                })
                .currentLast((results) -> {
                    TaskChainUtil.log("Results from last future task");
                    for (String result : results) {
                        TaskChainUtil.log(result);
                    }
                })
                .execute();

        // This chain essentially appends onto the previous one, and will not overlap
        asyncQueue.postAsync(() -> {
            TaskChain<?> chain2 = factory.newSharedChain("TEST");
            chain2
                .sync(() -> {
                    Object test = chain2.getTaskData("test");
                    TaskChainUtil.log("==> 3rd test: " + test + " = should be null");
                })
                .delay(20)
                .current(() -> {
                    TaskChainUtil.log("test 2nd chain 20 ticks later - completing f2 with 3");
                    f2.complete(3);
                })
                .execute((finished) -> TaskChainUtil.log("second test finished: " + finished));

            factory
                .newSharedChain("TEST")
                .async(() -> TaskChainUtil.log("==> 4th test - should print"))
                .returnData("notthere")
                .abortIfNull()
                .syncLast((val) -> TaskChainUtil.log("Shouldn't execute due to null abort"))
                .execute(() -> TaskChainUtil.log("finished runnable based test"));
        });
        factory
            .newSharedChain("TEST2")
            .delay(20 * 3)
            .sync(() -> TaskChainUtil.log("this should run at same time as 1st test"))
            .delay(20)
            .async(() -> TaskChainUtil.log("this should run at same time as 2nd test"))
            .execute((finished) -> TaskChainUtil.log("TEST2 finished: " + finished));
        factory.newChain()
            .asyncFirst(() -> TaskChain.multi(42, "Foo", 4.32F))
            .asyncLast((d) -> {
                throw new RuntimeException("Got " + d.var1 +", " + d.var2 + ", " + d.var3);
            })
            .execute((finished) -> TaskChainUtil.log("Finished error chain: " + finished), (e, task) -> {
                TaskChainUtil.logError("Got Exception on task " + task.getClass().getName() + ":" + e.getMessage());
            });
        factory
            .newChain()
            .sync(() -> TaskChainUtil.log("THE FIRST!"))
            .delay(20 * 10) // Wait 20s to start any task
            .async(() -> TaskChainUtil.log("This ran async - with no input or return"))
            .<Integer>asyncFirstCallback(next -> {
                // Use a callback to provide result
                TaskChainUtil.log("this also ran async, but will call next task in 3 seconds.");
                factory.getImplementation().scheduleTask(60, () -> {
                    TaskChainUtil.log("completing f1 with 5");
                    f1.complete(5);
                    next.accept(3);
                });
            })
            .sync(input -> { // Will be ran 3s later but didn't use .delay()
                TaskChainUtil.log("should of got 3: " + input);
                return 5 + input;
            })
            .asyncFuture((foo) -> {
                CompletableFuture<Integer> future = new CompletableFuture<>();
                asyncQueue.postAsync(() -> {
                    TaskChainUtil.log("Sleeping 1 before completing future");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    future.complete(foo + 10);
                });
                TaskChainUtil.log("Returning Future");
                return future;
            })
            .storeAsData("Test1")
            .syncLast(input2 -> TaskChainUtil.log("should be 18: " + input2)) // Consumes last result, but doesn't pass a new one
            .delay(20) // Wait 1s until next
            .sync(() -> TaskChainUtil.log("Generic 1s later")) // no input expected, no output, run sync
            .asyncFirst(() -> 3) // Run task async and return 3
            .delay(5 * 20) // Wait 5s
            .asyncLast(input1 -> TaskChainUtil.log("async last value 5s later should be 3: " + input1)) // Run async again, with value of 3
            .<Integer>returnData("Test1")
            .asyncLast((val) -> TaskChainUtil.log("Should of got 18 back from data: " + val))
            .<Integer>returnData("Test1")
            .abortIf(18)
            .sync(() -> TaskChainUtil.log("Shouldn't be called"))
            .execute((finished) -> TaskChainUtil.log("final test chain finished: " + finished));


    }
}
