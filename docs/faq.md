---
layout: default
title: FAQ
description: Answers to common PolarPvP-Manager questions
---

# FAQ

## Does this plugin work on Paper?

Yes. The plugin targets Bukkit-compatible servers including Spigot, Paper, and Purpur.

## What version of Minecraft is supported?

The plugin is configured for Minecraft 1.20+.

## Can players disable PvP anywhere?

No. Forced PvP zones and active playtime debt override the player's manual toggle.

## How do I create a PvP zone?

Use:

```bash
/pvpadmin wand
/pvpadmin zone create <name>
```

You must select two corners in the same world first.

## How do I test the playtime system quickly?

Use the simulation command:

```bash
/pvpadmin simtime 3600
```

## Where is player data stored?

In the plugin directory:

- `playerdata.yml`
- `zones.yml`

## What permission bypasses playtime debt?

`pvptoggle.bypass`

## Do I need to restart the server after config edits?

Usually no. Use:

```bash
/pvpadmin reload
```

If you suspect a bad startup state or malformed config, restart the server as well.