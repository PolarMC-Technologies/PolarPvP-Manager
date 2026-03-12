# PolarPvP-Manager

A comprehensive Minecraft PvP management plugin that provides players with control over their PvP status while giving administrators powerful tools to enforce PvP in specific zones and through playtime-based mechanics.

## Features

- Player-controlled PvP toggling
- Administrator-defined forced PvP zones
- Playtime-based forced PvP system (configurable debt mechanics)
- Compatible with Bukkit, Spigot, Paper, and Purpur
- Fully customizable messages with color code support
- Persistent data storage with automatic save intervals

## Requirements

- Minecraft Server 1.20+
- Java 17 or higher
- Bukkit/Spigot/Paper/Purpur

## Installation

1. Download the latest release JAR file
2. Place the JAR file in your server's `plugins/` directory
3. Start or restart your server
4. Configure the plugin by editing `plugins/PolarPvP-Manager/config.yml`
5. Use `/pvpadmin reload` to apply configuration changes without restarting

## Commands

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pvp on` | Enable your PvP status | `pvptoggle.use` |
| `/pvp off` | Disable your PvP status | `pvptoggle.use` |
| `/pvp status` | Check your current PvP status | `pvptoggle.use` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pvpadmin wand` | Get the zone selection wand | `pvptoggle.admin` |
| `/pvpadmin zone create <name>` | Create a forced PvP zone | `pvptoggle.admin` |
| `/pvpadmin zone delete <name>` | Remove a forced PvP zone | `pvptoggle.admin` |
| `/pvpadmin zone list` | List all zones | `pvptoggle.admin` |
| `/pvpadmin zone info <name>` | Display zone details | `pvptoggle.admin` |
| `/pvpadmin player <name> info` | View player PvP information | `pvptoggle.admin` |
| `/pvpadmin player <name> reset` | Reset player's PvP data | `pvptoggle.admin` |
| `/pvpadmin player <name> setdebt <seconds>` | Set player's forced PvP debt | `pvptoggle.admin` |
| `/pvpadmin simtime <seconds>` | Simulate playtime for testing | `pvptoggle.admin` |
| `/pvpadmin reload` | Reload configuration | `pvptoggle.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `pvptoggle.use` | Allows using /pvp commands | `true` (all players) |
| `pvptoggle.admin` | Allows using /pvpadmin commands | `op` |
| `pvptoggle.bypass` | Bypass forced PvP from playtime debt | `op` |

## Zone Management

Forced PvP zones are areas where players cannot disable PvP, regardless of their personal setting.

### Creating Zones

1. Use `/pvpadmin wand` to obtain the zone selection wand (default: Blaze Rod)
2. Left-click one corner block of your desired zone
3. Right-click the opposite corner block
4. Both blocks must be in the same world
5. Run `/pvpadmin zone create <name>` to create the zone

### Managing Zones

- List all zones: `/pvpadmin zone list`
- View zone details: `/pvpadmin zone info <name>`
- Delete a zone: `/pvpadmin zone delete <name>`

## Playtime Debt System

The playtime debt system automatically enforces periods of mandatory PvP based on player activity.

### How It Works

- Players accumulate playtime as they play on the server
- After a configured number of hours (default: 1 hour), players enter a forced PvP period
- During forced PvP, players cannot disable PvP
- Forced PvP lasts for a configured duration (default: 20 minutes)
- Debt only counts down when 2 or more players are online
- Logging out does not reduce debt

### Configuration

Configure the playtime system in `config.yml`:

```yaml
playtime:
  hours-per-cycle: 1        # Hours of playtime to trigger forced PvP
  forced-minutes: 20        # Minutes of forced PvP per cycle
```

## Configuration

The plugin configuration is located at `plugins/PolarPvP-Manager/config.yml`.

### Key Settings

| Setting | Description | Default |
|---------|-------------|---------|
| `default-pvp-state` | Default PvP state for new players | `false` |
| `playtime.hours-per-cycle` | Hours between forced PvP periods | `1` |
| `playtime.forced-minutes` | Duration of forced PvP in minutes | `20` |
| `zone-wand-material` | Material for zone selection wand | `BLAZE_ROD` |
| `save-interval` | Auto-save interval in minutes | `5` |
| `debug` | Enable debug logging | `false` |

### Message Customization

All messages can be customized in the config file. Use `&` color codes for formatting:

- `&a` - Green
- `&c` - Red
- `&7` - Gray
- `&l` - Bold
- And more (standard Minecraft color codes)

## Data Storage

The plugin stores data in YAML files:

- `playerdata.yml` - Player PvP states, playtime, and debt information
- `zones.yml` - Zone definitions and boundaries

Data is automatically saved:
- Every 5 minutes (configurable with `save-interval`)
- When a player quits the server
- When the server shuts down

## Building from Source

Requirements:
- Maven 3.6 or higher
- Java 17 or higher

Build the plugin:

```bash
mvn clean package
```

The compiled JAR will be located in the `target/` directory, typically named `PolarPvP-Manager-<version>.jar`.

## Troubleshooting

### PvP toggle not working

- Verify the player has the `pvptoggle.use` permission
- Check if the player is in a forced PvP zone
- Check if the player has active playtime debt
- Review configuration settings

### Debug Mode

Enable debug logging to troubleshoot issues:

1. Set `debug: true` in `config.yml`
2. Reload the plugin with `/pvpadmin reload`
3. Check console logs for detailed information

### Common Issues

- **Plugin not loading**: Ensure you are running Java 17 or higher
- **Commands not working**: Check permissions are properly configured
- **Zones not saving**: Verify file permissions on the plugin data directory

## Support

For bug reports, feature requests, or support, please open an issue on the GitHub repository.

## License

This plugin is licensed under GPL-3.0. See the LICENSE file for details.
