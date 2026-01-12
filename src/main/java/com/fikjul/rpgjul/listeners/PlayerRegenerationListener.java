package com.fikjul.rpgjul.listeners;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Handles player regeneration from Restoration rune
 */
public class PlayerRegenerationListener implements Listener {
    private final RPGJulPlugin plugin;

    public PlayerRegenerationListener(RPGJulPlugin plugin) {
        this.plugin = plugin;
        startRegenerationTask();
    }

    /**
     * Start regeneration task that runs every 5 seconds (100 ticks)
     */
    private void startRegenerationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                double regenAmount = plugin.getLevelManager().getRegenerationBonus(player);
                
                if (regenAmount > 0) {
                    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double newHealth = Math.min(player.getHealth() + regenAmount, maxHealth);
                    player.setHealth(newHealth);
                }
            }
        }, 100L, 100L); // Run every 5 seconds
    }
}
