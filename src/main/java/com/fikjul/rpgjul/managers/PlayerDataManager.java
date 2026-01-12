package com.fikjul.rpgjul.managers;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player data caching and persistence
 */
public class PlayerDataManager {
    private final RPGJulPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerData> playerDataCache;

    public PlayerDataManager(RPGJulPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerDataCache = new ConcurrentHashMap<>();
        
        startAutoSaveTask();
    }

    /**
     * Load player data when they join
     */
    public void loadPlayerData(Player player) {
        databaseManager.loadPlayerData(player.getUniqueId(), player.getName())
                .thenAccept(data -> {
                    playerDataCache.put(player.getUniqueId(), data);
                    plugin.getLogger().info("Loaded data for player: " + player.getName());
                });
    }

    /**
     * Save player data when they quit
     */
    public void savePlayerData(Player player) {
        PlayerData data = playerDataCache.get(player.getUniqueId());
        if (data != null) {
            databaseManager.savePlayerData(data);
        }
    }

    /**
     * Get player data from cache
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    /**
     * Get player data from cache
     */
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    /**
     * Save all player data
     */
    public void saveAllPlayerData() {
        for (PlayerData data : playerDataCache.values()) {
            databaseManager.savePlayerData(data);
        }
        plugin.getLogger().info("Saved data for " + playerDataCache.size() + " players");
    }

    /**
     * Start auto-save task
     */
    private void startAutoSaveTask() {
        int interval = plugin.getConfig().getInt("database.auto-save-interval", 300);
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            saveAllPlayerData();
        }, interval * 20L, interval * 20L);
    }

    /**
     * Remove player from cache
     */
    public void removePlayerData(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    /**
     * Get all cached player data
     */
    public Collection<PlayerData> getAllPlayerData() {
        return new ArrayList<>(playerDataCache.values());
    }
}
