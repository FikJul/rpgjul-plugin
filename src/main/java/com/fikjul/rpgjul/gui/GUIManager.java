package com.fikjul.rpgjul.gui;

import com.fikjul.rpgjul.RPGJulPlugin;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all GUI instances and player GUI state
 */
public class GUIManager {
    private final RPGJulPlugin plugin;
    private final ConcurrentHashMap<UUID, BaseGUI> openGUIs;

    public GUIManager(RPGJulPlugin plugin) {
        this.plugin = plugin;
        this.openGUIs = new ConcurrentHashMap<>();
    }

    /**
     * Open main menu for a player
     */
    public void openMainMenu(Player player) {
        MainMenuGUI gui = new MainMenuGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * Open stats GUI for a player
     */
    public void openStatsGUI(Player player) {
        StatsGUI gui = new StatsGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * Open rune management GUI for a player
     */
    public void openRuneGUI(Player player) {
        RuneManagementGUI gui = new RuneManagementGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * Open class showcase GUI for a player
     */
    public void openClassGUI(Player player) {
        ClassShowcaseGUI gui = new ClassShowcaseGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * Open leaderboard GUI for a player
     */
    public void openLeaderboardGUI(Player player) {
        LeaderboardGUI gui = new LeaderboardGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * Open party GUI for a player
     */
    public void openPartyGUI(Player player) {
        PartyGUI gui = new PartyGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * Open a GUI and track it
     */
    private void openGUI(Player player, BaseGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
        gui.open();
    }

    /**
     * Get the currently open GUI for a player
     */
    public BaseGUI getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }

    /**
     * Close a GUI for a player
     */
    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
    }

    /**
     * Check if player has a GUI open
     */
    public boolean hasGUIOpen(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
}
