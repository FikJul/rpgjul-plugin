package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Leaderboard GUI - Shows top players by various categories
 */
public class LeaderboardGUI extends BaseGUI {
    private LeaderboardCategory currentCategory;

    public enum LeaderboardCategory {
        TOTAL_LEVEL("Total Level", Material.GOLD_INGOT, 0),
        VITALITY("Vitality Rune", Material.RED_DYE, 1),
        BLADESTORM("Bladestorm", Material.IRON_SWORD, 2),
        WARBRINGER("Warbringer", Material.IRON_AXE, 3),
        MARKSMAN("Marksman", Material.BOW, 4),
        SWIFTWIND("Swiftwind", Material.FEATHER, 5),
        AEGIS("Aegis", Material.IRON_CHESTPLATE, 6);

        private final String displayName;
        private final Material icon;
        private final int slot;

        LeaderboardCategory(String displayName, Material icon, int slot) {
            this.displayName = displayName;
            this.icon = icon;
            this.slot = slot;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }

        public int getSlot() {
            return slot;
        }
    }

    public LeaderboardGUI(RPGJulPlugin plugin, Player player) {
        super(plugin, player);
        this.currentCategory = LeaderboardCategory.TOTAL_LEVEL;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 54, "§e§lLeaderboard");

        // Category selection (Top Row)
        for (LeaderboardCategory category : LeaderboardCategory.values()) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to view");
            
            ItemStack item;
            if (category == currentCategory) {
                item = createGlowingItem(category.getIcon(), "§e§l" + category.getDisplayName(), lore);
            } else {
                item = createItem(category.getIcon(), "§7" + category.getDisplayName(), lore);
            }
            inventory.setItem(category.getSlot(), item);
        }

        // Get leaderboard data
        List<LeaderboardEntry> leaderboard = getLeaderboardData();

        // Display top 10 players
        int displaySlot = 10;
        for (int i = 0; i < Math.min(10, leaderboard.size()); i++) {
            LeaderboardEntry entry = leaderboard.get(i);
            addLeaderboardEntry(displaySlot, i + 1, entry);
            
            displaySlot++;
            if (displaySlot == 17) displaySlot = 19;
            if (displaySlot == 26) displaySlot = 28;
            if (displaySlot == 35) displaySlot = 37;
        }

        // Your Rank (Slot 49)
        int playerRank = getPlayerRank(leaderboard);
        if (playerRank > 10) {
            LeaderboardEntry playerEntry = leaderboard.get(playerRank - 1);
            List<String> rankLore = new ArrayList<>();
            rankLore.add("§7Rank: §f#" + playerRank);
            rankLore.add("§7Level: §f" + playerEntry.value);
            if (playerRank > 10) {
                rankLore.add("§7Distance from #10: §c" + (leaderboard.get(9).value - playerEntry.value));
            }
            inventory.setItem(49, createPlayerHead(player, "§a§lYour Rank", rankLore));
        } else {
            // Back button if player is in top 10
            inventory.setItem(49, createItem(Material.ARROW, "§e⬅ Back"));
        }

        // Refresh (Slot 45)
        List<String> refreshLore = new ArrayList<>();
        refreshLore.add("§7Last updated: §anow");
        inventory.setItem(45, createItem(Material.COMPASS, "§a§lRefresh Leaderboard", refreshLore));

        // Close (Slot 53)
        inventory.setItem(53, createItem(Material.BARRIER, "§c§lClose"));

        playSound(Sound.BLOCK_ENDER_CHEST_OPEN);
        player.openInventory(inventory);
    }

    private void addLeaderboardEntry(int slot, int rank, LeaderboardEntry entry) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.uuid);
        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
        
        String rankDisplay;
        String color;
        if (rank == 1) {
            rankDisplay = "🥇";
            color = "§6§l";
        } else if (rank == 2) {
            rankDisplay = "🥈";
            color = "§7§l";
        } else if (rank == 3) {
            rankDisplay = "🥉";
            color = "§c§l";
        } else {
            rankDisplay = "#" + rank;
            color = "§f";
        }

        List<String> lore = new ArrayList<>();
        lore.add("§7Level: §e" + entry.value);
        
        if (rank <= 3) {
            if (rank == 1) lore.add("§6§lFIRST PLACE!");
            if (rank == 2) lore.add("§7§lSECOND PLACE!");
            if (rank == 3) lore.add("§c§lTHIRD PLACE!");
        }

        ItemStack item;
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            item = createPlayerHead(offlinePlayer.getPlayer(), color + rankDisplay + " " + playerName, lore);
        } else {
            // Use a placeholder for offline players
            item = createItem(Material.PLAYER_HEAD, color + rankDisplay + " " + playerName, lore);
        }
        
        inventory.setItem(slot, item);
    }

    private List<LeaderboardEntry> getLeaderboardData() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        // Get all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(p);
            if (data != null) {
                int value = getValueForCategory(data);
                entries.add(new LeaderboardEntry(p.getUniqueId(), data.getUsername(), value));
            }
        }

        // Sort by value (descending)
        entries.sort((a, b) -> Integer.compare(b.value, a.value));
        
        return entries;
    }

    private int getValueForCategory(PlayerData data) {
        switch (currentCategory) {
            case TOTAL_LEVEL:
                return data.getTotalLevel();
            case VITALITY:
                return data.getRuneLevel(RuneType.VITALITY);
            case BLADESTORM:
                return data.getRuneLevel(RuneType.BLADESTORM);
            case WARBRINGER:
                return data.getRuneLevel(RuneType.WARBRINGER);
            case MARKSMAN:
                return data.getRuneLevel(RuneType.MARKSMAN);
            case SWIFTWIND:
                return data.getRuneLevel(RuneType.SWIFTWIND);
            case AEGIS:
                return data.getRuneLevel(RuneType.AEGIS);
            default:
                return 0;
        }
    }

    private int getPlayerRank(List<LeaderboardEntry> leaderboard) {
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).uuid.equals(player.getUniqueId())) {
                return i + 1;
            }
        }
        return leaderboard.size() + 1;
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem, boolean isShiftClick, boolean isRightClick) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        playSound(Sound.UI_BUTTON_CLICK);

        // Check category selection
        for (LeaderboardCategory category : LeaderboardCategory.values()) {
            if (slot == category.getSlot()) {
                currentCategory = category;
                playSound(Sound.ITEM_BOOK_PAGE_TURN);
                open(); // Refresh
                return;
            }
        }

        switch (slot) {
            case 45: // Refresh
                playSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE);
                open();
                break;
            case 49: // Back or Your Rank
                plugin.getGUIManager().openMainMenu(player);
                break;
            case 53: // Close
                player.closeInventory();
                playSound(Sound.BLOCK_ENDER_CHEST_CLOSE);
                break;
        }
    }

    private static class LeaderboardEntry {
        UUID uuid;
        String username;
        int value;

        LeaderboardEntry(UUID uuid, String username, int value) {
            this.uuid = uuid;
            this.username = username;
            this.value = value;
        }
    }
}
