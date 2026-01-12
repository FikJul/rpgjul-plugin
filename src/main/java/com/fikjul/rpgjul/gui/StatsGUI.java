package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RPGClass;
import com.fikjul.rpgjul.enums.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Stats Display GUI - Shows all player stats with bonuses
 */
public class StatsGUI extends BaseGUI {

    public StatsGUI(RPGJulPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 54, "§b§lYour Stats");

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            player.sendMessage("§cFailed to load your data!");
            return;
        }

        // Row 1: Health & Combat
        // HP Stat (Slot 10)
        int vitalityLevel = data.getRuneLevel(RuneType.VITALITY);
        double hpBonus = RuneType.VITALITY.calculateBonus(vitalityLevel);
        List<String> hpLore = new ArrayList<>();
        hpLore.add("§7Base HP: §f20.0");
        hpLore.add("§7Vitality Rune Bonus: §a+" + String.format("%.1f", hpBonus) + " HP");
        hpLore.add("§7Total: §c" + String.format("%.1f", 20.0 + hpBonus) + " HP");
        inventory.setItem(10, createItem(Material.RED_DYE, "§c❤ Health", hpLore));

        // Sword Damage (Slot 11)
        int bladestormLevel = data.getRuneLevel(RuneType.BLADESTORM);
        double swordBonus = RuneType.BLADESTORM.calculateBonus(bladestormLevel);
        double swordClassBonus = 0.0;
        List<String> swordLore = new ArrayList<>();
        swordLore.add("§7Bladestorm Rune: §a+" + String.format("%.0f", swordBonus) + "%");
        swordLore.add("§7Class Bonus: §6+" + String.format("%.0f", swordClassBonus) + "%");
        swordLore.add("§7Total: §e+" + String.format("%.0f", swordBonus + swordClassBonus) + "%");
        inventory.setItem(11, createItem(Material.IRON_SWORD, "§e⚔ Sword Damage", swordLore));

        // Axe Damage (Slot 12)
        int warbringerLevel = data.getRuneLevel(RuneType.WARBRINGER);
        double axeBonus = RuneType.WARBRINGER.calculateBonus(warbringerLevel);
        List<String> axeLore = new ArrayList<>();
        axeLore.add("§7Warbringer Rune: §a+" + String.format("%.0f", axeBonus) + "%");
        axeLore.add("§7Total: §6+" + String.format("%.0f", axeBonus) + "%");
        inventory.setItem(12, createItem(Material.IRON_AXE, "§6🪓 Axe Damage", axeLore));

        // Bow Damage (Slot 13)
        int marksmanLevel = data.getRuneLevel(RuneType.MARKSMAN);
        double bowBonus = RuneType.MARKSMAN.calculateBonus(marksmanLevel);
        double bowClassBonus = 0.0;
        if (data.getActiveClass() == RPGClass.RANGER) {
            bowClassBonus = 20.0;
        }
        List<String> bowLore = new ArrayList<>();
        bowLore.add("§7Marksman Rune: §a+" + String.format("%.0f", bowBonus) + "%");
        bowLore.add("§7Class Bonus: §6+" + String.format("%.0f", bowClassBonus) + "%");
        bowLore.add("§7Total: §b+" + String.format("%.0f", bowBonus + bowClassBonus) + "%");
        inventory.setItem(13, createItem(Material.BOW, "§b🏹 Bow Damage", bowLore));

        // Row 2: Defense & Mobility
        // Defense (Slot 19)
        int aegisLevel = data.getRuneLevel(RuneType.AEGIS);
        double defenseBonus = RuneType.AEGIS.calculateBonus(aegisLevel);
        List<String> defenseLore = new ArrayList<>();
        defenseLore.add("§7Aegis Rune: §a+" + String.format("%.1f", defenseBonus) + "% Damage Reduction");
        defenseLore.add("§7Class Bonus: §6+0.0%");
        defenseLore.add("§7Total: §9+" + String.format("%.1f", defenseBonus) + "%");
        inventory.setItem(19, createItem(Material.IRON_CHESTPLATE, "§9🛡 Defense", defenseLore));

        // Regeneration (Slot 20)
        int restorationLevel = data.getRuneLevel(RuneType.RESTORATION);
        double regenBonus = RuneType.RESTORATION.calculateBonus(restorationLevel);
        List<String> regenLore = new ArrayList<>();
        regenLore.add("§7Restoration Rune: §a+" + String.format("%.1f", regenBonus) + " HP/5s");
        regenLore.add("§7Total: §a+" + String.format("%.1f", regenBonus) + " HP/5s");
        inventory.setItem(20, createItem(Material.GOLDEN_APPLE, "§a🔄 Health Regeneration", regenLore));

        // Speed (Slot 21)
        int swiftwindLevel = data.getRuneLevel(RuneType.SWIFTWIND);
        double speedBonus = RuneType.SWIFTWIND.calculateBonus(swiftwindLevel);
        List<String> speedLore = new ArrayList<>();
        speedLore.add("§7Swiftwind Rune: §a+" + String.format("%.1f", speedBonus) + "%");
        speedLore.add("§7Total: §f+" + String.format("%.1f", speedBonus) + "%");
        inventory.setItem(21, createItem(Material.FEATHER, "§f🏃 Movement Speed", speedLore));

        // Shield (Slot 22)
        int guardianLevel = data.getRuneLevel(RuneType.GUARDIAN);
        double shieldBonus = RuneType.GUARDIAN.calculateBonus(guardianLevel);
        List<String> shieldLore = new ArrayList<>();
        shieldLore.add("§7Guardian Rune: §a+" + String.format("%.1f", shieldBonus) + "%");
        shieldLore.add("§7Total: §3+" + String.format("%.1f", shieldBonus) + "%");
        inventory.setItem(22, createItem(Material.SHIELD, "§3🛡️ Shield Effectiveness", shieldLore));

        // Row 3: Skills
        // Mining (Slot 28)
        int architectLevel = data.getRuneLevel(RuneType.ARCHITECT);
        double miningBonus = RuneType.ARCHITECT.calculateBonus(architectLevel);
        List<String> miningLore = new ArrayList<>();
        miningLore.add("§7Architect Rune: §a+" + String.format("%.1f", miningBonus) + "%");
        miningLore.add("§7Total: §7+" + String.format("%.1f", miningBonus) + "%");
        inventory.setItem(28, createItem(Material.DIAMOND_PICKAXE, "§7⛏ Mining Speed", miningLore));

        // Fishing (Slot 29)
        int anglerLevel = data.getRuneLevel(RuneType.ANGLER);
        double fishingBonus = RuneType.ANGLER.calculateBonus(anglerLevel);
        List<String> fishingLore = new ArrayList<>();
        fishingLore.add("§7Angler Rune: §a+" + String.format("%.0f", fishingBonus) + "%");
        fishingLore.add("§7Total: §b+" + String.format("%.0f", fishingBonus) + "%");
        inventory.setItem(29, createItem(Material.FISHING_ROD, "§b🎣 Fishing Luck", fishingLore));

        // Row 4: Class Info
        if (data.getActiveClass() != null) {
            RPGClass activeClass = data.getActiveClass();
            List<String> classLore = new ArrayList<>();
            classLore.add("§7Active Class: §6" + activeClass.getDisplayName());
            classLore.add("§7Bonus: §a" + activeClass.getBonus());
            inventory.setItem(40, createItem(Material.ENCHANTED_BOOK, "§6§lClass Bonus", classLore));
        }

        // Bottom Row
        // Back Button (Slot 49)
        inventory.setItem(49, createItem(Material.ARROW, "§e⬅ Back"));

        // Close (Slot 53)
        inventory.setItem(53, createItem(Material.BARRIER, "§c§lClose"));

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
