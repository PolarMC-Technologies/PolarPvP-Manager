---
layout: default
title: Installation Guide
description: Install PolarPvP-Manager on a Bukkit-compatible server
---

# Installation Guide

This guide covers plugin installation, first startup, and basic validation.

## Requirements

- Minecraft 1.20 or newer
- Java 17 or newer
- Bukkit, Spigot, Paper, or Purpur
- Maven 3.6+ if building from source

## Download or Build

### From Releases

Download the latest jar from [GitHub Releases]({{ site.github.repository_url }}/releases).

### From Source

```bash
git clone {{ site.github.repository_url }}.git
cd PolarPvP-Manager
mvn clean package
```

The jar will be produced in `target/`, typically as `PolarPvP-Manager-1.0.0.jar`.

## Install on Your Server

1. Stop the server.
2. Copy the jar into `plugins/`.
3. Start the server.
4. Wait for the plugin to generate its default config files.
5. Stop the server again if you want to make initial config changes before players join.

## Generated Files

PolarPvP-Manager stores its data in the plugin directory:

- `config.yml` for behavior and messages
- `playerdata.yml` for player PvP state, playtime, and debt
- `zones.yml` for forced PvP zone definitions

## First Validation

After startup, verify:

1. The plugin loads without errors in console.
2. `/pvp status` works for a player.
3. `/pvpadmin reload` works for an admin.
4. `/pvpadmin wand` gives the configured wand item.

## Common Installation Mistakes

- Running on Java older than 17
- Using an unsupported server jar
- Forgetting to grant `pvptoggle.admin` to staff
- Editing config values and not reloading with `/pvpadmin reload`