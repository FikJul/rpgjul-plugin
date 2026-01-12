package com.fikjul.rpgjul.listeners;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player quit events
 */
public class PlayerQuitListener implements Listener {
    private final RPGJulPlugin plugin;

    public PlayerQuitListener(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player data
        plugin.getPlayerDataManager().savePlayerData(event.getPlayer());
        
        // Remove from cache after saving
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerDataManager().removePlayerData(event.getPlayer().getUniqueId());
        }, 40L); // 2 second delay to ensure save completes
    }
}
