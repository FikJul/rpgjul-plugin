package com.fikjul.rpgjul.enums;

/**
 * Represents all available runes in the RPG system.
 * Each rune provides different stat bonuses when leveled up.
 */
public enum RuneType {
    VITALITY("Rune of the Titan", "❤", "+2 HP per level", 2.0),
    BLADESTORM("Bladestorm Rune", "⚔", "+1% sword damage per level", 1.0),
    WARBRINGER("Warbringer Rune", "🪓", "+1% axe damage per level", 1.0),
    RESTORATION("Restoration Rune", "🔄", "+0.1 HP regen/5s per level", 0.1),
    SWIFTWIND("Swiftwind Rune", "🏃", "+0.2% movement speed per level", 0.2),
    AEGIS("Rune of Iron", "🛡", "+0.5% damage reduction per level", 0.5),
    MARKSMAN("Marksman Rune", "🏹", "+1% bow damage per level", 1.0),
    GUARDIAN("Guardian Rune", "🛡️", "+0.5% shield block effectiveness per level", 0.5),
    ARCHITECT("Architect Rune", "⛏", "+0.5% mining speed per level", 0.5),
    ANGLER("Angler's Rune", "🎣", "+1% fishing luck per level", 1.0);

    private final String displayName;
    private final String icon;
    private final String description;
    private final double bonusPerLevel;

    RuneType(String displayName, String icon, String description, double bonusPerLevel) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.bonusPerLevel = bonusPerLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public double getBonusPerLevel() {
        return bonusPerLevel;
    }

    /**
     * Calculate the total bonus for a given level
     */
    public double calculateBonus(int level) {
        return bonusPerLevel * level;
    }
}
