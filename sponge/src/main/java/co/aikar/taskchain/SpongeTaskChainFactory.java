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

public class SpongeTaskChainFactory extends TaskChainFactory {

    private SpongeTaskChainFactory(GameInterface impl) {
        super(impl);
    }
    public static TaskChainFactory create(Object plugin) {
        // TODO: Ensure it's a plugin?
        return new SpongeTaskChainFactory(new SpongeGameInterface(plugin));
    }

    private static class SpongeGameInterface implements GameInterface {
        private final AsyncQueue asyncQueue;

        public SpongeGameInterface(Object plugin) {
            this.asyncQueue = new TaskChainAsyncQueue();
        }

        @Override
        public boolean isMainThread() {
            return false;
        }

        @Override
        public AsyncQueue getAsyncQueue() {
            return asyncQueue;
        }

        @Override
        public void postToMain(Runnable run) {

        }

        @Override
        public void scheduleTask(int gameUnits, Runnable run) {

        }

        @Override
        public void registerShutdownHandler(TaskChainFactory factory) {

        }
    }
}
