package com.fikjul.rpgjul.managers;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RuneType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Manages XP gain and level progression
 */
public class LevelManager {
    private final RPGJulPlugin plugin;
    private final PlayerDataManager playerDataManager;

    public LevelManager(RPGJulPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Give XP to a player
     */
    public void giveXP(Player player, double amount) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return;

        data.setCurrentXP(data.getCurrentXP() + amount);

        // Check for level up
        while (data.getCurrentXP() >= data.getXpToNextLevel()) {
            levelUp(player, data);
        }
    }

    /**
     * Level up a player
     */
    private void levelUp(Player player, PlayerData data) {
        int maxLevel = plugin.getConfig().getInt("leveling.max-level", 100);
        if (data.getTotalLevel() >= maxLevel) {
            data.setCurrentXP(data.getXpToNextLevel());
            return;
        }

        double xpBase = plugin.getConfig().getDouble("leveling.xp-base", 100);
        double xpMultiplier = plugin.getConfig().getDouble("leveling.xp-multiplier", 1.5);
        String formula = plugin.getConfig().getString("leveling.xp-formula", "exponential");
        int pointsPerLevel = plugin.getConfig().getInt("runes.points-per-level", 1);

        data.levelUp(xpBase, xpMultiplier, formula, pointsPerLevel);

        // Effects
        if (plugin.getConfig().getBoolean("gui.particles-enabled", true)) {
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        }
        
        if (plugin.getConfig().getBoolean("gui.sounds-enabled", true)) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        // Message
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.YELLOW + "Level Up! " + 
                ChatColor.GOLD + "You are now level " + ChatColor.AQUA + data.getTotalLevel() + 
                ChatColor.GOLD + "! You gained " + ChatColor.GREEN + pointsPerLevel + 
                ChatColor.GOLD + " Rune Point" + (pointsPerLevel > 1 ? "s" : "") + "!");
    }

    /**
     * Calculate XP from killing a mob
     */
    public double calculateMobXP(Entity entity, int mobLevel) {
        EntityType type = entity.getType();
        String mobName = type.name().toLowerCase();
        
        // Get base XP from config
        double baseXP = plugin.getConfig().getDouble("xp-rates." + mobName, 10.0);
        
        // Check if it's a boss mob
        if (isBossMob(type)) {
            double bossMultiplier = plugin.getConfig().getDouble("xp-rates.boss-multiplier", 5.0);
            baseXP *= bossMultiplier;
        }
        
        // Apply level multiplier
        double levelMultiplier = plugin.getConfig().getDouble("mob-leveling.xp-multiplier-per-level", 0.015);
        double xpMultiplier = 1.0 + (mobLevel * levelMultiplier);
        
        return baseXP * xpMultiplier;
    }

    /**
     * Check if entity is a boss mob
     */
    private boolean isBossMob(EntityType type) {
        return type == EntityType.ENDER_DRAGON || 
               type == EntityType.WITHER || 
               type == EntityType.WARDEN ||
               type == EntityType.ELDER_GUARDIAN;
    }

    /**
     * Apply stat bonuses from runes to player
     */
    public void applyStatBonuses(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return;

        // Apply max health from Vitality rune
        int vitalityLevel = data.getRuneLevel(RuneType.VITALITY);
        double bonusHealth = RuneType.VITALITY.calculateBonus(vitalityLevel);
        
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0 + bonusHealth);
        }

        // Apply movement speed from Swiftwind rune
        int swiftwindLevel = data.getRuneLevel(RuneType.SWIFTWIND);
        double speedBonus = RuneType.SWIFTWIND.calculateBonus(swiftwindLevel) / 100.0; // Convert to percentage
        
        AttributeInstance speed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            double baseSpeed = 0.1; // Default player speed
            speed.setBaseValue(baseSpeed * (1.0 + speedBonus));
        }

        // Heal player to max health if needed
        if (player.getHealth() > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
    }

    /**
     * Calculate damage multiplier for a weapon type
     */
    public double getDamageMultiplier(Player player, String weaponType) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 1.0;

        double multiplier = 1.0;

        switch (weaponType.toUpperCase()) {
            case "SWORD":
                int bladestormLevel = data.getRuneLevel(RuneType.BLADESTORM);
                multiplier += RuneType.BLADESTORM.calculateBonus(bladestormLevel) / 100.0;
                break;
            case "AXE":
                int warbringerLevel = data.getRuneLevel(RuneType.WARBRINGER);
                multiplier += RuneType.WARBRINGER.calculateBonus(warbringerLevel) / 100.0;
                break;
            case "BOW":
                int marksmanLevel = data.getRuneLevel(RuneType.MARKSMAN);
                multiplier += RuneType.MARKSMAN.calculateBonus(marksmanLevel) / 100.0;
                break;
        }

        return multiplier;
    }

    /**
     * Get damage reduction percentage
     */
    public double getDamageReduction(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        int aegisLevel = data.getRuneLevel(RuneType.AEGIS);
        return RuneType.AEGIS.calculateBonus(aegisLevel) / 100.0; // Convert to percentage
    }

    /**
     * Get regeneration bonus
     */
    public double getRegenerationBonus(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player);
        if (data == null) return 0.0;

        int restorationLevel = data.getRuneLevel(RuneType.RESTORATION);
        return RuneType.RESTORATION.calculateBonus(restorationLevel);
    }
}
