package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all GUI implementations
 */
public abstract class BaseGUI {
    protected final RPGJulPlugin plugin;
    protected final Player player;
    protected Inventory inventory;

    public BaseGUI(RPGJulPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Initialize and open the GUI
     */
    public abstract void open();

    /**
     * Handle click events
     */
    public abstract void handleClick(int slot, ItemStack clickedItem, boolean isShiftClick, boolean isRightClick);

    /**
     * Get the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Create an item with name and lore
     */
    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create an item with name
     */
    protected ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }

    /**
     * Create a glowing item
     */
    protected ItemStack createGlowingItem(Material material, String name, List<String> lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create a player head item
     */
    protected ItemStack createPlayerHead(Player player, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    /**
     * Create a progress bar string
     */
    protected String createProgressBar(int current, int max, int barLength) {
        double percentage = max > 0 ? (double) current / max : 0;
        int filled = (int) (percentage * barLength);
        
        StringBuilder bar = new StringBuilder("§7Progress: §a");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("▰");
            } else {
                bar.append("§7▱");
            }
        }
        bar.append(" §f").append((int)(percentage * 100)).append("%");
        
        return bar.toString();
    }

    /**
     * Play a sound for the player
     */
    protected void playSound(Sound sound) {
        if (plugin.getConfig().getBoolean("gui.sounds-enabled", true)) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    /**
     * Fill empty slots with a filler item
     */
    protected void fillEmptySlots() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    /**
     * Get color based on level/percentage
     */
    protected String getColorByLevel(int level, int max) {
        double percentage = (double) level / max;
        if (percentage >= 0.75) return "§a"; // Green
        if (percentage >= 0.50) return "§e"; // Yellow
        if (percentage >= 0.25) return "§6"; // Gold
        return "§c"; // Red
    }
}
