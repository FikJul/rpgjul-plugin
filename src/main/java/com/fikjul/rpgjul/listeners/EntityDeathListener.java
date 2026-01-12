package com.fikjul.rpgjul.listeners;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.Party;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

/**
 * Handles entity death events for XP distribution
 */
public class EntityDeathListener implements Listener {
    private final RPGJulPlugin plugin;

    public EntityDeathListener(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer == null) {
            return;
        }

        // Get mob level
        int mobLevel = plugin.getMobManager().getMobLevel(entity);
        
        // Calculate XP
        double xp = plugin.getLevelManager().calculateMobXP(entity, mobLevel);
        
        // Check if in party and XP sharing is enabled
        if (plugin.getConfig().getBoolean("party.shared-xp.enabled", true)) {
            Party party = plugin.getPartyManager().getPlayerParty(killer);
            
            if (party != null && party.getSize() > 1) {
                // Share XP with nearby party members
                double shareRadius = plugin.getConfig().getDouble("party.shared-xp.share-radius", 20.0);
                List<Player> nearbyMembers = plugin.getPartyManager().getPartyMembersNearby(killer, shareRadius);
                
                // Include killer in the count
                int totalPlayers = nearbyMembers.size() + 1;
                double sharedXP = xp / totalPlayers;
                
                // Give XP to killer
                plugin.getLevelManager().giveXP(killer, sharedXP);
                
                // Give XP to nearby party members
                for (Player member : nearbyMembers) {
                    plugin.getLevelManager().giveXP(member, sharedXP);
                }
                
                return;
            }
        }
        
        // Give full XP to killer if not sharing
        plugin.getLevelManager().giveXP(killer, xp);
    }
}
