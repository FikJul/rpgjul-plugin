package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.Party;
import com.fikjul.rpgjul.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Main menu GUI - Central hub for accessing all RPG features
 */
public class MainMenuGUI extends BaseGUI {

    public MainMenuGUI(RPGJulPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, "§6§lRPG Menu");

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            player.sendMessage("§cFailed to load your data!");
            return;
        }

        // Stats Item (Slot 2)
        List<String> statsLore = new ArrayList<>();
        statsLore.add("§7Click to view detailed stats");
        inventory.setItem(2, createItem(Material.DIAMOND, "§b§l📊 Your Stats", statsLore));

        // Runes Item (Slot 4)
        List<String> runesLore = new ArrayList<>();
        runesLore.add("§7Available Points: §d" + data.getAvailableRunePoints());
        runesLore.add("§7Click to manage runes");
        ItemStack runeItem = data.getAvailableRunePoints() > 0 
            ? createGlowingItem(Material.NETHER_STAR, "§d§l⚡ Rune Management", runesLore)
            : createItem(Material.NETHER_STAR, "§d§l⚡ Rune Management", runesLore);
        inventory.setItem(4, runeItem);

        // Class Item (Slot 6)
        List<String> classLore = new ArrayList<>();
        classLore.add("§7Current Class: §6" + (data.getActiveClass() != null ? data.getActiveClass().getDisplayName() : "None"));
        classLore.add("§7Click to view classes");
        inventory.setItem(6, createItem(Material.GOLDEN_HELMET, "§6§l👑 Classes", classLore));

        // Party Item (Slot 19)
        Party party = plugin.getPartyManager().getPlayerParty(player);
        List<String> partyLore = new ArrayList<>();
        if (party != null) {
            partyLore.add("§7Party: §a" + (party.isLeader(player.getUniqueId()) ? "Your Party" : "Member"));
            partyLore.add("§7Members: §f" + party.getSize() + "/5");
        } else {
            partyLore.add("§7Party: §cNone");
            partyLore.add("§7Members: §f0/5");
        }
        partyLore.add("§7Click to manage party");
        inventory.setItem(19, createPlayerHead(player, "§a§l👥 Party", partyLore));

        // Leaderboard Item (Slot 21)
        List<String> leaderboardLore = new ArrayList<>();
        leaderboardLore.add("§7Click to view top players");
        inventory.setItem(21, createItem(Material.GOLD_INGOT, "§e§l🏆 Leaderboard", leaderboardLore));

        // Close Item (Slot 23)
        inventory.setItem(23, createItem(Material.BARRIER, "§c§lClose"));

        // Center Display (Slot 13) - Player Info
        List<String> playerLore = new ArrayList<>();
        playerLore.add("§7Level: §a" + data.getTotalLevel());
        playerLore.add("§7XP: §b" + String.format("%.1f", data.getCurrentXP()) + "/" + String.format("%.1f", data.getXpToNextLevel()));
        playerLore.add("§7Rune Points: §d" + data.getAvailableRunePoints());
        playerLore.add("§7Class: §6" + (data.getActiveClass() != null ? data.getActiveClass().getDisplayName() : "None"));
        inventory.setItem(13, createPlayerHead(player, "§f§l" + player.getName(), playerLore));

        playSound(Sound.BLOCK_ENDER_CHEST_OPEN);
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem, boolean isShiftClick, boolean isRightClick) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        playSound(Sound.UI_BUTTON_CLICK);

        switch (slot) {
            case 2: // Stats
                plugin.getGUIManager().openStatsGUI(player);
                break;
            case 4: // Runes
                plugin.getGUIManager().openRuneGUI(player);
                break;
            case 6: // Class
                plugin.getGUIManager().openClassGUI(player);
                break;
            case 19: // Party
                plugin.getGUIManager().openPartyGUI(player);
                break;
            case 21: // Leaderboard
                plugin.getGUIManager().openLeaderboardGUI(player);
                break;
            case 23: // Close
                player.closeInventory();
                playSound(Sound.BLOCK_ENDER_CHEST_CLOSE);
                break;
        }
    }
}
