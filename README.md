

# PolarPvP-Manager

Simple Minecraft plugin for toggling PvP. Players can enable or disable PvP for themselves. Admins can force PvP in certain zones or after enough playtime. Compatible with Bukkit, Spigot, Paper, and Purpur.


## Setup
- Place the jar in your `plugins/` folder
- Start your server
- Edit `config.yml` to customize settings
- Use `/plpvpadmin reload` to reload config


## Commands
- `/plpvp on`, `/plpvp off`, `/plpvp status` — Toggle or check your PvP
- `/plpvpadmin wand` — Get the zone wand
- `/plpvpadmin zone create <name>` — Create a zone
- `/plpvpadmin zone delete <name>` — Delete a zone
- `/plpvpadmin zone list` — List all zones
- `/plpvpadmin zone info <name>` — Show zone details
- `/plpvpadmin player <name> info|reset|setdebt <sec>` — Manage player data
- `/plpvpadmin simtime <seconds>` — Add fake playtime (for testing)
- `/plpvpadmin reload` — Reload config


## Permissions
- `pvptoggle.use` — All players
- `pvptoggle.admin` — Admins/OP
- `pvptoggle.bypass` — Bypass playtime debt (OP by default)


## Zones
Select two blocks with `/plpvpadmin wand` (left and right click), then use `/plpvpadmin zone create <name>`. Both blocks must be in the same world.



## Playtime Debt
You build up PvP debt as you play. There are two modes:
- `per_hour`: Every X hours played gives Y minutes of PvP debt
- `per_minute`: Every X minutes played gives Y minutes of PvP debt
Debt only counts down if at least two players are online. Logging out does not reset debt.

### PvP Debt Cap
Players can only accumulate up to a set amount of PvP debt (`pvp-debt-cap` in config). If you reach this cap, PvP is forced ON until you work off your debt. You get a chat notification when this happens.


## Config
All settings are in `config.yml`:
- `default-pvp-state`: PvP enabled/disabled for new players
- `playtime.mode`: "per_hour" or "per_minute"
- `playtime.hours-per-cycle`: Hours per cycle (per_hour mode)
- `playtime.minutes-per-cycle`: Minutes per cycle (per_minute mode)
- `playtime.forced-minutes`: Forced PvP minutes per cycle
- `pvp-debt-cap`: Max PvP debt (minutes) before PvP is forced
- `zone-wand-material`: Wand item (default: BLAZE_ROD)
- `save-interval`: Auto-save interval (minutes)
- `debug`: Print debug info
Messages are customizable. Use `&` for color codes.


## Data
- `playerdata.yml`: PvP state, playtime, debt
- `zones.yml`: Zone definitions
Data is auto-saved every 5 minutes, on player quit, and server shutdown.


## Troubleshooting
- PvP not working? Check permissions and config
- Make sure you're running Java 17 or higher
- Use `/plpvpadmin reload` after editing config
- Set `debug: true` in config for more logs


## Build
Run `mvn clean package`.
The jar will be in `target/PolarPvP-Manager-1.0.0.jar`.

## License
See LICENSE.
