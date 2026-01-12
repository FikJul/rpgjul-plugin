package com.fikjul.rpgjul.listeners;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles player join events
 */
public class PlayerJoinListener implements Listener {
    private final RPGJulPlugin plugin;

    public PlayerJoinListener(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player data
        plugin.getPlayerDataManager().loadPlayerData(event.getPlayer());
        
        // Apply stat bonuses after a small delay to ensure data is loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getLevelManager().applyStatBonuses(event.getPlayer());
        }, 20L); // 1 second delay
    }
}
