package com.fikjul.rpgjul.managers;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.Party;
import com.fikjul.rpgjul.data.PartyInvitation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages party system including creation, invitations, and XP sharing
 */
public class PartyManager {
    private final RPGJulPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Party> parties; // partyId -> Party
    private final Map<UUID, UUID> playerToParty; // playerId -> partyId
    private final Map<UUID, PartyInvitation> pendingInvitations; // inviteeId -> invitation

    public PartyManager(RPGJulPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.parties = new ConcurrentHashMap<>();
        this.playerToParty = new ConcurrentHashMap<>();
        this.pendingInvitations = new ConcurrentHashMap<>();
        
        loadParties();
    }

    /**
     * Load parties from database
     */
    private void loadParties() {
        Map<UUID, Party> loadedParties = databaseManager.loadParties();
        parties.putAll(loadedParties);
        
        // Build player to party mapping
        for (Party party : loadedParties.values()) {
            for (UUID memberId : party.getMembers()) {
                playerToParty.put(memberId, party.getPartyId());
            }
        }
        
        plugin.getLogger().info("Loaded " + parties.size() + " parties from database");
    }

    /**
     * Create a new party
     */
    public Party createParty(Player leader) {
        if (playerToParty.containsKey(leader.getUniqueId())) {
            return null; // Player already in a party
        }

        UUID partyId = UUID.randomUUID();
        Party party = new Party(partyId, leader.getUniqueId());
        
        parties.put(partyId, party);
        playerToParty.put(leader.getUniqueId(), partyId);
        
        databaseManager.saveParty(party);
        
        return party;
    }

    /**
     * Invite a player to a party
     */
    public boolean invitePlayer(Player inviter, Player invitee) {
        if (!playerToParty.containsKey(inviter.getUniqueId())) {
            return false; // Inviter not in a party
        }

        if (playerToParty.containsKey(invitee.getUniqueId())) {
            inviter.sendMessage(ChatColor.RED + invitee.getName() + " is already in a party!");
            return false;
        }

        UUID partyId = playerToParty.get(inviter.getUniqueId());
        Party party = parties.get(partyId);

        if (!party.isLeader(inviter.getUniqueId())) {
            inviter.sendMessage(ChatColor.RED + "Only the party leader can invite players!");
            return false;
        }

        int maxMembers = plugin.getConfig().getInt("party.max-members", 5);
        if (party.getSize() >= maxMembers) {
            inviter.sendMessage(ChatColor.RED + "Your party is full!");
            return false;
        }

        int expireSeconds = plugin.getConfig().getInt("party.invitation-expire-seconds", 60);
        PartyInvitation invitation = new PartyInvitation(partyId, inviter.getUniqueId(), 
                invitee.getUniqueId(), expireSeconds);
        
        pendingInvitations.put(invitee.getUniqueId(), invitation);

        // Schedule invitation expiration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingInvitations.remove(invitee.getUniqueId());
        }, expireSeconds * 20L);

        return true;
    }

    /**
     * Accept a party invitation
     */
    public boolean acceptInvitation(Player player) {
        PartyInvitation invitation = pendingInvitations.remove(player.getUniqueId());
        
        if (invitation == null) {
            return false;
        }

        if (invitation.isExpired()) {
            return false;
        }

        Party party = parties.get(invitation.getPartyId());
        if (party == null) {
            return false;
        }

        int maxMembers = plugin.getConfig().getInt("party.max-members", 5);
        if (party.getSize() >= maxMembers) {
            player.sendMessage(ChatColor.RED + "That party is now full!");
            return false;
        }

        party.addMember(player.getUniqueId());
        playerToParty.put(player.getUniqueId(), party.getPartyId());
        
        databaseManager.saveParty(party);

        return true;
    }

    /**
     * Leave party
     */
    public boolean leaveParty(Player player) {
        UUID partyId = playerToParty.remove(player.getUniqueId());
        if (partyId == null) {
            return false;
        }

        Party party = parties.get(partyId);
        if (party == null) {
            return false;
        }

        // Check if leader before removing from party
        boolean wasLeader = party.isLeader(player.getUniqueId());
        
        party.removeMember(player.getUniqueId());

        // If leader left, promote new leader or disband
        if (wasLeader) {
            if (party.getSize() > 1) {
                party.promoteNewLeader();
                databaseManager.saveParty(party);
            } else {
                // Disband party
                parties.remove(partyId);
                databaseManager.deleteParty(partyId);
                return true;
            }
        }

        databaseManager.saveParty(party);
        return true;
    }

    /**
     * Kick a player from party
     */
    public boolean kickPlayer(Player leader, Player target) {
        UUID partyId = playerToParty.get(leader.getUniqueId());
        if (partyId == null) {
            return false;
        }

        Party party = parties.get(partyId);
        if (party == null || !party.isLeader(leader.getUniqueId())) {
            return false;
        }

        if (!party.isMember(target.getUniqueId())) {
            return false;
        }

        party.removeMember(target.getUniqueId());
        playerToParty.remove(target.getUniqueId());
        
        databaseManager.saveParty(party);
        return true;
    }

    /**
     * Disband a party
     */
    public boolean disbandParty(Player leader) {
        UUID partyId = playerToParty.get(leader.getUniqueId());
        if (partyId == null) {
            return false;
        }

        Party party = parties.get(partyId);
        if (party == null || !party.isLeader(leader.getUniqueId())) {
            return false;
        }

        // Remove all members from mapping
        for (UUID memberId : party.getMembers()) {
            playerToParty.remove(memberId);
        }

        parties.remove(partyId);
        databaseManager.deleteParty(partyId);
        
        return true;
    }

    /**
     * Get party for a player
     */
    public Party getPlayerParty(UUID playerId) {
        UUID partyId = playerToParty.get(playerId);
        return partyId != null ? parties.get(partyId) : null;
    }

    /**
     * Get party for a player
     */
    public Party getPlayerParty(Player player) {
        return getPlayerParty(player.getUniqueId());
    }

    /**
     * Get pending invitation for a player
     */
    public PartyInvitation getPendingInvitation(Player player) {
        return pendingInvitations.get(player.getUniqueId());
    }

    /**
     * Check if player is in a party
     */
    public boolean isInParty(UUID playerId) {
        return playerToParty.containsKey(playerId);
    }

    /**
     * Get party members within radius of a player
     */
    public List<Player> getPartyMembersNearby(Player player, double radius) {
        List<Player> nearby = new ArrayList<>();
        Party party = getPlayerParty(player);
        
        if (party == null) {
            return nearby;
        }

        for (UUID memberId : party.getMembers()) {
            if (memberId.equals(player.getUniqueId())) {
                continue;
            }

            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline() && 
                member.getWorld().equals(player.getWorld()) &&
                member.getLocation().distance(player.getLocation()) <= radius) {
                nearby.add(member);
            }
        }

        return nearby;
    }

    /**
     * Get party damage bonus
     */
    public double getPartyDamageBonus(Player player) {
        if (!plugin.getConfig().getBoolean("party.party-buff.enabled", true)) {
            return 0.0;
        }

        Party party = getPlayerParty(player);
        if (party == null) {
            return 0.0;
        }

        int minMembers = plugin.getConfig().getInt("party.party-buff.min-members", 3);
        if (party.getSize() < minMembers) {
            return 0.0;
        }

        return plugin.getConfig().getDouble("party.party-buff.damage-bonus-percent", 10.0) / 100.0;
    }

    /**
     * Broadcast message to party
     */
    public void broadcastToParty(Party party, String message) {
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        }
    }
}
