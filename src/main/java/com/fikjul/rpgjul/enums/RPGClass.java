package com.fikjul.rpgjul.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents all available classes in the RPG system.
 * Each class has specific rune requirements and unique bonuses.
 */
public enum RPGClass {
    BLADEMASTER(
        "Blademaster",
        "⚔️",
        "+15% Critical Hit Chance",
        createRequirements(RuneType.BLADESTORM, 50, RuneType.SWIFTWIND, 30)
    ),
    BERSERKER(
        "Berserker",
        "🪓",
        "+20% Lifesteal on Axe Hits",
        createRequirements(RuneType.WARBRINGER, 50, RuneType.VITALITY, 30)
    ),
    SHARPSHOOTER(
        "Sharpshooter",
        "🏹",
        "15% chance to shoot double arrows",
        createRequirements(RuneType.MARKSMAN, 70, RuneType.SWIFTWIND, 20)
    ),
    PALADIN(
        "Paladin",
        "✝",
        "Regeneration when blocking with shield",
        createRequirements(RuneType.GUARDIAN, 40, RuneType.RESTORATION, 40)
    ),
    SHADOW_ASSASSIN(
        "Shadow Assassin",
        "🗡",
        "+50% Backstab Damage",
        createRequirements(RuneType.BLADESTORM, 30, RuneType.SWIFTWIND, 50)
    ),
    FORTRESS(
        "Fortress",
        "🏰",
        "10% Damage Reflection",
        createRequirements(RuneType.AEGIS, 60, RuneType.VITALITY, 40)
    ),
    ARTISAN(
        "Artisan",
        "⚒",
        "+25% Resource Gathering Speed",
        createRequirements(RuneType.ARCHITECT, 80, RuneType.ANGLER, 20)
    ),
    RANGER(
        "Ranger",
        "🌲",
        "+20% Damage to all ranged weapons",
        createRequirements(RuneType.MARKSMAN, 40, RuneType.ANGLER, 30, RuneType.SWIFTWIND, 30)
    );

    private final String displayName;
    private final String icon;
    private final String bonus;
    private final Map<RuneType, Integer> requirements;

    RPGClass(String displayName, String icon, String bonus, Map<RuneType, Integer> requirements) {
        this.displayName = displayName;
        this.icon = icon;
        this.bonus = bonus;
        this.requirements = requirements;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getBonus() {
        return bonus;
    }

    public Map<RuneType, Integer> getRequirements() {
        return requirements;
    }

    /**
     * Helper method to create requirements map
     */
    private static Map<RuneType, Integer> createRequirements(Object... pairs) {
        Map<RuneType, Integer> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((RuneType) pairs[i], (Integer) pairs[i + 1]);
        }
        return map;
    }

    /**
     * Check if player meets requirements for this class
     */
    public boolean meetsRequirements(Map<RuneType, Integer> playerRunes) {
        for (Map.Entry<RuneType, Integer> entry : requirements.entrySet()) {
            int playerLevel = playerRunes.getOrDefault(entry.getKey(), 0);
            if (playerLevel < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
}
