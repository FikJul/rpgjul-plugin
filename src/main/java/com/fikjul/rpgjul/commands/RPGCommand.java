package com.fikjul.rpgjul.commands;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main RPG command handler
 */
public class RPGCommand implements CommandExecutor, TabCompleter {
    private final RPGJulPlugin plugin;

    public RPGCommand(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rpgjul.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            // Open main GUI
            plugin.getGUIManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stats":
                plugin.getGUIManager().openStatsGUI(player);
                break;
            case "runes":
                plugin.getGUIManager().openRuneGUI(player);
                break;
            case "class":
                plugin.getGUIManager().openClassGUI(player);
                break;
            case "leaderboard":
                plugin.getGUIManager().openLeaderboardGUI(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use: /rpg [stats|runes|class|leaderboard]");
                break;
        }

        return true;
    }

    /**
     * Send stats message to player
     */
    private void sendStatsMessage(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "Failed to load your data!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "========== " + ChatColor.AQUA + "RPG Stats" + ChatColor.GOLD + " ==========");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + data.getTotalLevel());
        player.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.WHITE + 
                String.format("%.1f", data.getCurrentXP()) + "/" + String.format("%.1f", data.getXpToNextLevel()));
        player.sendMessage(ChatColor.YELLOW + "Rune Points: " + ChatColor.WHITE + data.getAvailableRunePoints());
        player.sendMessage(ChatColor.YELLOW + "Active Class: " + ChatColor.WHITE + 
                (data.getActiveClass() != null ? data.getActiveClass().getDisplayName() : "None"));
        player.sendMessage(ChatColor.GOLD + "================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("stats", "runes", "class", "leaderboard");
        }
        return new ArrayList<>();
    }
}
