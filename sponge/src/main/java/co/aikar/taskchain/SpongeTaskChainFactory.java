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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class SpongeTaskChainFactory extends TaskChainFactory {

    private SpongeTaskChainFactory(GameInterface impl) {
        super(impl);
    }
    public static TaskChainFactory create(PluginContainer plugin) {
        return new SpongeTaskChainFactory(new SpongeGameInterface(plugin));
    }

    private static class SpongeGameInterface implements GameInterface {
        private final AsyncQueue asyncQueue;
        private final Object plugin;

        private SpongeGameInterface(PluginContainer plugin) {
            final Object pluginObject = plugin.getInstance().orElse(null);
            if (pluginObject == null) {
                throw new NullPointerException("Plugin can not be null");
            }
            this.plugin = pluginObject;
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
            Task.builder().execute(run).submit(plugin);
        }

        @Override
        public void scheduleTask(int gameUnits, Runnable run) {
            Task.builder().delayTicks(gameUnits).execute(run).submit(plugin);
        }

        @Override
        public void registerShutdownHandler(TaskChainFactory factory) {
            Sponge.getEventManager().registerListener(plugin, GameStoppingEvent.class, event -> {
                factory.shutdown(60, TimeUnit.SECONDS);
            });
        }
    }
}
