

# PolarPvP-Manager

Simple Minecraft plugin for toggling PvP. Players can enable or disable PvP for themselves. Admins can force PvP in certain zones or after enough playtime. Compatible with Bukkit, Spigot, Paper, and Purpur.



## Setup

1. Place the jar in your `plugins/` folder
2. Start your server
3. Edit `config.yml` to customize settings
4. Use `/plpvpadmin reload` to reload config



## Commands

| Command | Description |
|--------|-------------|
| `/plpvp on` | Enable PvP |
| `/plpvp off` | Disable PvP |
| `/plpvp status` | Show your PvP status |
| `/plpvpadmin wand` | Get the zone wand |
| `/plpvpadmin zone create <name>` | Create a zone |
| `/plpvpadmin zone delete <name>` | Delete a zone |
| `/plpvpadmin zone list` | List all zones |
| `/plpvpadmin zone info <name>` | Show zone details |
| `/plpvpadmin player <name> info` | Show player info |
| `/plpvpadmin player <name> reset` | Reset player data |
| `/plpvpadmin player <name> setdebt <sec>` | Set PvP debt |
| `/plpvpadmin simtime <seconds>` | Add fake playtime (testing) |
| `/plpvpadmin reload` | Reload config |



## Permissions

| Permission | Who |
|------------|-----|
| `pvptoggle.use` | All players |
| `pvptoggle.admin` | Admins/OP |
| `pvptoggle.bypass` | Bypass playtime debt (OP by default) |



## Zones

Select two blocks with `/plpvpadmin wand` (left and right click), then use `/plpvpadmin zone create <name>`. Both blocks must be in the same world.




## Playtime Debt

You build up PvP debt as you play. Modes:

* `per_hour`: Every X hours played gives Y minutes of PvP debt
* `per_minute`: Every X minutes played gives Y minutes of PvP debt

Debt only counts down if at least two players are online. Logging out does not reset debt.

**PvP Debt Cap:**

Players can only accumulate up to a set amount of PvP debt (`pvp-debt-cap` in config). If you reach this cap, PvP is forced ON until you work off your debt. You get a chat notification when this happens.



## Config

All settings are in `config.yml`:

```yaml
default-pvp-state: false
playtime:
	mode: per_hour
	hours-per-cycle: 1
	minutes-per-cycle: 5
	forced-minutes: 20
pvp-debt-cap: 60
zone-wand-material: BLAZE_ROD
save-interval: 5
debug: false
```

Messages are customizable. Use `&` for color codes.



## Data

| File | Purpose |
|------|---------|
| `playerdata.yml` | PvP state, playtime, debt |
| `zones.yml` | Zone definitions |

Data is auto-saved every 5 minutes, on player quit, and server shutdown.



## Troubleshooting

* PvP not working? Check permissions and config
* Make sure you're running Java 17 or higher
* Use `/plpvpadmin reload` after editing config
* Set `debug: true` in config for more logs



## Build

Run:

```sh
mvn clean package
```

The jar will be in:

```sh
target/PolarPvP-Manager-1.0.0.jar
```

## License

See LICENSE.
