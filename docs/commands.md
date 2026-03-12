---
layout: default
title: Commands Reference
description: Complete list of player and admin commands for PolarPvP-Manager
---

# Commands Reference

## Player Commands

### `/pvp`

Shows your current PvP status when used without arguments.

**Permission:** `pvptoggle.use`

```bash
/pvp
```

### `/pvp on`

Enable your manual PvP toggle.

**Permission:** `pvptoggle.use`

```bash
/pvp on
```

### `/pvp off`

Disable your manual PvP toggle, unless PvP is currently forced by a zone or playtime debt.

**Permission:** `pvptoggle.use`

```bash
/pvp off
```

### `/pvp status`

Show whether your effective PvP state is enabled, whether you are forced, and your total playtime.

**Permission:** `pvptoggle.use`

```bash
/pvp status
```

## Admin Commands

All admin commands require `pvptoggle.admin`.

### `/pvpadmin wand`

Give yourself the zone selection wand.

```bash
/pvpadmin wand
```

### `/pvpadmin zone create <name>`

Create a forced PvP zone from your current wand selection.

```bash
/pvpadmin zone create arena
```

### `/pvpadmin zone delete <name>`

Delete an existing zone.

```bash
/pvpadmin zone delete arena
```

### `/pvpadmin zone list`

List all configured zones.

```bash
/pvpadmin zone list
```

### `/pvpadmin zone info <name>`

Display a zone's world and corner coordinates.

```bash
/pvpadmin zone info arena
```

### `/pvpadmin player <name> info`

Show a player's PvP toggle, total playtime, processed cycles, and debt.

```bash
/pvpadmin player Steve info
```

### `/pvpadmin player <name> reset`

Reset stored data for a player.

```bash
/pvpadmin player Steve reset
```

### `/pvpadmin player <name> setdebt <seconds>`

Set a player's forced PvP debt directly.

```bash
/pvpadmin player Steve setdebt 1200
```

### `/pvpadmin simtime <seconds>`

Add simulated playtime to the executing player for testing.

```bash
/pvpadmin simtime 3600
```

### `/pvpadmin reload`

Reload `config.yml` from disk.

```bash
/pvpadmin reload
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `pvptoggle.use` | Use `/pvp` commands | true |
| `pvptoggle.admin` | Use `/pvpadmin` commands | op |
| `pvptoggle.bypass` | Ignore playtime-based forced PvP | op |