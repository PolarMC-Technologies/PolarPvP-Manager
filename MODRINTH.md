# PolarPvP-Manager

A powerful and flexible PvP management plugin for Minecraft servers.

## Overview

PolarPvP-Manager gives your players complete control over their PvP experience while providing server administrators with advanced tools to manage PvP enforcement. Perfect for survival servers, minigame networks, and anywhere you need fine-grained PvP control.

## Key Features

**Player Control**
- Simple commands to enable or disable PvP at will
- Clear status checking with customizable messages
- Protection from unwanted combat

**Administrative Tools**
- Create forced PvP zones with an easy-to-use wand tool
- Define rectangular regions where PvP is always active
- Manage player PvP data and reset states as needed

**Playtime-Based System**
- Automatic forced PvP periods based on player activity
- Configurable cycles prevent indefinite PvP avoidance
- Smart debt system only counts down when multiple players are online
- Bypass permissions for VIP or staff members

**Highly Configurable**
- Customize all messages with color code support
- Adjust playtime cycles and forced PvP duration
- Configure zone wand material and save intervals
- Debug mode for troubleshooting

## Quick Start

1. Install the plugin in your `plugins/` directory
2. Start your server
3. Players can use `/pvp on` or `/pvp off` to toggle their status
4. Admins can create zones with `/pvpadmin wand` and zone commands
5. Customize messages and settings in `config.yml`

## Commands

### For Players
- `/pvp on` - Enable PvP
- `/pvp off` - Disable PvP
- `/pvp status` - Check current status

### For Admins
- `/pvpadmin wand` - Get zone selection tool
- `/pvpadmin zone create <name>` - Create forced PvP zone
- `/pvpadmin zone delete <name>` - Remove zone
- `/pvpadmin zone list` - List all zones
- `/pvpadmin player <name> info` - View player data
- `/pvpadmin reload` - Reload configuration

## How Zones Work

Creating forced PvP zones is simple:

1. Get the wand with `/pvpadmin wand`
2. Left-click one corner of your zone
3. Right-click the opposite corner
4. Run `/pvpadmin zone create <name>`

Players inside these zones cannot disable PvP, making them perfect for arenas, dungeons, or specific PvP areas.

## Playtime Debt Explained

The playtime debt system ensures players can't avoid PvP indefinitely:

- Every hour of playtime (configurable) triggers a forced PvP period
- Default is 20 minutes of forced PvP per hour played
- Debt only decreases when 2+ players are online
- Logging out doesn't help - debt persists across sessions
- Admins can bypass this with the `pvptoggle.bypass` permission

This creates engaging dynamics where players must occasionally participate in PvP, promoting server activity and interaction.

## Permissions

- `pvptoggle.use` - Use /pvp commands (default: everyone)
- `pvptoggle.admin` - Use /pvpadmin commands (default: ops)
- `pvptoggle.bypass` - Skip playtime debt (default: ops)

## Configuration

All settings are in `config.yml`:

- Default PvP state for new players
- Playtime cycle duration and forced PvP length
- Custom messages with color codes
- Zone wand material
- Auto-save intervals
- Debug mode

## Compatibility

- Minecraft 1.20+
- Bukkit, Spigot, Paper, Purpur
- Java 17+

## Data Storage

Player data and zones are stored in YAML files with automatic saving:
- Every 5 minutes (configurable)
- On player logout
- On server shutdown

## Support

Need help or found a bug? Visit the GitHub repository to open an issue or check the documentation.

## License

GPL-3.0
