/*
 * Copyright (c) 2016-2021 Daniel Ennis (Aikar) - MIT License
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

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.plugin.PluginDisableEvent;
import cn.nukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class NukkitTaskChainFactory extends TaskChainFactory {

    private NukkitTaskChainFactory(Plugin plugin, AsyncQueue asyncQueue) {
        super(new NukkitGameInterface(plugin, asyncQueue));
    }

    public static TaskChainFactory create(Plugin plugin) {
        return new NukkitTaskChainFactory(plugin, new TaskChainAsyncQueue());
    }

    private static class NukkitGameInterface implements GameInterface {

        private final Plugin plugin;

        private final AsyncQueue asyncQueue;

        private NukkitGameInterface(Plugin plugin, AsyncQueue asyncQueue) {
            this.plugin = plugin;
            this.asyncQueue = asyncQueue;
        }

        @Override
        public boolean isMainThread() {
            return this.plugin.getServer().isPrimaryThread();
        }

        @Override
        public AsyncQueue getAsyncQueue() {
            return this.asyncQueue;
        }

        @Override
        public void postToMain(Runnable run) {
            if (this.plugin.isEnabled()) {
                this.plugin.getServer().getScheduler().scheduleTask(this.plugin, run);
                return;
            }

            run.run();
        }

        @Override
        public void scheduleTask(int gameUnits, Runnable run) {
            if (this.plugin.isEnabled()) {
                this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, run, gameUnits);
                return;
            }

            run.run();
        }

        @Override
        public void registerShutdownHandler(TaskChainFactory factory) {
            this.plugin.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPluginDisable(PluginDisableEvent event) {
                    if (event.getPlugin().equals(NukkitGameInterface.this.plugin))
                        factory.shutdown(60, TimeUnit.SECONDS);
                }
            }, this.plugin);
        }

        public static final TaskChainAbortAction<Player, String, ?> MESSAGE = new TaskChainAbortAction<Player, String, Object>() {
            @Override
            public void onAbort(TaskChain<?> chain, Player player, String message) {
                player.sendMessage(message);
            }
        };

        public static final TaskChainAbortAction<Player, String, ?> COLOR_MESSAGE = new TaskChainAbortAction<Player, String, Object>() {
            @Override
            public void onAbort(TaskChain<?> chain, Player player, String message) {
                player.sendMessage(message.replaceAll("&", "§"));
            }
        };

    }

}
