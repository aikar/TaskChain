/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
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
