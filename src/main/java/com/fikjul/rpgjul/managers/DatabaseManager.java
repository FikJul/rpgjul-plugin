package com.fikjul.rpgjul.managers;

import com.fikjul.rpgjul.RPGJulPlugin;
import com.fikjul.rpgjul.data.Party;
import com.fikjul.rpgjul.data.PlayerData;
import com.fikjul.rpgjul.enums.RPGClass;
import com.fikjul.rpgjul.enums.RuneType;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Manages SQLite database operations for player data and parties
 */
public class DatabaseManager {
    private final RPGJulPlugin plugin;
    private Connection connection;
    private final File databaseFile;

    public DatabaseManager(RPGJulPlugin plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "rpg_data.db");
    }

    /**
     * Initialize database connection and create tables
     */
    public void initialize() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            
            createTables();
            plugin.getLogger().info("Database initialized successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create all necessary database tables
     */
    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Players table
        stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY," +
                "username TEXT NOT NULL," +
                "total_level INTEGER DEFAULT 1," +
                "current_xp REAL DEFAULT 0," +
                "xp_to_next_level REAL DEFAULT 100," +
                "available_rune_points INTEGER DEFAULT 0," +
                "active_class TEXT," +
                "last_rune_reset INTEGER DEFAULT 0" +
                ")");

        // Player runes table
        stmt.execute("CREATE TABLE IF NOT EXISTS player_runes (" +
                "uuid TEXT NOT NULL," +
                "rune_name TEXT NOT NULL," +
                "rune_level INTEGER DEFAULT 0," +
                "PRIMARY KEY (uuid, rune_name)," +
                "FOREIGN KEY (uuid) REFERENCES players(uuid)" +
                ")");

        // Parties table
        stmt.execute("CREATE TABLE IF NOT EXISTS parties (" +
                "party_id TEXT PRIMARY KEY," +
                "leader_uuid TEXT NOT NULL," +
                "created_at INTEGER NOT NULL" +
                ")");

        // Party members table
        stmt.execute("CREATE TABLE IF NOT EXISTS party_members (" +
                "party_id TEXT NOT NULL," +
                "player_uuid TEXT NOT NULL," +
                "joined_at INTEGER NOT NULL," +
                "PRIMARY KEY (party_id, player_uuid)," +
                "FOREIGN KEY (party_id) REFERENCES parties(party_id)" +
                ")");

        stmt.close();
    }

    /**
     * Load player data from database (async)
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement stmt = connection.prepareStatement(
                        "SELECT * FROM players WHERE uuid = ?");
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                PlayerData data;
                if (rs.next()) {
                    data = new PlayerData(uuid, rs.getString("username"));
                    data.setTotalLevel(rs.getInt("total_level"));
                    data.setCurrentXP(rs.getDouble("current_xp"));
                    data.setXpToNextLevel(rs.getDouble("xp_to_next_level"));
                    data.setAvailableRunePoints(rs.getInt("available_rune_points"));
                    
                    String className = rs.getString("active_class");
                    if (className != null && !className.isEmpty()) {
                        try {
                            data.setActiveClass(RPGClass.valueOf(className));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    
                    data.setLastRuneReset(rs.getLong("last_rune_reset"));
                } else {
                    // Create new player data
                    data = new PlayerData(uuid, username);
                }
                
                rs.close();
                stmt.close();

                // Load rune levels
                loadRuneLevels(data);

                return data;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player data for " + uuid + ": " + e.getMessage());
                return new PlayerData(uuid, username);
            }
        });
    }

    /**
     * Load rune levels for a player
     */
    private void loadRuneLevels(PlayerData data) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT rune_name, rune_level FROM player_runes WHERE uuid = ?");
        stmt.setString(1, data.getUuid().toString());
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            try {
                RuneType rune = RuneType.valueOf(rs.getString("rune_name"));
                int level = rs.getInt("rune_level");
                data.setRuneLevel(rune, level);
            } catch (IllegalArgumentException ignored) {}
        }

        rs.close();
        stmt.close();
    }

    /**
     * Save player data to database (async)
     */
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Save or update player
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT OR REPLACE INTO players " +
                        "(uuid, username, total_level, current_xp, xp_to_next_level, " +
                        "available_rune_points, active_class, last_rune_reset) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                
                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, data.getUsername());
                stmt.setInt(3, data.getTotalLevel());
                stmt.setDouble(4, data.getCurrentXP());
                stmt.setDouble(5, data.getXpToNextLevel());
                stmt.setInt(6, data.getAvailableRunePoints());
                stmt.setString(7, data.getActiveClass() != null ? data.getActiveClass().name() : null);
                stmt.setLong(8, data.getLastRuneReset());
                stmt.executeUpdate();
                stmt.close();

                // Save rune levels
                saveRuneLevels(data);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player data for " + data.getUuid() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Save rune levels for a player
     */
    private void saveRuneLevels(PlayerData data) throws SQLException {
        // Delete existing runes
        PreparedStatement deleteStmt = connection.prepareStatement(
                "DELETE FROM player_runes WHERE uuid = ?");
        deleteStmt.setString(1, data.getUuid().toString());
        deleteStmt.executeUpdate();
        deleteStmt.close();

        // Insert current runes
        PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO player_runes (uuid, rune_name, rune_level) VALUES (?, ?, ?)");
        
        for (Map.Entry<RuneType, Integer> entry : data.getRuneLevels().entrySet()) {
            insertStmt.setString(1, data.getUuid().toString());
            insertStmt.setString(2, entry.getKey().name());
            insertStmt.setInt(3, entry.getValue());
            insertStmt.addBatch();
        }
        
        insertStmt.executeBatch();
        insertStmt.close();
    }

    /**
     * Save party to database
     */
    public CompletableFuture<Void> saveParty(Party party) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Save or update party
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT OR REPLACE INTO parties (party_id, leader_uuid, created_at) " +
                        "VALUES (?, ?, ?)");
                stmt.setString(1, party.getPartyId().toString());
                stmt.setString(2, party.getLeaderId().toString());
                stmt.setLong(3, party.getCreatedAt());
                stmt.executeUpdate();
                stmt.close();

                // Delete existing members
                PreparedStatement deleteStmt = connection.prepareStatement(
                        "DELETE FROM party_members WHERE party_id = ?");
                deleteStmt.setString(1, party.getPartyId().toString());
                deleteStmt.executeUpdate();
                deleteStmt.close();

                // Insert current members
                PreparedStatement insertStmt = connection.prepareStatement(
                        "INSERT INTO party_members (party_id, player_uuid, joined_at) VALUES (?, ?, ?)");
                
                for (UUID memberId : party.getMembers()) {
                    insertStmt.setString(1, party.getPartyId().toString());
                    insertStmt.setString(2, memberId.toString());
                    insertStmt.setLong(3, System.currentTimeMillis());
                    insertStmt.addBatch();
                }
                
                insertStmt.executeBatch();
                insertStmt.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save party: " + e.getMessage());
            }
        });
    }

    /**
     * Load all parties from database
     */
    public Map<UUID, Party> loadParties() {
        Map<UUID, Party> parties = new HashMap<>();
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM parties");

            while (rs.next()) {
                UUID partyId = UUID.fromString(rs.getString("party_id"));
                UUID leaderId = UUID.fromString(rs.getString("leader_uuid"));
                Party party = new Party(partyId, leaderId);
                
                // Load members
                PreparedStatement memberStmt = connection.prepareStatement(
                        "SELECT player_uuid FROM party_members WHERE party_id = ?");
                memberStmt.setString(1, partyId.toString());
                ResultSet memberRs = memberStmt.executeQuery();
                
                while (memberRs.next()) {
                    UUID memberId = UUID.fromString(memberRs.getString("player_uuid"));
                    party.addMember(memberId);
                }
                
                memberRs.close();
                memberStmt.close();
                
                parties.put(partyId, party);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load parties: " + e.getMessage());
        }

        return parties;
    }

    /**
     * Delete party from database
     */
    public CompletableFuture<Void> deleteParty(UUID partyId) {
        return CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM parties WHERE party_id = ?");
                stmt.setString(1, partyId.toString());
                stmt.executeUpdate();
                stmt.close();

                PreparedStatement memberStmt = connection.prepareStatement(
                        "DELETE FROM party_members WHERE party_id = ?");
                memberStmt.setString(1, partyId.toString());
                memberStmt.executeUpdate();
                memberStmt.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete party: " + e.getMessage());
            }
        });
    }

    /**
     * Get top players by level for leaderboard
     */
    public List<Map.Entry<String, Integer>> getTopPlayersByLevel(int limit) {
        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
        
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT username, total_level FROM players ORDER BY total_level DESC LIMIT ?");
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                int level = rs.getInt("total_level");
                leaderboard.add(new AbstractMap.SimpleEntry<>(username, level));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get leaderboard: " + e.getMessage());
        }

        return leaderboard;
    }

    /**
     * Get top players by specific rune level
     */
    public List<Map.Entry<String, Integer>> getTopPlayersByRune(RuneType rune, int limit) {
        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
        
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT p.username, r.rune_level FROM players p " +
                    "JOIN player_runes r ON p.uuid = r.uuid " +
                    "WHERE r.rune_name = ? " +
                    "ORDER BY r.rune_level DESC LIMIT ?");
            stmt.setString(1, rune.name());
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                int level = rs.getInt("rune_level");
                leaderboard.add(new AbstractMap.SimpleEntry<>(username, level));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get rune leaderboard: " + e.getMessage());
        }

        return leaderboard;
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
        }
    }
}
