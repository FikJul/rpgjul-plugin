package com.fikjul.rpgjul.listeners;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.gui.BaseGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles inventory click events for RPG GUIs
 */
public class InventoryClickListener implements Listener {
    private final RPGJulPlugin plugin;

    public InventoryClickListener(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        BaseGUI gui = plugin.getGUIManager().getOpenGUI(player);

        if (gui == null) {
            return;
        }

        // Check if the clicked inventory belongs to the GUI
        if (!event.getInventory().equals(gui.getInventory())) {
            return;
        }

        // Cancel the event to prevent item movement
        event.setCancelled(true);

        // Get click details
        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();
        boolean isShiftClick = event.isShiftClick();
        boolean isRightClick = event.isRightClick();

        // Handle the click
        gui.handleClick(slot, clickedItem, isShiftClick, isRightClick);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        BaseGUI gui = plugin.getGUIManager().getOpenGUI(player);

        if (gui != null && event.getInventory().equals(gui.getInventory())) {
            plugin.getGUIManager().closeGUI(player);
        }
    }
}
