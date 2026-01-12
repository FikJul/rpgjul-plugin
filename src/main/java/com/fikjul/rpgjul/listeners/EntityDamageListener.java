package com.fikjul.rpgjul.listeners;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.enums.RPGClass;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Handles damage events for class bonuses and rune effects
 */
public class EntityDamageListener implements Listener {
    private final RPGJulPlugin plugin;
    private final Random random;

    public EntityDamageListener(RPGJulPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Handle player attacking entity
        if (event.getDamager() instanceof Player) {
            handlePlayerAttack(event, (Player) event.getDamager());
        }
        
        // Handle arrow damage
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                handleArrowAttack(event, (Player) arrow.getShooter(), arrow);
            }
        }
        
        // Handle player receiving damage
        if (event.getEntity() instanceof Player) {
            handlePlayerDefense(event, (Player) event.getEntity());
        }
    }

    /**
     * Handle player melee attack
     */
    private void handlePlayerAttack(EntityDamageByEntityEvent event, Player attacker) {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        double damage = event.getDamage();
        
        // Apply weapon-specific rune bonuses
        if (weapon.getType().name().contains("SWORD")) {
            double multiplier = plugin.getLevelManager().getDamageMultiplier(attacker, "SWORD");
            damage *= multiplier;
            
            // Blademaster critical hit
            double critChance = plugin.getClassManager().getCriticalHitChance(attacker);
            if (critChance > 0 && random.nextDouble() < critChance) {
                damage *= 1.5; // 50% more damage on crit
            }
        } else if (weapon.getType().name().contains("AXE")) {
            double multiplier = plugin.getLevelManager().getDamageMultiplier(attacker, "AXE");
            damage *= multiplier;
            
            // Berserker lifesteal
            double lifesteal = plugin.getClassManager().getLifestealPercentage(attacker);
            if (lifesteal > 0) {
                double healAmount = damage * lifesteal;
                double newHealth = Math.min(attacker.getHealth() + healAmount, 
                        attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                attacker.setHealth(newHealth);
            }
        }
        
        // Shadow Assassin backstab damage
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            if (isBackstab(attacker, target)) {
                double backstabMultiplier = plugin.getClassManager().getBackstabMultiplier(attacker);
                damage *= backstabMultiplier;
            }
        }
        
        // Apply party damage bonus
        double partyBonus = plugin.getPartyManager().getPartyDamageBonus(attacker);
        if (partyBonus > 0) {
            damage *= (1.0 + partyBonus);
        }
        
        event.setDamage(damage);
    }

    /**
     * Handle arrow attack
     */
    private void handleArrowAttack(EntityDamageByEntityEvent event, Player shooter, Arrow arrow) {
        double damage = event.getDamage();
        
        // Apply bow damage bonus
        double multiplier = plugin.getLevelManager().getDamageMultiplier(shooter, "BOW");
        damage *= multiplier;
        
        // Ranger bonus
        double rangedBonus = plugin.getClassManager().getRangedDamageBonus(shooter);
        if (rangedBonus > 0) {
            damage *= (1.0 + rangedBonus);
        }
        
        // Sharpshooter double arrow
        double doubleArrowChance = plugin.getClassManager().getDoubleArrowChance(shooter);
        if (doubleArrowChance > 0 && random.nextDouble() < doubleArrowChance) {
            // Spawn second arrow
            Arrow secondArrow = shooter.getWorld().spawnArrow(
                    arrow.getLocation(), 
                    arrow.getVelocity(), 
                    (float) arrow.getVelocity().length(), 
                    0);
            secondArrow.setShooter(shooter);
            secondArrow.setDamage(damage);
        }
        
        // Apply party damage bonus
        double partyBonus = plugin.getPartyManager().getPartyDamageBonus(shooter);
        if (partyBonus > 0) {
            damage *= (1.0 + partyBonus);
        }
        
        event.setDamage(damage);
    }

    /**
     * Handle player defense
     */
    private void handlePlayerDefense(EntityDamageByEntityEvent event, Player defender) {
        double damage = event.getDamage();
        
        // Apply damage reduction from Aegis rune
        double reduction = plugin.getLevelManager().getDamageReduction(defender);
        damage *= (1.0 - reduction);
        
        // Fortress damage reflection
        double reflection = plugin.getClassManager().getDamageReflection(defender);
        if (reflection > 0 && event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            double reflectedDamage = event.getDamage() * reflection;
            attacker.damage(reflectedDamage);
        }
        
        event.setDamage(damage);
    }

    /**
     * Check if attack is a backstab
     */
    private boolean isBackstab(Player attacker, LivingEntity target) {
        Vector attackerDirection = attacker.getLocation().getDirection();
        Vector targetDirection = target.getLocation().getDirection();
        
        // Calculate angle between directions
        double dotProduct = attackerDirection.dot(targetDirection);
        
        // If dot product is positive, they're facing same direction (backstab)
        return dotProduct > 0.5;
    }
}
