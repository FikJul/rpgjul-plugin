package com.fikjul.rpgjul.commands;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.Party;
import com.fikjul.rpgjul.data.PartyInvitation;
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
 * Party command handler
 */
public class PartyCommand implements CommandExecutor, TabCompleter {
    private final RPGJulPlugin plugin;

    public PartyCommand(RPGJulPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rpgjul.party")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /party <create|invite|accept|leave|kick|disband|info>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
                    return true;
                }
                handleInvite(player, args[1]);
                break;
            case "accept":
                handleAccept(player);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party kick <player>");
                    return true;
                }
                handleKick(player, args[1]);
                break;
            case "disband":
                handleDisband(player);
                break;
            case "info":
                plugin.getGUIManager().openPartyGUI(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use: /party <create|invite|accept|leave|kick|disband|info>");
                break;
        }

        return true;
    }

    private void handleCreate(Player player) {
        Party party = plugin.getPartyManager().createParty(player);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are already in a party!");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "✓ Party created! You are now the party leader.");
        
        if (plugin.getConfig().getBoolean("gui.sounds-enabled", true)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        }
    }

    private void handleInvite(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot invite yourself!");
            return;
        }

        if (plugin.getPartyManager().invitePlayer(player, target)) {
            player.sendMessage(ChatColor.GREEN + "✓ Invited " + ChatColor.AQUA + target.getName() + 
                    ChatColor.GREEN + " to your party!");
            target.sendMessage(ChatColor.YELLOW + "You have been invited to " + ChatColor.AQUA + 
                    player.getName() + ChatColor.YELLOW + "'s party!");
            target.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.AQUA + "/party accept" + 
                    ChatColor.YELLOW + " to join!");
        }
    }

    private void handleAccept(Player player) {
        PartyInvitation invitation = plugin.getPartyManager().getPendingInvitation(player);
        if (invitation == null) {
            player.sendMessage(ChatColor.RED + "You don't have any pending party invitations!");
            return;
        }

        if (plugin.getPartyManager().acceptInvitation(player)) {
            player.sendMessage(ChatColor.GREEN + "✓ You joined the party!");
            
            // Notify party members
            Party party = plugin.getPartyManager().getPlayerParty(player);
            if (party != null) {
                String message = ChatColor.YELLOW + player.getName() + " joined the party!";
                plugin.getPartyManager().broadcastToParty(party, message);
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to join party. The invitation may have expired or the party is full.");
        }
    }

    private void handleLeave(Player player) {
        Party party = plugin.getPartyManager().getPlayerParty(player);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        String message = ChatColor.YELLOW + player.getName() + " left the party!";
        
        if (plugin.getPartyManager().leaveParty(player)) {
            player.sendMessage(ChatColor.GREEN + "✓ You left the party.");
            
            // Notify remaining members if party still exists
            if (party.getSize() > 1) {
                plugin.getPartyManager().broadcastToParty(party, message);
            }
        }
    }

    private void handleKick(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (plugin.getPartyManager().kickPlayer(player, target)) {
            player.sendMessage(ChatColor.GREEN + "✓ Kicked " + ChatColor.AQUA + target.getName() + 
                    ChatColor.GREEN + " from the party!");
            target.sendMessage(ChatColor.RED + "You were kicked from the party!");
            
            // Notify party
            Party party = plugin.getPartyManager().getPlayerParty(player);
            if (party != null) {
                String message = ChatColor.YELLOW + target.getName() + " was kicked from the party!";
                plugin.getPartyManager().broadcastToParty(party, message);
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to kick player. You must be the party leader!");
        }
    }

    private void handleDisband(Player player) {
        Party party = plugin.getPartyManager().getPlayerParty(player);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        String message = ChatColor.RED + "The party has been disbanded!";
        plugin.getPartyManager().broadcastToParty(party, message);

        if (plugin.getPartyManager().disbandParty(player)) {
            player.sendMessage(ChatColor.GREEN + "✓ Party disbanded.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to disband party. You must be the party leader!");
        }
    }

    private void handleInfo(Player player) {
        Party party = plugin.getPartyManager().getPlayerParty(player);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "========== " + ChatColor.AQUA + "Party Info" + ChatColor.GOLD + " ==========");
        player.sendMessage(ChatColor.YELLOW + "Members (" + party.getSize() + "):");
        
        for (java.util.UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            String name = member != null ? member.getName() : "Unknown";
            String status = member != null && member.isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline";
            String leader = party.isLeader(memberId) ? ChatColor.GOLD + " ⭐" : "";
            player.sendMessage(ChatColor.WHITE + "  • " + name + leader + " " + status);
        }
        
        player.sendMessage(ChatColor.GOLD + "================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "leave", "kick", "disband", "info");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick"))) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                players.add(p.getName());
            }
            return players;
        }
        return new ArrayList<>();
    }
}
