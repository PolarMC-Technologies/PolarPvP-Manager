---
layout: default
title: Quick Start Guide
description: Get PolarPvP-Manager running with the minimum required setup
---

This is the shortest path to a working PolarPvP-Manager installation.

## Before You Start

- Minecraft 1.20+ server
- Java 17+
- Bukkit, Spigot, Paper, or Purpur

## Setup Steps

### Step 1: Install the Plugin

Place the built jar in your server's `plugins/` directory and start the server once.

### Step 2: Generate the Config

After first startup, the plugin creates:

- `plugins/PolarPvP-Manager/config.yml`
- `plugins/PolarPvP-Manager/playerdata.yml`
- `plugins/PolarPvP-Manager/zones.yml`

### Step 3: Review Core Settings

Start with these values in `config.yml`:

```yaml
default-pvp-state: false

playtime:
  hours-per-cycle: 1
  forced-minutes: 20

zone-wand-material: BLAZE_ROD
save-interval: 5
debug: false
```

### Step 4: Test Player Commands

Join the server and run:

```bash
/pvp status
/pvp on
/pvp off
```

### Step 5: Create a Forced PvP Zone

1. Run `/pvpadmin wand`
2. Left-click the first corner block
3. Right-click the opposite corner block
4. Run `/pvpadmin zone create arena`
5. Confirm the zone with `/pvpadmin zone list`

### Step 6: Test Playtime Debt

Use the admin simulation command to avoid waiting for a full cycle:

```bash
/pvpadmin simtime 3600
```

Then run `/pvp status` to confirm the player enters forced PvP when a cycle threshold is crossed.

## Next Steps

- Read [Configuration](configuration) to tune timings and messages.
- Read [Zones](zones) for zone creation and management details.
- Read [Playtime System](playtime-system) for debt behavior and testing notes.
