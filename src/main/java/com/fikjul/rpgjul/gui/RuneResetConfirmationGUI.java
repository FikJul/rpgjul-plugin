package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Confirmation GUI for resetting runes
 */
public class RuneResetConfirmationGUI extends BaseGUI {

    public RuneResetConfirmationGUI(RPGJulPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, "§c§lConfirm Rune Reset");

        // Warning (Slot 4)
        List<String> warningLore = new ArrayList<>();
        warningLore.add("§7This will reset ALL runes to 0");
        warningLore.add("§7You will receive all spent points back");
        warningLore.add("§7Your active class will be deactivated");
        warningLore.add("§7");
        warningLore.add("§c§lThis cannot be undone!");
        inventory.setItem(4, createItem(Material.PAPER, "§e§l⚠ Warning", warningLore));

        // Confirm (Slot 10)
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add("§7Click to confirm reset");
        inventory.setItem(10, createItem(Material.GREEN_WOOL, "§a§l✓ CONFIRM RESET", confirmLore));

        // Cancel (Slot 16)
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add("§7Click to cancel");
        inventory.setItem(16, createItem(Material.RED_WOOL, "§c§l✗ CANCEL", cancelLore));

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
            case 10: // Confirm
                if (plugin.getRuneManager().resetRunes(player, false)) {
                    player.sendMessage("§a§lRunes have been reset!");
                    playSound(Sound.BLOCK_ANVIL_LAND);
                } else {
                    playSound(Sound.ENTITY_VILLAGER_NO);
                }
                plugin.getGUIManager().openRuneGUI(player);
                break;
            case 16: // Cancel
                plugin.getGUIManager().openRuneGUI(player);
                break;
        }
    }
}
