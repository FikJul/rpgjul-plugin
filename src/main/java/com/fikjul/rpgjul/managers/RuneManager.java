package com.fikjul.rpgjul.managers;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RuneType;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * Manages rune allocation and reset
 */
public class RuneManager {
    private final RPGJulPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;

    public RuneManager(RPGJulPlugin plugin, PlayerDataManager playerDataManager, LevelManager levelManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
    }

    /**
     * Allocate a rune point to a specific rune
     */
    public boolean allocateRune(Player player, RuneType rune) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return false;

        int maxLevel = plugin.getConfig().getInt("runes.max-level", 100);
        
        if (!data.allocateRune(rune, maxLevel)) {
            if (data.getAvailableRunePoints() <= 0) {
                player.sendMessage(ChatColor.RED + "You don't have any Rune Points available!");
            } else {
                player.sendMessage(ChatColor.RED + "This rune is already at maximum level!");
            }
            return false;
        }

        // Apply stat bonuses
        levelManager.applyStatBonuses(player);

        // Effects
        if (plugin.getConfig().getBoolean("gui.particles-enabled", true)) {
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5);
        }
        
        if (plugin.getConfig().getBoolean("gui.sounds-enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        }

        player.sendMessage(ChatColor.GREEN + "✓ Upgraded " + ChatColor.AQUA + rune.getDisplayName() + 
                ChatColor.GREEN + " to level " + ChatColor.GOLD + data.getRuneLevel(rune) + 
                ChatColor.GREEN + "!");

        return true;
    }

    /**
     * Reset all runes for a player
     */
    public boolean resetRunes(Player player, boolean bypassCooldown) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return false;

        if (!bypassCooldown) {
            long cooldownDays = plugin.getConfig().getLong("runes.reset-cooldown-days", 7);
            long cooldownMs = TimeUnit.DAYS.toMillis(cooldownDays);
            long timeSinceReset = System.currentTimeMillis() - data.getLastRuneReset();

            if (timeSinceReset < cooldownMs && data.getLastRuneReset() > 0) {
                long remainingMs = cooldownMs - timeSinceReset;
                long remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMs);
                long remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMs) % 24;
                
                player.sendMessage(ChatColor.RED + "You can reset your runes again in " + 
                        remainingDays + " days and " + remainingHours + " hours!");
                return false;
            }
        }

        data.resetRunes();
        levelManager.applyStatBonuses(player);

        player.sendMessage(ChatColor.GREEN + "✓ All runes have been reset! " + 
                ChatColor.GOLD + "You received " + ChatColor.AQUA + data.getAvailableRunePoints() + 
                ChatColor.GOLD + " Rune Points!");

        return true;
    }

    /**
     * Get total rune points invested
     */
    public int getTotalRunePoints(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0;

        int total = 0;
        for (int level : data.getRuneLevels().values()) {
            total += level;
        }

        return total;
    }
}
