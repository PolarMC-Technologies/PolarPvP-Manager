

PolarPvP-Manager

Minecraft plugin for toggling PvP. Players can turn it on/off, admins can force it in zones or after enough playtime. Works on Bukkit/Spigot/Paper/Purpur. No fancy stuff.

Setup:
- Drop the jar in your `plugins/` folder
- Start the server
- Edit `config.yml` if you want
- `/pvpadmin reload` reloads config

Commands:
- `/pvp on` / `/pvp off` / `/pvp status` — Toggle/check PvP
- `/pvpadmin wand` — Get the zone wand
- `/pvpadmin zone create <name>` — Make a zone
- `/pvpadmin zone delete <name>` — Remove a zone
- `/pvpadmin zone list` — List zones
- `/pvpadmin zone info <name>` — Zone details
- `/pvpadmin player <name> info|reset|setdebt <sec>` — Player stuff
- `/pvpadmin simtime <seconds>` — Add fake playtime (testing)
- `/pvpadmin reload` — Reload config

Permissions:
- `pvptoggle.use` — Everyone
- `pvptoggle.admin` — OP/admin
- `pvptoggle.bypass` — Skips playtime debt (OP by default)

Zones:
Use `/pvpadmin wand` to select two blocks (left/right click), then `/pvpadmin zone create <name>`. Both blocks must be in the same world.

Playtime Debt:
Every hour of playtime (configurable) adds forced PvP time. Debt only counts down if 2+ players are online. Logging out doesn't help.

Config:
Everything is in `config.yml`. Main stuff:
- `default-pvp-state`: PvP on/off for new players
- `playtime.hours-per-cycle`: Hours per forced PvP cycle
- `playtime.forced-minutes`: Minutes of forced PvP per cycle
- `zone-wand-material`: Wand item (default: BLAZE_ROD)
- `save-interval`: Auto-save interval (minutes)
- `debug`: Print debug info
Messages are customizable, use `&` color codes.

Data:
- `playerdata.yml`: PvP state, playtime, debt
- `zones.yml`: Zone definitions
Auto-saved every 5 minutes, on player quit, and shutdown.

Troubleshooting:
- PvP not working? Check permissions/config.
- Make sure you're running Java 17+.
- `/pvpadmin reload` after editing config.
- Set `debug: true` in config for more logs.

Build:
Run `mvn clean package`.
Jar will be in `target/PolarPvP-Manager-1.0.0.jar`.

License: 
GPL3
