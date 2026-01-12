package com.fikjul.rpgjul.commands;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RuneType;
import org.bukkit.Bukkit;
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
 * Admin command handler
 */
public class RPGAdminCommand implements CommandExecutor, TabCompleter {
    private final RPGJulPlugin plugin;

    public RPGAdminCommand(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rpgjul.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin <setlevel|giverune|setrunepoints|reset|resetrunes|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setlevel":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin setlevel <player> <level>");
                    return true;
                }
                handleSetLevel(sender, args[1], args[2]);
                break;
            case "giverune":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin giverune <player> <rune> <level>");
                    return true;
                }
                handleGiveRune(sender, args[1], args[2], args[3]);
                break;
            case "setrunepoints":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin setrunepoints <player> <amount>");
                    return true;
                }
                handleSetRunePoints(sender, args[1], args[2]);
                break;
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin reset <player>");
                    return true;
                }
                handleReset(sender, args[1]);
                break;
            case "resetrunes":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin resetrunes <player>");
                    return true;
                }
                handleResetRunes(sender, args[1]);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                break;
        }

        return true;
    }

    private void handleSetLevel(CommandSender sender, String playerName, String levelStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        try {
            int level = Integer.parseInt(levelStr);
            if (level < 1 || level > plugin.getConfig().getInt("leveling.max-level", 100)) {
                sender.sendMessage(ChatColor.RED + "Invalid level!");
                return;
            }

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "Failed to load player data!");
                return;
            }

            data.setTotalLevel(level);
            plugin.getLevelManager().applyStatBonuses(target);

            sender.sendMessage(ChatColor.GREEN + "✓ Set " + target.getName() + "'s level to " + level);
            target.sendMessage(ChatColor.GREEN + "Your level has been set to " + level + " by an admin!");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level number!");
        }
    }

    private void handleGiveRune(CommandSender sender, String playerName, String runeName, String levelStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        try {
            RuneType rune = RuneType.valueOf(runeName.toUpperCase());
            int level = Integer.parseInt(levelStr);
            
            if (level < 0 || level > plugin.getConfig().getInt("runes.max-level", 100)) {
                sender.sendMessage(ChatColor.RED + "Invalid level!");
                return;
            }

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "Failed to load player data!");
                return;
            }

            data.setRuneLevel(rune, level);
            plugin.getLevelManager().applyStatBonuses(target);

            sender.sendMessage(ChatColor.GREEN + "✓ Set " + target.getName() + "'s " + rune.getDisplayName() + 
                    " to level " + level);
            target.sendMessage(ChatColor.GREEN + "Your " + rune.getDisplayName() + 
                    " has been set to level " + level + " by an admin!");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid rune name or level!");
        }
    }

    private void handleSetRunePoints(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);
            if (amount < 0) {
                sender.sendMessage(ChatColor.RED + "Amount must be positive!");
                return;
            }

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "Failed to load player data!");
                return;
            }

            data.setAvailableRunePoints(amount);

            sender.sendMessage(ChatColor.GREEN + "✓ Set " + target.getName() + "'s rune points to " + amount);
            target.sendMessage(ChatColor.GREEN + "Your rune points have been set to " + amount + " by an admin!");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number!");
        }
    }

    private void handleReset(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        if (data == null) {
            sender.sendMessage(ChatColor.RED + "Failed to load player data!");
            return;
        }

        // Create new player data
        PlayerData newData = new PlayerData(target.getUniqueId(), target.getName());
        plugin.getPlayerDataManager().getPlayerData(target.getUniqueId()).setTotalLevel(1);
        plugin.getPlayerDataManager().getPlayerData(target.getUniqueId()).setCurrentXP(0);
        plugin.getPlayerDataManager().getPlayerData(target.getUniqueId()).setAvailableRunePoints(0);
        plugin.getPlayerDataManager().getPlayerData(target.getUniqueId()).setActiveClass(null);
        
        for (RuneType rune : RuneType.values()) {
            data.setRuneLevel(rune, 0);
        }

        plugin.getLevelManager().applyStatBonuses(target);

        sender.sendMessage(ChatColor.GREEN + "✓ Reset all data for " + target.getName());
        target.sendMessage(ChatColor.YELLOW + "Your RPG data has been reset by an admin!");
    }

    private void handleResetRunes(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (plugin.getRuneManager().resetRunes(target, true)) {
            sender.sendMessage(ChatColor.GREEN + "✓ Reset runes for " + target.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to reset runes!");
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "✓ Configuration reloaded!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("setlevel", "giverune", "setrunepoints", "reset", "resetrunes", "reload");
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                players.add(p.getName());
            }
            return players;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("giverune")) {
            List<String> runes = new ArrayList<>();
            for (RuneType rune : RuneType.values()) {
                runes.add(rune.name());
            }
            return runes;
        }
        return new ArrayList<>();
    }
}
