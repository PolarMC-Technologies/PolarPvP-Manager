# PolarPvP-Manager

PvP toggle plugin for Minecraft servers (Bukkit/Spigot/Paper/Purpur). Adds player-controlled PvP toggling, admin-defined forced PvP zones, and a playtime-based forced PvP system.

Requires Java 17+ and Minecraft 1.20+.

## Features

**PvP Toggle** - Players use `/pvp on` and `/pvp off`. Both the attacker and victim must have PvP enabled for damage to register. Projectiles (arrows, tridents, potions) and tamed animals are traced back to the owning player.

**Forced PvP Zones** - Admins define cuboid regions with a wand tool (Blaze Rod). Anyone inside a zone has PvP forced on regardless of their toggle. Players get notified on entry/exit.

**Playtime Debt** - Every hour of cumulative playtime adds 20 minutes of forced PvP (configurable). Playtime always ticks, but the debt countdown only runs with 2+ players online so you can't burn it off solo. Debt stacks and persists through logout.

## Commands

`/pvp on|off|status` - Toggle or check your PvP (permission: `pvptoggle.use`, default: everyone)

`/pvpadmin wand` - Get the zone selection wand
`/pvpadmin zone create|delete|list|info <name>` - Manage PvP zones
`/pvpadmin player <name> info|reset|setdebt <seconds>` - Manage player data
`/pvpadmin reload` - Reload config

Admin commands need `pvptoggle.admin` (default: op). `pvptoggle.bypass` exempts from playtime debt (default: op).

## Setup

1. Drop the jar into `plugins/`
2. Restart the server
3. Edit `plugins/PolarPvP-Manager/config.yml`
4. `/pvpadmin reload`

## Creating Zones

1. `/pvpadmin wand` for the selector (Blaze Rod)
2. Left click a block = pos1, right click = pos2
3. `/pvpadmin zone create <name>`

Both positions need to be in the same world.

## Config

See `config.yml` for all options. The important ones:

- `default-pvp-state` - New players start with PvP on/off (default: false)
- `playtime.hours-per-cycle` - Hours per forced PvP cycle (default: 1)
- `playtime.forced-minutes` - Minutes of forced PvP per cycle (default: 20)
- `zone-wand-material` - Wand material (default: BLAZE_ROD)
- `save-interval` - Auto-save interval in minutes (default: 5)

All messages support `&` color codes.

## Data Files

- `playerdata.yml` - PvP toggle state, playtime, processed cycles, debt per player
- `zones.yml` - Zone definitions (name, world, corners)

Auto-saved every 5 minutes, on every player quit, and on shutdown.

## Building

```
mvn clean package
```

Output: `target/PolarPvP-Manager-1.0.0.jar`

## License

[LICENSE](LICENSE)
