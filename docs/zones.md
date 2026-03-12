---
layout: default
title: Zone Management
description: Create and manage forced PvP regions with the zone selector wand
---

Forced PvP zones are rectangular regions where players cannot disable PvP, even if their manual toggle is off.

## Use Cases

- Arenas
- Dungeons
- Event areas
- High-value resource zones
- Spawn-adjacent combat rings

## How Zone Selection Works

Admins create zones with a configurable wand item.

Default wand material:

```yaml
zone-wand-material: BLAZE_ROD
```

The wand sets two corners:

- Left click: position 1
- Right click: position 2

Both corners must be in the same world.

## Creating a Zone

1. Run `/pvpadmin wand`.
2. Left click the first corner block.
3. Right click the opposite corner block.
4. Run `/pvpadmin zone create <name>`.

Example:

```bash
/pvpadmin wand
/pvpadmin zone create arena
```

## Inspecting and Removing Zones

```bash
/pvpadmin zone list
/pvpadmin zone info arena
/pvpadmin zone delete arena
```

## Zone Messages

The plugin supports forced-zone messaging through config keys such as:

```yaml
messages:
  pvp-forced-zone: "&4&lWarning: forced PvP zone."
```

Exit notifications are rate-limited with:

```yaml
zone-exit-cooldowns:
  chat: 3
  actionbar: 0
```

## Operational Notes

- Zone enforcement overrides player preference.
- Zone state is persisted in `zones.yml`.
- Zone creation fails if the selection is incomplete or spans different worlds.
