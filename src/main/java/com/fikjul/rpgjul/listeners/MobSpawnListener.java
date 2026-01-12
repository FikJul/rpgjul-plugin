package com.fikjul.rpgjul.listeners;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Handles mob spawn events to apply leveling
 */
public class MobSpawnListener implements Listener {
    private final RPGJulPlugin plugin;

    public MobSpawnListener(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        
        // Apply mob level
        plugin.getMobManager().applyMobLevel(entity);
    }
}
