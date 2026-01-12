package com.fikjul.rpgjul;

import com.fikjul.rpgjul.commands.*;
import com.fikjul.rpgjul.listeners.*;
import com.fikjul.rpgjul.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for RPG Jul Plugin
 */
public class RPGJulPlugin extends JavaPlugin {
    
    // Managers
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;
    private RuneManager runeManager;
    private ClassManager classManager;
    private MobManager mobManager;
    private PartyManager partyManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        getLogger().info("Initializing RPG Jul Plugin...");
        
        // Initialize managers
        initializeManagers();
        
        // Register commands
        registerCommands();
        
        // Register event listeners
        registerListeners();
        
        getLogger().info("RPG Jul Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling RPG Jul Plugin...");
        
        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
        }
        
        // Close database connection
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("RPG Jul Plugin disabled successfully!");
    }

    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        playerDataManager = new PlayerDataManager(this, databaseManager);
        levelManager = new LevelManager(this, playerDataManager);
        runeManager = new RuneManager(this, playerDataManager, levelManager);
        classManager = new ClassManager(this, playerDataManager);
        mobManager = new MobManager(this);
        partyManager = new PartyManager(this, databaseManager);
    }

    /**
     * Register all commands
     */
    private void registerCommands() {
        getCommand("rpg").setExecutor(new RPGCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("p").setExecutor(new PartyChatCommand(this));
        getCommand("rpgadmin").setExecutor(new RPGAdminCommand(this));
        
        // Set tab completers
        getCommand("rpg").setTabCompleter(new RPGCommand(this));
        getCommand("party").setTabCompleter(new PartyCommand(this));
        getCommand("rpgadmin").setTabCompleter(new RPGAdminCommand(this));
    }

    /**
     * Register all event listeners
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new MobSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRegenerationListener(this), this);
    }

    // Getters for managers
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public RuneManager getRuneManager() {
        return runeManager;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }
}
