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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class BukkitTaskChainFactory extends TaskChainFactory {
    private BukkitTaskChainFactory(Plugin plugin) {
        super(new BukkitGameInterface(plugin));
    }

    public static TaskChainFactory create(Plugin plugin) {
        return new BukkitTaskChainFactory(plugin);
    }

    @SuppressWarnings("PublicInnerClass")
    private static class BukkitGameInterface implements GameInterface {
        private final Plugin plugin;
        private final AsyncQueue asyncQueue;

        BukkitGameInterface(Plugin plugin) {
            this.plugin = plugin;
            this.asyncQueue = new TaskChainAsyncQueue();
        }

        @Override
        public AsyncQueue getAsyncQueue() {
            return this.asyncQueue;
        }

        @Override
        public boolean isMainThread() {
            return Bukkit.isPrimaryThread();
        }

        @Override
        public void postToMain(Runnable run) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run);
        }

        @Override
        public void postAsync(Runnable run) {
            asyncQueue.postAsync(run);
        }

        @Override
        public void scheduleTask(int ticks, Runnable run) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run, ticks);
        }

        @Override
        public void registerShutdownHandler(TaskChainFactory factory) {
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPluginDisable(PluginDisableEvent event) {
                    if (event.getPlugin().equals(plugin)) {
                        factory.shutdown(60, TimeUnit.SECONDS);
                    }
                }
            }, plugin);
        }
    }

    public static final TaskChainNullAction<Player, String, ?> MESSAGE = new TaskChainNullAction<Player, String, Object>() {
        @Override
        public void onNull(Player player, String message) {
            player.sendMessage(message);
        }
    };
    public static final TaskChainNullAction<Player, String, ?> COLOR_MESSAGE = new TaskChainNullAction<Player, String, Object>() {
        @Override
        public void onNull(Player player, String message) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    };
}
