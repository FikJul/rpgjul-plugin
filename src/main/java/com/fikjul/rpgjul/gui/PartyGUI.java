package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.Party;
import com.fikjul.rpgjul.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Party GUI - Manage party members and settings
 */
public class PartyGUI extends BaseGUI {

    public PartyGUI(RPGJulPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 54, "§a§lParty");

        Party party = plugin.getPartyManager().getPlayerParty(player);

        if (party != null) {
            // Player is in a party
            displayPartyInfo(party);
        } else {
            // Player is not in a party
            displayNoParty();
        }

        // Back (Slot 49)
        inventory.setItem(49, createItem(Material.ARROW, "§e⬅ Back"));

        // Close (Slot 53)
        inventory.setItem(53, createItem(Material.BARRIER, "§c§lClose"));

        playSound(Sound.BLOCK_ENDER_CHEST_OPEN);
        player.openInventory(inventory);
    }

    private void displayPartyInfo(Party party) {
        boolean isLeader = party.isLeader(player.getUniqueId());
        
        // Party Info (Slot 4)
        List<String> infoLore = new ArrayList<>();
        OfflinePlayer leader = Bukkit.getOfflinePlayer(party.getLeaderId());
        infoLore.add("§7Leader: §f" + (leader.getName() != null ? leader.getName() : "Unknown"));
        infoLore.add("§7Members: §f" + party.getSize() + "/5");
        
        int minMembers = plugin.getConfig().getInt("party.party-buff.min-members", 3);
        boolean buffActive = party.getSize() >= minMembers;
        infoLore.add("§7Party Buff: " + (buffActive ? "§a✓ Active" : "§c✗ Inactive"));
        
        long timeAgo = System.currentTimeMillis() - party.getCreatedAt();
        long hoursAgo = timeAgo / (1000 * 60 * 60);
        infoLore.add("§7Created: §7" + hoursAgo + " hours ago");
        
        inventory.setItem(4, createItem(Material.PURPLE_BANNER, "§a§lYour Party", infoLore));

        // Display members (Slots 19-23)
        List<UUID> members = party.getMembers();
        for (int i = 0; i < Math.min(5, members.size()); i++) {
            UUID memberId = members.get(i);
            addMemberItem(19 + i, memberId, party, isLeader);
        }

        // Party Actions
        if (isLeader) {
            // Invite Player (Slot 45)
            List<String> inviteLore = new ArrayList<>();
            inviteLore.add("§7Use: /party invite <player>");
            inviteLore.add("§7to invite someone");
            inventory.setItem(45, createItem(Material.PLAYER_HEAD, "§a§l+ Invite Player", inviteLore));

            // Disband Party (Slot 46)
            List<String> disbandLore = new ArrayList<>();
            disbandLore.add("§7Click to disband party");
            disbandLore.add("§c§lWarning: This cannot be undone!");
            inventory.setItem(46, createItem(Material.TNT, "§c§l✗ Disband Party", disbandLore));
        } else {
            // Leave Party (Slot 45)
            List<String> leaveLore = new ArrayList<>();
            leaveLore.add("§7Click to leave the party");
            inventory.setItem(45, createItem(Material.IRON_DOOR, "§e§l← Leave Party", leaveLore));
        }
    }

    private void addMemberItem(int slot, UUID memberId, Party party, boolean viewerIsLeader) {
        OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
        String memberName = member.getName() != null ? member.getName() : "Unknown";
        boolean isLeader = party.isLeader(memberId);
        
        List<String> lore = new ArrayList<>();
        
        // Get player data if online
        if (member.isOnline() && member.getPlayer() != null) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(member.getPlayer());
            if (data != null) {
                lore.add("§7Level: §f" + data.getTotalLevel());
                lore.add("§7Class: §6" + (data.getActiveClass() != null ? data.getActiveClass().getDisplayName() : "None"));
            }
            lore.add("§7Status: §a● Online");
        } else {
            lore.add("§7Level: §f???");
            lore.add("§7Class: §6???");
            lore.add("§7Status: §c● Offline");
        }
        
        lore.add("§7");
        if (viewerIsLeader && !memberId.equals(player.getUniqueId())) {
            lore.add("§c§lClick to kick");
        }

        String displayName = "§a" + memberName;
        if (isLeader) {
            displayName += " §6⭐";
        }

        ItemStack item;
        if (member.isOnline() && member.getPlayer() != null) {
            item = createPlayerHead(member.getPlayer(), displayName, lore);
        } else {
            item = createItem(Material.PLAYER_HEAD, displayName, lore);
        }
        
        inventory.setItem(slot, item);
    }

    private void displayNoParty() {
        // No Party Message (Slot 13)
        List<String> noPartyLore = new ArrayList<>();
        noPartyLore.add("§7You are not in a party");
        noPartyLore.add("§7");
        noPartyLore.add("§7Create a party with:");
        noPartyLore.add("§e/party create");
        inventory.setItem(13, createItem(Material.BARRIER, "§c§lNo Party", noPartyLore));

        // Create Party (Slot 45)
        List<String> createLore = new ArrayList<>();
        createLore.add("§7Use: /party create");
        createLore.add("§7to create a party");
        inventory.setItem(45, createItem(Material.EMERALD, "§a§l+ Create Party", createLore));
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem, boolean isShiftClick, boolean isRightClick) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        playSound(Sound.UI_BUTTON_CLICK);

        Party party = plugin.getPartyManager().getPlayerParty(player);

        // Handle member kick (Slots 19-23)
        if (slot >= 19 && slot <= 23 && party != null && party.isLeader(player.getUniqueId())) {
            int memberIndex = slot - 19;
            List<UUID> members = party.getMembers();
            
            if (memberIndex < members.size()) {
                UUID targetId = members.get(memberIndex);
                
                // Don't allow kicking self
                if (!targetId.equals(player.getUniqueId())) {
                    Player target = Bukkit.getPlayer(targetId);
                    if (target != null) {
                        if (plugin.getPartyManager().kickPlayer(player, target)) {
                            player.sendMessage("§a§lKicked " + target.getName() + " from the party!");
                            target.sendMessage("§c§lYou were kicked from the party!");
                            playSound(Sound.ENTITY_VILLAGER_NO);
                            open(); // Refresh
                        }
                    }
                }
            }
            return;
        }

        switch (slot) {
            case 45: // Create/Leave/Invite
                if (party == null) {
                    // Create party
                    player.sendMessage("§7Use §e/party create §7to create a party");
                } else if (party.isLeader(player.getUniqueId())) {
                    // Invite (leader)
                    player.sendMessage("§7Use §e/party invite <player> §7to invite someone");
                } else {
                    // Leave (member)
                    if (plugin.getPartyManager().leaveParty(player)) {
                        player.sendMessage("§e§lYou left the party!");
                        playSound(Sound.BLOCK_NOTE_BLOCK_BASS);
                        open(); // Refresh
                    }
                }
                break;
            case 46: // Disband
                if (party != null && party.isLeader(player.getUniqueId())) {
                    if (plugin.getPartyManager().disbandParty(player)) {
                        player.sendMessage("§c§lParty disbanded!");
                        playSound(Sound.ENTITY_ENDER_DRAGON_GROWL);
                        
                        // Notify all members
                        for (UUID memberId : party.getMembers()) {
                            Player member = Bukkit.getPlayer(memberId);
                            if (member != null && !member.equals(player)) {
                                member.sendMessage("§c§lThe party has been disbanded!");
                            }
                        }
                        
                        open(); // Refresh
                    }
                }
                break;
            case 49: // Back
                plugin.getGUIManager().openMainMenu(player);
                break;
            case 53: // Close
                player.closeInventory();
                playSound(Sound.BLOCK_ENDER_CHEST_CLOSE);
                break;
        }
    }
}
