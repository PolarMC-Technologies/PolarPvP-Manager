# ⚔️ PolarPvP-Manager

A lightweight **PvP toggle plugin** for Minecraft servers with **forced PvP zones** and a **playtime-based forced PvP** system. Built for maximum compatibility.

### Supported Servers
| Platform | Status |
|----------|--------|
| Bukkit   | ✅ |
| Spigot   | ✅ |
| Paper    | ✅ |
| Purpur   | ✅ |

> Requires **Java 17+** and **Minecraft 1.20+**

---

## Features

### 🔀 PvP Toggle
Players can freely turn PvP on and off. **Both** the attacker and the victim must have PvP enabled for damage to go through — otherwise the hit is silently blocked with a message.

### 🗺️ Forced PvP Zones
Admins can define cuboid regions where PvP is **always forced on** for everyone, regardless of their toggle. Players get a notification when they enter/leave a zone.

### ⏱️ Playtime → Forced PvP
- For every **1 hour** of playtime, players receive **20 minutes of forced PvP** (configurable).
- **Playtime always counts**, even when you're the only player online.
- **Debt countdown only ticks with 2+ players online** — you can't burn it off solo.
- **Debt stacks**: 2 hours played = 40 min debt, 3 hours = 60 min, etc.
- **Persists through logout** — players can't leave to dodge their debt.
- Players see a live **action bar timer** while debt is active (shows "paused — solo" when alone).

### 🛡️ Combat Tracing
Damage from **projectiles** (arrows, tridents, splash potions) and **tamed animals** (wolves) is traced back to the owning player for PvP checks.

---

## Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/pvp on` | Enable your PvP | `pvptoggle.use` |
| `/pvp off` | Disable your PvP (blocked while forced) | `pvptoggle.use` |
| `/pvp status` | Show your PvP status, playtime & debt | `pvptoggle.use` |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/pvpadmin wand` | Get the zone selection wand | `pvptoggle.admin` |
| `/pvpadmin zone create <name>` | Create a forced PvP zone from selection | `pvptoggle.admin` |
| `/pvpadmin zone delete <name>` | Delete a zone | `pvptoggle.admin` |
| `/pvpadmin zone list` | List all zones | `pvptoggle.admin` |
| `/pvpadmin zone info <name>` | Show zone details | `pvptoggle.admin` |
| `/pvpadmin player <name> info` | Inspect player data | `pvptoggle.admin` |
| `/pvpadmin player <name> reset` | Reset all data for a player | `pvptoggle.admin` |
| `/pvpadmin player <name> setdebt <seconds>` | Manually set PvP debt | `pvptoggle.admin` |
| `/pvpadmin reload` | Reload config.yml | `pvptoggle.admin` |

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `pvptoggle.use` | Use `/pvp` command | Everyone |
| `pvptoggle.admin` | Use `/pvpadmin` command | OP |
| `pvptoggle.bypass` | Exempt from playtime forced PvP | OP |

---

## Creating a Forced PvP Zone

1. Run `/pvpadmin wand` to get the **PvP Zone Selector** (Blaze Rod).
2. **Left click** a block to set position 1.
3. **Right click** a block to set position 2.
4. Run `/pvpadmin zone create <name>` to create the zone.

Both positions must be in the same world. The zone is a cuboid between the two corners.

---

## Configuration

All values are in `config.yml` — see inline comments:

```yaml
# Default PvP state for new players
default-pvp-state: false

# All messages support & color codes
messages:
  pvp-enabled: "&a&l⚔ PvP has been enabled!"
  pvp-disabled: "&c⚔ PvP has been disabled."
  pvp-blocked-attacker: "&cYou have PvP disabled! Use &e/pvp on &cto enable it."
  pvp-blocked-victim: "&cThat player has PvP disabled!"
  pvp-forced-zone: "&c&lYou are in a forced PvP zone!"
  pvp-forced-playtime: "&c&lPvP is forced due to playtime! &f%time% &cremaining."

# Playtime cycle settings
playtime:
  hours-per-cycle: 1      # Hours of playtime before forced PvP triggers
  forced-minutes: 20      # Minutes of forced PvP per cycle

# Zone selection wand material
zone-wand-material: BLAZE_ROD

# Auto-save interval in minutes
save-interval: 5
```

---

## Data Storage

| File | Contents |
|------|----------|
| `playerdata.yml` | Per-player PvP toggle, total playtime, processed cycles, PvP debt |
| `zones.yml` | All defined forced PvP zones with corners and world |

Both files are auto-saved periodically (default: every 5 minutes) and on every player quit.

---

## Building from Source

```bash
# Requires Java 17+ and Maven
mvn clean package
```

The compiled JAR will be at `target/PolarPvP-Manager-1.0.0.jar`.

---

## Installation

1. Build the plugin or download the JAR from releases.
2. Drop `PolarPvP-Manager-1.0.0.jar` into your server's `plugins/` folder.
3. Restart (or reload) the server.
4. Edit `plugins/PolarPvP-Manager/config.yml` to your liking.
5. Run `/pvpadmin reload` to apply changes.

---

## License

See [LICENSE](LICENSE) for details.
