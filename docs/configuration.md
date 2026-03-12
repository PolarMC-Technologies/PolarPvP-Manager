---
layout: default
title: Configuration Reference
description: Configure default PvP state, messages, zone tools, and playtime debt
---

The main configuration file lives at `plugins/PolarPvP-Manager/config.yml`.

## Overview

The config controls:

- Default PvP behavior for new players
- Player-facing messages
- Playtime debt timing
- Zone wand material
- Zone exit message cooldowns
- Save interval and debug logging

## Core Settings

```yaml
default-pvp-state: false

playtime:
  hours-per-cycle: 1
  forced-minutes: 20

zone-wand-material: BLAZE_ROD

zone-exit-cooldowns:
  chat: 3
  actionbar: 0

save-interval: 5
debug: false
```

## Default PvP State

```yaml
default-pvp-state: false
```

Controls whether new players begin with PvP enabled or disabled.

- `true`: new players start with PvP on
- `false`: new players start with PvP off

## Messages

All player-facing strings live under `messages:` and support standard Bukkit color codes.

Example keys:

```yaml
messages:
  pvp-enabled: "&a&lPvP enabled!"
  pvp-disabled: "&c&lPvP disabled."
  pvp-forced-zone: "&4&lForced PvP zone!"
  pvp-forced-playtime: "&4&lForced PvP active! &f%time% &cremaining."
```

Useful format codes:

- `&a` green
- `&c` red
- `&e` yellow
- `&7` gray
- `&l` bold

## Playtime Settings

```yaml
playtime:
  hours-per-cycle: 1
  forced-minutes: 20
```

- `hours-per-cycle`: how much accumulated playtime triggers a forced PvP cycle
- `forced-minutes`: how long forced PvP lasts for each processed cycle

See [Playtime System](playtime-system) for behavior details.

## Zone Tooling

```yaml
zone-wand-material: BLAZE_ROD
```

Any valid Bukkit material can be used as the zone selector wand.

## Zone Exit Cooldowns

```yaml
zone-exit-cooldowns:
  chat: 3
  actionbar: 0
```

These values control how often players can receive leave-zone notifications.

- `chat`: cooldown in seconds for chat messages
- `actionbar`: cooldown in seconds for action bar messages
- `0`: no cooldown

## Persistence and Debugging

```yaml
save-interval: 5
debug: false
```

- `save-interval`: automatic save cadence in minutes
- `debug`: enables extra console logging for troubleshooting

## Applying Changes

Most config changes can be applied with:

```bash
/pvpadmin reload
```
