package com.fikjul.rpgjul.data;

import com.fikjul.rpgjul.enums.RPGClass;
import com.fikjul.rpgjul.enums.RuneType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's RPG data including levels, runes, and class
 */
public class PlayerData {
    private final UUID uuid;
    private String username;
    private int totalLevel;
    private double currentXP;
    private double xpToNextLevel;
    private int availableRunePoints;
    private RPGClass activeClass;
    private long lastRuneReset;
    private final Map<RuneType, Integer> runeLevels;

    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.totalLevel = 1;
        this.currentXP = 0;
        this.xpToNextLevel = 100;
        this.availableRunePoints = 0;
        this.activeClass = null;
        this.lastRuneReset = 0;
        this.runeLevels = new EnumMap<>(RuneType.class);
        
        // Initialize all runes at level 0
        for (RuneType rune : RuneType.values()) {
            runeLevels.put(rune, 0);
        }
    }

    // Getters and setters
    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalLevel() {
        return totalLevel;
    }

    public void setTotalLevel(int totalLevel) {
        this.totalLevel = totalLevel;
    }

    public double getCurrentXP() {
        return currentXP;
    }

    public void setCurrentXP(double currentXP) {
        this.currentXP = currentXP;
    }

    public double getXpToNextLevel() {
        return xpToNextLevel;
    }

    public void setXpToNextLevel(double xpToNextLevel) {
        this.xpToNextLevel = xpToNextLevel;
    }

    public int getAvailableRunePoints() {
        return availableRunePoints;
    }

    public void setAvailableRunePoints(int availableRunePoints) {
        this.availableRunePoints = availableRunePoints;
    }

    public RPGClass getActiveClass() {
        return activeClass;
    }

    public void setActiveClass(RPGClass activeClass) {
        this.activeClass = activeClass;
    }

    public long getLastRuneReset() {
        return lastRuneReset;
    }

    public void setLastRuneReset(long lastRuneReset) {
        this.lastRuneReset = lastRuneReset;
    }

    public Map<RuneType, Integer> getRuneLevels() {
        return runeLevels;
    }

    public int getRuneLevel(RuneType rune) {
        return runeLevels.getOrDefault(rune, 0);
    }

    public void setRuneLevel(RuneType rune, int level) {
        runeLevels.put(rune, level);
    }

    /**
     * Add XP to the player and handle level ups
     * @return true if player leveled up
     */
    public boolean addXP(double amount) {
        currentXP += amount;
        if (currentXP >= xpToNextLevel) {
            return true;
        }
        return false;
    }

    /**
     * Level up the player
     */
    public void levelUp(double xpBase, double xpMultiplier, String formula, int pointsPerLevel) {
        totalLevel++;
        currentXP -= xpToNextLevel;
        availableRunePoints += pointsPerLevel;
        
        // Calculate XP needed for next level
        if (formula.equalsIgnoreCase("exponential")) {
            xpToNextLevel = xpBase * Math.pow(xpMultiplier, totalLevel - 1);
        } else {
            xpToNextLevel = xpBase + (totalLevel - 1) * 50;
        }
    }

    /**
     * Allocate a point to a rune
     */
    public boolean allocateRune(RuneType rune, int maxLevel) {
        if (availableRunePoints <= 0) {
            return false;
        }
        
        int currentLevel = getRuneLevel(rune);
        if (currentLevel >= maxLevel) {
            return false;
        }
        
        setRuneLevel(rune, currentLevel + 1);
        availableRunePoints--;
        return true;
    }

    /**
     * Reset all runes and refund points
     */
    public void resetRunes() {
        int totalPoints = 0;
        for (int level : runeLevels.values()) {
            totalPoints += level;
        }
        
        for (RuneType rune : RuneType.values()) {
            runeLevels.put(rune, 0);
        }
        
        availableRunePoints += totalPoints;
        activeClass = null; // Reset class since requirements might not be met
        lastRuneReset = System.currentTimeMillis();
    }
}
