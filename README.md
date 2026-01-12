# RPGJul Plugin

A comprehensive RPG plugin for Minecraft servers with levels, runes, classes, mob leveling, and party systems.

## Features

### Core Systems
- **Level & XP System**: Custom leveling separate from vanilla Minecraft XP
- **Rune System**: 10 unique runes with 100 levels each providing stat bonuses
- **Class System**: 8 unlockable classes with special abilities
- **Mob Leveling**: Dynamic mob scaling with level-based stats and XP
- **Party System**: Cooperative gameplay with XP sharing and buffs

### Runes (Max Level 100)
1. **Vitality Rune** - +2 HP per level
2. **Bladestorm Rune** - +1% sword damage per level
3. **Warbringer Rune** - +1% axe damage per level
4. **Restoration Rune** - +0.1 HP regen/5s per level
5. **Swiftwind Rune** - +0.2% movement speed per level
6. **Aegis Rune** - +0.5% damage reduction per level
7. **Marksman Rune** - +1% bow damage per level
8. **Guardian Rune** - +0.5% shield block effectiveness per level
9. **Architect Rune** - +0.5% mining speed per level
10. **Angler's Rune** - +1% fishing luck per level

### Classes
1. **Blademaster** - Bladestorm Lv.50 + Swiftwind Lv.30 → +15% Critical Hit Chance
2. **Berserker** - Warbringer Lv.50 + Vitality Lv.30 → +20% Lifesteal on Axe Hits
3. **Sharpshooter** - Marksman Lv.70 + Swiftwind Lv.20 → 15% double arrow chance
4. **Paladin** - Guardian Lv.40 + Restoration Lv.40 → Shield block regeneration
5. **Shadow Assassin** - Bladestorm Lv.30 + Swiftwind Lv.50 → +50% Backstab Damage
6. **Fortress** - Aegis Lv.60 + Vitality Lv.40 → 10% Damage Reflection
7. **Artisan** - Architect Lv.80 + Angler Lv.20 → +25% Gathering Speed
8. **Ranger** - Marksman Lv.40 + Angler Lv.30 + Swiftwind Lv.30 → +20% Ranged Damage

## Commands

### Player Commands
- `/rpg` - Open main menu GUI
- `/rpg stats` - View your stats
- `/rpg runes` - Open rune management GUI
- `/rpg class` - Open class showcase GUI
- `/rpg leaderboard` - Open leaderboard GUI
- `/party create` - Create a party
- `/party invite <player>` - Invite player to party
- `/party accept` - Accept party invitation
- `/party leave` - Leave your party
- `/party kick <player>` - Kick member (leader only)
- `/party disband` - Disband party (leader only)
- `/party info` - View party information
- `/p <message>` - Send message to party chat

### Admin Commands
- `/rpgadmin setlevel <player> <level>` - Set player level
- `/rpgadmin giverune <player> <rune> <amount>` - Give rune levels
- `/rpgadmin setrunepoints <player> <amount>` - Set rune points
- `/rpgadmin reset <player>` - Reset all player data
- `/rpgadmin resetrunes <player>` - Reset player's runes
- `/rpgadmin reload` - Reload configuration

## Permissions
- `rpgjul.use` - Access RPG system (default: true)
- `rpgjul.party` - Access party system (default: true)
- `rpgjul.party.chat` - Use party chat (default: true)
- `rpgjul.admin` - Admin commands (default: op)

## Building

**Requirements:**
- Java 17 or higher
- Maven 3.6+
- Access to Paper/Spigot repository

**Build Command:**
```bash
mvn clean package
```

The compiled JAR will be in the `target/` directory.

**Note:** This plugin requires a Minecraft server running Paper or Spigot 1.19+ to function properly.

## Installation

1. Build the plugin or download the JAR
2. Place the JAR in your server's `plugins/` folder
3. Start or restart your server
4. Configure the plugin in `plugins/RPGJulPlugin/config.yml`
5. Reload with `/rpgadmin reload` or restart

## Configuration

All settings can be adjusted in `config.yml`:
- XP rates per mob type
- Leveling formula and rates
- Rune settings and reset cooldown
- Mob leveling parameters
- Party system settings
- GUI preferences

## Database

The plugin uses SQLite to store:
- Player levels and XP
- Rune allocations
- Party data and members
- Auto-saves every 5 minutes (configurable)

## Implementation Status

✅ **Completed:**
- Project structure and Maven configuration
- All data models (PlayerData, Party, PartyInvitation)
- All enum definitions (RuneType, RPGClass)
- Database manager with SQLite
- Player data manager with caching
- Level manager with XP calculations
- Rune manager with allocation and reset
- Class manager with requirement checking
- Mob manager with level scaling
- Party manager with full party system
- All event listeners (join, quit, death, spawn, damage, regen)
- All commands (RPG, Party, Party Chat, Admin)
- Configuration files (plugin.yml, config.yml)

🚧 **TODO (GUI System):**
- Main Menu GUI
- Stats Display GUI
- Rune Management GUI
- Class Showcase GUI
- Leaderboard GUI
- Party GUI

Note: GUI implementation requires interactive inventory menus which are planned for future updates.

## Technical Details

- **API Version**: 1.19+
- **Language**: Java 17
- **Database**: SQLite (bundled)
- **Thread Safety**: Async database operations
- **Performance**: Cached player data, batch operations

## Support

For issues, feature requests, or contributions, please visit the GitHub repository.

## License

This project is licensed under the MIT License.