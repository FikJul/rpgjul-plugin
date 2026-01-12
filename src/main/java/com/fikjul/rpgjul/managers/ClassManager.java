package com.fikjul.rpgjul.managers;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RPGClass;
import org.bukkit.entity.Player;

/**
 * Manages class unlocking and activation
 */
public class ClassManager {
    private final RPGJulPlugin plugin;
    private final PlayerDataManager playerDataManager;

    public ClassManager(RPGJulPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Check if player can unlock a class
     */
    public boolean canUnlockClass(Player player, RPGClass rpgClass) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return false;

        return rpgClass.meetsRequirements(data.getRuneLevels());
    }

    /**
     * Activate a class for a player
     */
    public boolean activateClass(Player player, RPGClass rpgClass) {
        if (!canUnlockClass(player, rpgClass)) {
            return false;
        }

        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return false;

        data.setActiveClass(rpgClass);
        return true;
    }

    /**
     * Get active class for player
     */
    public RPGClass getActiveClass(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return null;

        return data.getActiveClass();
    }

    /**
     * Get critical hit chance for Blademaster
     */
    public double getCriticalHitChance(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        if (data.getActiveClass() == RPGClass.BLADEMASTER) {
            return 0.15; // 15% critical hit chance
        }

        return 0.0;
    }

    /**
     * Get lifesteal percentage for Berserker
     */
    public double getLifestealPercentage(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        if (data.getActiveClass() == RPGClass.BERSERKER) {
            return 0.20; // 20% lifesteal
        }

        return 0.0;
    }

    /**
     * Get double arrow chance for Sharpshooter
     */
    public double getDoubleArrowChance(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        if (data.getActiveClass() == RPGClass.SHARPSHOOTER) {
            return 0.15; // 15% double arrow chance
        }

        return 0.0;
    }

    /**
     * Get backstab damage multiplier for Shadow Assassin
     */
    public double getBackstabMultiplier(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 1.0;

        if (data.getActiveClass() == RPGClass.SHADOW_ASSASSIN) {
            return 1.5; // 50% bonus damage
        }

        return 1.0;
    }

    /**
     * Get damage reflection for Fortress
     */
    public double getDamageReflection(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        if (data.getActiveClass() == RPGClass.FORTRESS) {
            return 0.10; // 10% damage reflection
        }

        return 0.0;
    }

    /**
     * Get gathering speed bonus for Artisan
     */
    public double getGatheringSpeedBonus(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        if (data.getActiveClass() == RPGClass.ARTISAN) {
            return 0.25; // 25% gathering speed bonus
        }

        return 0.0;
    }

    /**
     * Get ranged damage bonus for Ranger
     */
    public double getRangedDamageBonus(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        if (data.getActiveClass() == RPGClass.RANGER) {
            return 0.20; // 20% ranged damage bonus
        }

        return 0.0;
    }

    /**
     * Check if Paladin should regenerate (when blocking)
     */
    public boolean shouldPaladinRegenerate(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return false;

        return data.getActiveClass() == RPGClass.PALADIN;
    }
}
