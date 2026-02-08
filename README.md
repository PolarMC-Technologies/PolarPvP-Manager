
PolarPvP-Manager
================

Simple PvP toggle for Minecraft (Bukkit/Spigot/Paper/Purpur). Lets players turn PvP on/off, and admins force PvP in zones or after enough playtime. No nonsense, just works.

Requirements:
- Java 17+
- Minecraft 1.20+

Quick Start
-----------
1. Put the jar in `plugins/`
2. Start the server
3. Edit `config.yml` if you want
4. `/pvpadmin reload` to apply changes

Commands
--------
- `/pvp on` / `/pvp off` / `/pvp status` — Toggle or check your PvP
- `/pvpadmin wand` — Get the zone wand (Blaze Rod)
- `/pvpadmin zone create <name>` — Make a forced PvP zone
- `/pvpadmin zone delete <name>` — Remove a zone
- `/pvpadmin zone list` — List zones
- `/pvpadmin zone info <name>` — Zone details
- `/pvpadmin player <name> info|reset|setdebt <sec>` — Player data
- `/pvpadmin simtime <seconds>` — Add fake playtime (testing)
- `/pvpadmin reload` — Reload config

Permissions:
- `pvptoggle.use` — Everyone
- `pvptoggle.admin` — OP/admin
- `pvptoggle.bypass` — Exempts from playtime debt (default: OP)

Zones
-----
Use `/pvpadmin wand` to select two corners (left/right click blocks), then `/pvpadmin zone create <name>`. Both corners must be in the same world.

Playtime Debt
-------------
Every hour of playtime (configurable) adds forced PvP time. Debt only counts down if 2+ players are online. You can't dodge it by logging out.

Config
------
Everything is in `config.yml`. Main options:
- `default-pvp-state`: PvP on/off for new players
- `playtime.hours-per-cycle`: Hours per forced PvP cycle
- `playtime.forced-minutes`: Minutes of forced PvP per cycle
- `zone-wand-material`: Wand item (default: BLAZE_ROD)
- `save-interval`: Auto-save interval (minutes)
- `debug`: Print debug info to console
Messages are customizable and use `&` color codes.

Data
----
- `playerdata.yml`: PvP state, playtime, debt
- `zones.yml`: Zone definitions
Auto-saved every 5 minutes, on player quit, and shutdown.

Troubleshooting
---------------
- If PvP isn't working, check your permissions and config.
- Make sure you're running Java 17+.
- Use `/pvpadmin reload` after editing config.
- Enable `debug: true` in config for extra logs.

Build
-----
Run:
	mvn clean package
Jar will be in `target/PolarPvP-Manager-1.0.0.jar`

License
-------
See LICENSE.
