package com.fikjul.rpgjul.managers;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Manages mob leveling system
 */
public class MobManager {
    private final RPGJulPlugin plugin;
    private final Random random;

    public MobManager(RPGJulPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Apply level to a mob when it spawns
     */
    public void applyMobLevel(LivingEntity mob) {
        if (!plugin.getConfig().getBoolean("mob-leveling.enabled", true)) {
            return;
        }

        int level = generateMobLevel(mob);
        mob.setMetadata("rpg_level", new FixedMetadataValue(plugin, level));

        // Apply stat scaling
        scaleMobStats(mob, level);

        // Apply name tag
        if (plugin.getConfig().getBoolean("mob-leveling.show-name-tags", true)) {
            applyNameTag(mob, level);
        }
    }

    /**
     * Generate a random level for a mob
     */
    private int generateMobLevel(LivingEntity mob) {
        EntityType type = mob.getType();
        
        // Boss mobs always spawn at max level
        if (isBossMob(type) && plugin.getConfig().getBoolean("mob-leveling.boss-always-max-level", true)) {
            return plugin.getConfig().getInt("mob-leveling.max-level", 100);
        }

        int minLevel = plugin.getConfig().getInt("mob-leveling.min-level", 1);
        int maxLevel = plugin.getConfig().getInt("mob-leveling.max-level", 100);

        return minLevel + random.nextInt(maxLevel - minLevel + 1);
    }

    /**
     * Scale mob stats based on level
     */
    private void scaleMobStats(LivingEntity mob, int level) {
        double healthPerLevel = plugin.getConfig().getDouble("mob-leveling.health-per-level", 2.0);
        double damagePerLevel = plugin.getConfig().getDouble("mob-leveling.damage-per-level", 0.5);

        // Scale health
        AttributeInstance maxHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double baseHealth = maxHealth.getBaseValue();
            double newHealth = baseHealth + (healthPerLevel * level);
            maxHealth.setBaseValue(newHealth);
            mob.setHealth(newHealth);
        }

        // Scale damage
        AttributeInstance attackDamage = mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) {
            double baseDamage = attackDamage.getBaseValue();
            double newDamage = baseDamage + (damagePerLevel * level);
            attackDamage.setBaseValue(newDamage);
        }
    }

    /**
     * Apply colored name tag to mob based on level
     */
    private void applyNameTag(LivingEntity mob, int level) {
        ChatColor color;
        
        if (level >= 91) {
            color = ChatColor.DARK_PURPLE;
        } else if (level >= 61) {
            color = ChatColor.RED;
        } else if (level >= 31) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.WHITE;
        }

        String mobName = formatMobName(mob.getType());
        String nameTag = color + "[Lv " + level + "] " + mobName;
        
        mob.setCustomName(nameTag);
        mob.setCustomNameVisible(true);
    }

    /**
     * Format mob type name for display
     */
    private String formatMobName(EntityType type) {
        String name = type.name().replace("_", " ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : name.split(" ")) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase());
            formatted.append(word.substring(1).toLowerCase());
        }
        
        return formatted.toString();
    }

    /**
     * Get the level of a mob
     */
    public int getMobLevel(Entity mob) {
        if (mob.hasMetadata("rpg_level")) {
            return mob.getMetadata("rpg_level").get(0).asInt();
        }
        return 1;
    }

    /**
     * Check if entity is a boss mob
     */
    public boolean isBossMob(EntityType type) {
        return type == EntityType.ENDER_DRAGON || 
               type == EntityType.WITHER || 
               type == EntityType.WARDEN ||
               type == EntityType.ELDER_GUARDIAN;
    }
}
