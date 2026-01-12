package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RPGClass;
import com.fikjul.rpgjul.enums.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class Showcase GUI - View and activate classes
 */
public class ClassShowcaseGUI extends BaseGUI {

    public ClassShowcaseGUI(RPGJulPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 54, "§6§lClasses");

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            player.sendMessage("§cFailed to load your data!");
            return;
        }

        // Display all classes in grid
        addClassItem(10, RPGClass.BLADEMASTER, Material.DIAMOND_SWORD, data);
        addClassItem(11, RPGClass.BERSERKER, Material.DIAMOND_AXE, data);
        addClassItem(12, RPGClass.SHARPSHOOTER, Material.BOW, data);
        addClassItem(13, RPGClass.PALADIN, Material.GOLDEN_HELMET, data);
        addClassItem(14, RPGClass.SHADOW_ASSASSIN, Material.IRON_SWORD, data);
        addClassItem(15, RPGClass.FORTRESS, Material.IRON_CHESTPLATE, data);
        addClassItem(16, RPGClass.ARTISAN, Material.DIAMOND_PICKAXE, data);
        addClassItem(19, RPGClass.RANGER, Material.CROSSBOW, data);

        // Current Class Info (Slot 40)
        if (data.getActiveClass() != null) {
            List<String> currentLore = new ArrayList<>();
            currentLore.add("§7Current: §6" + data.getActiveClass().getDisplayName());
            currentLore.add("§7Bonus: §a" + data.getActiveClass().getBonus());
            currentLore.add("§7");
            currentLore.add("§e§lClick to deactivate");
            inventory.setItem(40, createPlayerHead(player, "§a§lCurrent Class", currentLore));
        }

        // Back (Slot 49)
        inventory.setItem(49, createItem(Material.ARROW, "§e⬅ Back"));

        // Close (Slot 53)
        inventory.setItem(53, createItem(Material.BARRIER, "§c§lClose"));

        playSound(Sound.BLOCK_ENDER_CHEST_OPEN);
        player.openInventory(inventory);
    }

    private void addClassItem(int slot, RPGClass rpgClass, Material material, PlayerData data) {
        boolean unlocked = rpgClass.meetsRequirements(data.getRuneLevels());
        boolean isCurrent = data.getActiveClass() == rpgClass;

        List<String> lore = new ArrayList<>();
        
        if (unlocked) {
            lore.add("§a§l✓ UNLOCKED");
        } else {
            lore.add("§c§l🔒 LOCKED");
        }
        lore.add("§7");
        lore.add("§7Requirements:");
        
        Map<RuneType, Integer> requirements = rpgClass.getRequirements();
        int metRequirements = 0;
        for (Map.Entry<RuneType, Integer> req : requirements.entrySet()) {
            int playerLevel = data.getRuneLevel(req.getKey());
            int required = req.getValue();
            
            if (playerLevel >= required) {
                lore.add("  §a✓ " + req.getKey().getDisplayName() + " Level " + playerLevel + "/" + required);
                metRequirements++;
            } else {
                lore.add("  §c✗ " + req.getKey().getDisplayName() + " Level " + playerLevel + "/" + required);
            }
        }
        
        lore.add("§7");
        lore.add("§7Bonus: " + (unlocked ? "§6" : "§8") + rpgClass.getBonus());
        lore.add("§7");
        
        if (!unlocked) {
            lore.add("§7Progress: " + metRequirements + "/" + requirements.size() + " requirements met");
            lore.add(createProgressBar(metRequirements, requirements.size(), 7));
        } else if (isCurrent) {
            lore.add("§a§l✓ CURRENT CLASS");
        } else {
            lore.add("§e§lClick to activate!");
        }

        ItemStack item;
        if (isCurrent) {
            item = createGlowingItem(material, "§6§l" + rpgClass.getDisplayName(), lore);
        } else if (unlocked) {
            item = createGlowingItem(material, "§6§l" + rpgClass.getDisplayName(), lore);
        } else {
            // For locked classes, use a stone variant or gray material
            Material lockedMaterial = material;
            item = createItem(lockedMaterial, "§c§l🔒 " + rpgClass.getDisplayName(), lore);
        }
        
        inventory.setItem(slot, item);
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem, boolean isShiftClick, boolean isRightClick) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        RPGClass clickedClass = null;
        switch (slot) {
            case 10: clickedClass = RPGClass.BLADEMASTER; break;
            case 11: clickedClass = RPGClass.BERSERKER; break;
            case 12: clickedClass = RPGClass.SHARPSHOOTER; break;
            case 13: clickedClass = RPGClass.PALADIN; break;
            case 14: clickedClass = RPGClass.SHADOW_ASSASSIN; break;
            case 15: clickedClass = RPGClass.FORTRESS; break;
            case 16: clickedClass = RPGClass.ARTISAN; break;
            case 19: clickedClass = RPGClass.RANGER; break;
            case 40: // Deactivate current class
                if (data.getActiveClass() != null) {
                    data.setActiveClass(null);
                    player.sendMessage("§e§lClass deactivated!");
                    playSound(Sound.BLOCK_NOTE_BLOCK_BASS);
                    open(); // Refresh
                }
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

        if (clickedClass != null) {
            if (data.getActiveClass() == clickedClass) {
                // Already active, deactivate
                data.setActiveClass(null);
                player.sendMessage("§e§lClass deactivated!");
                playSound(Sound.BLOCK_NOTE_BLOCK_BASS);
            } else if (plugin.getClassManager().activateClass(player, clickedClass)) {
                // Successfully activated
                player.sendMessage("§a§lActivated class: §6" + clickedClass.getDisplayName());
                playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
                
                // Particles
                if (plugin.getConfig().getBoolean("gui.particles-enabled", true)) {
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, 
                        player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5);
                }
            } else {
                // Cannot activate
                player.sendMessage("§c§lYou don't meet the requirements for this class!");
                playSound(Sound.ENTITY_VILLAGER_NO);
            }
            open(); // Refresh
        }
    }
}
