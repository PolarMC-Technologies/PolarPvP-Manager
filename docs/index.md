---
layout: default
title: PolarPvP-Manager Documentation
description: PvP toggle plugin with forced zones and playtime-based PvP enforcement
---

PolarPvP-Manager gives players direct control over their PvP status while giving administrators tools to force PvP in specific regions and during configured playtime debt windows.

## Overview

- Player-controlled PvP toggling with `/pvp on`, `/pvp off`, and `/pvp status`
- Forced PvP zones for arenas, dungeons, events, or contested areas
- Playtime-based forced PvP cycles to prevent indefinite avoidance
- YAML-backed persistence for player data and zone definitions
- Configurable messages, wand material, save interval, and debug logging

## Requirements

- Minecraft 1.20+
- Java 17+
- Bukkit, Spigot, Paper, or Purpur

## Documentation

- [Quick Start](quick-start)
- [Installation](installation)
- [Configuration](configuration)
- [Commands](commands)
- [Zones](zones)
- [Playtime System](playtime-system)
- [Troubleshooting](troubleshooting)
- [FAQ](faq)

## Quick Start Summary

1. Drop the jar into `plugins/` and start the server.
2. Adjust `plugins/PolarPvP-Manager/config.yml`.
3. Give admins access to `pvptoggle.admin`.
4. Create a zone with `/pvpadmin wand` and `/pvpadmin zone create <name>`.
5. Test forced PvP with `/pvp status` and `/pvpadmin simtime <seconds>`.

## Key Features

### Player Control

Players can opt in or out of PvP when they are not currently forced into combat by a zone or active playtime debt.

### Zone Enforcement

Admins can define rectangular forced PvP regions with an in-game wand. Players inside those areas cannot toggle PvP off.

### Playtime Debt

The plugin can periodically force players into PvP after a configured amount of playtime. Debt persists across sessions and only counts down while multiple players are online.

## Support

- [Repository]({{ site.github.repository_url }})
- [Issue Tracker]({{ site.github.repository_url }}/issues)
- [Releases]({{ site.github.repository_url }}/releases)
