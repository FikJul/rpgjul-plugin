package com.fikjul.rpgjul.commands;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.Party;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Party chat command handler
 */
public class PartyChatCommand implements CommandExecutor {
    private final RPGJulPlugin plugin;

    public PartyChatCommand(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rpgjul.party.chat")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /p <message>");
            return true;
        }

        Party party = plugin.getPartyManager().getPlayerParty(player);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return true;
        }

        // Combine args into message
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            if (message.length() > 0) {
                message.append(" ");
            }
            message.append(arg);
        }

        // Format and broadcast
        String formattedMessage = ChatColor.GREEN + "[Party] " + ChatColor.WHITE + player.getName() + 
                ChatColor.GRAY + ": " + ChatColor.WHITE + message.toString();
        plugin.getPartyManager().broadcastToParty(party, formattedMessage);

        return true;
    }
}
