package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Rune Management GUI - Allocate and manage runes
 */
public class RuneManagementGUI extends BaseGUI {

    public RuneManagementGUI(RPGJulPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 54, "§d§lRune Management");

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            player.sendMessage("§cFailed to load your data!");
            return;
        }

        int maxLevel = plugin.getConfig().getInt("runes.max-level", 100);

        // Available Points (Slot 4)
        List<String> pointsLore = new ArrayList<>();
        pointsLore.add("§f" + data.getAvailableRunePoints() + " points");
        ItemStack pointsItem = data.getAvailableRunePoints() > 0
            ? createGlowingItem(Material.NETHER_STAR, "§d§lAvailable Rune Points", pointsLore)
            : createItem(Material.NETHER_STAR, "§d§lAvailable Rune Points", pointsLore);
        inventory.setItem(4, pointsItem);

        // Left Column Runes
        addRuneItem(10, RuneType.VITALITY, Material.RED_DYE, data, maxLevel);
        addRuneItem(19, RuneType.BLADESTORM, Material.IRON_SWORD, data, maxLevel);
        addRuneItem(28, RuneType.WARBRINGER, Material.IRON_AXE, data, maxLevel);
        addRuneItem(37, RuneType.RESTORATION, Material.GOLDEN_APPLE, data, maxLevel);
        addRuneItem(46, RuneType.SWIFTWIND, Material.FEATHER, data, maxLevel);

        // Right Column Runes
        addRuneItem(14, RuneType.AEGIS, Material.IRON_CHESTPLATE, data, maxLevel);
        addRuneItem(23, RuneType.MARKSMAN, Material.BOW, data, maxLevel);
        addRuneItem(32, RuneType.GUARDIAN, Material.SHIELD, data, maxLevel);
        addRuneItem(41, RuneType.ARCHITECT, Material.DIAMOND_PICKAXE, data, maxLevel);
        addRuneItem(50, RuneType.ANGLER, Material.FISHING_ROD, data, maxLevel);

        // Reset Runes (Slot 45)
        long cooldownDays = plugin.getConfig().getLong("runes.reset-cooldown-days", 7);
        long cooldownMs = TimeUnit.DAYS.toMillis(cooldownDays);
        long timeSinceReset = System.currentTimeMillis() - data.getLastRuneReset();
        
        List<String> resetLore = new ArrayList<>();
        if (timeSinceReset < cooldownMs && data.getLastRuneReset() > 0) {
            long remainingMs = cooldownMs - timeSinceReset;
            long remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMs);
            long remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMs) % 24;
            resetLore.add("§7Cooldown: §c" + remainingDays + "d " + remainingHours + "h remaining");
        } else {
            resetLore.add("§7Cooldown: §aReady");
        }
        resetLore.add("§7Click to reset all runes");
        resetLore.add("§cThis action requires confirmation!");
        inventory.setItem(45, createItem(Material.TNT, "§c§l⚠ Reset All Runes", resetLore));

        // Back (Slot 49)
        inventory.setItem(49, createItem(Material.ARROW, "§e⬅ Back"));

        // Close (Slot 53)
        inventory.setItem(53, createItem(Material.BARRIER, "§c§lClose"));

        playSound(Sound.BLOCK_ENDER_CHEST_OPEN);
        player.openInventory(inventory);
    }

    private void addRuneItem(int slot, RuneType rune, Material material, PlayerData data, int maxLevel) {
        int currentLevel = data.getRuneLevel(rune);
        double currentBonus = rune.calculateBonus(currentLevel);
        double nextBonus = rune.calculateBonus(currentLevel + 1);
        
        String color = getRuneColor(currentLevel, maxLevel);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Level: §f" + currentLevel + "/" + maxLevel);
        
        // Extract unit from description (e.g., " HP", "%", " HP/5s")
        String description = rune.getDescription();
        int lastSpaceIndex = description.lastIndexOf(" ");
        String unit = lastSpaceIndex >= 0 ? description.substring(lastSpaceIndex) : "";
        
        lore.add("§7Current Bonus: §a" + String.format("%.1f", currentBonus) + unit);
        
        if (currentLevel < maxLevel) {
            lore.add("§7Next Level: §b" + String.format("%.1f", nextBonus) + unit);
        }
        
        lore.add("§7");
        lore.add(createProgressBar(currentLevel, maxLevel, 10));
        lore.add("§7");
        
        if (currentLevel >= maxLevel) {
            lore.add("§c§lMax Level!");
        } else if (data.getAvailableRunePoints() > 0) {
            lore.add("§e§lClick to upgrade (1 Point)");
            lore.add("§7Shift-Click to upgrade multiple");
        } else {
            lore.add("§c§lNo points available!");
        }

        ItemStack item;
        if (currentLevel >= maxLevel) {
            item = createGlowingItem(material, color + "§l" + rune.getDisplayName(), lore);
        } else {
            item = createItem(material, color + "§l" + rune.getDisplayName(), lore);
        }
        
        inventory.setItem(slot, item);
    }

    private String getRuneColor(int level, int maxLevel) {
        if (level >= maxLevel) return "§6"; // Gold
        if (level >= 70) return "§a"; // Green
        if (level >= 30) return "§e"; // Yellow
        return "§c"; // Red
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem, boolean isShiftClick, boolean isRightClick) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Check which rune was clicked
        RuneType clickedRune = null;
        switch (slot) {
            case 10: clickedRune = RuneType.VITALITY; break;
            case 19: clickedRune = RuneType.BLADESTORM; break;
            case 28: clickedRune = RuneType.WARBRINGER; break;
            case 37: clickedRune = RuneType.RESTORATION; break;
            case 46: clickedRune = RuneType.SWIFTWIND; break;
            case 14: clickedRune = RuneType.AEGIS; break;
            case 23: clickedRune = RuneType.MARKSMAN; break;
            case 32: clickedRune = RuneType.GUARDIAN; break;
            case 41: clickedRune = RuneType.ARCHITECT; break;
            case 50: clickedRune = RuneType.ANGLER; break;
            case 45: // Reset
                openResetConfirmation();
                return;
            case 49: // Back
                playSound(Sound.UI_BUTTON_CLICK);
                plugin.getGUIManager().openMainMenu(player);
                return;
            case 53: // Close
                playSound(Sound.UI_BUTTON_CLICK);
                player.closeInventory();
                playSound(Sound.BLOCK_ENDER_CHEST_CLOSE);
                return;
        }

        if (clickedRune != null) {
            if (isShiftClick) {
                // Allocate 10 points
                allocateMultiple(clickedRune, 10);
            } else {
                // Allocate 1 point
                allocateSingle(clickedRune);
            }
            open(); // Refresh GUI
        }
    }

    private void allocateSingle(RuneType rune) {
        if (plugin.getRuneManager().allocateRune(player, rune)) {
            playSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE);
        } else {
            playSound(Sound.ENTITY_VILLAGER_NO);
        }
    }

    private void allocateMultiple(RuneType rune, int count) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int allocated = 0;
        for (int i = 0; i < count && i < data.getAvailableRunePoints(); i++) {
            if (plugin.getRuneManager().allocateRune(player, rune)) {
                allocated++;
            } else {
                break;
            }
        }

        if (allocated > 0) {
            playSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE);
            player.sendMessage("§a§lAllocated " + allocated + " points to " + rune.getDisplayName() + "!");
        } else {
            playSound(Sound.ENTITY_VILLAGER_NO);
        }
    }

    private void openResetConfirmation() {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Check cooldown
        long cooldownDays = plugin.getConfig().getLong("runes.reset-cooldown-days", 7);
        long cooldownMs = TimeUnit.DAYS.toMillis(cooldownDays);
        long timeSinceReset = System.currentTimeMillis() - data.getLastRuneReset();
        
        if (timeSinceReset < cooldownMs && data.getLastRuneReset() > 0) {
            player.sendMessage("§cYou cannot reset your runes yet!");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }

        playSound(Sound.UI_BUTTON_CLICK);
        new RuneResetConfirmationGUI(plugin, player).open();
    }
}
