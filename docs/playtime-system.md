---
layout: default
title: Playtime System
description: Understand how playtime debt forces PvP after configured playtime cycles
---

PolarPvP-Manager can force players into PvP after they accumulate a configured amount of playtime.

## Configuration

```yaml
playtime:
  hours-per-cycle: 1
  forced-minutes: 20
```

With the default values:

- each 1 hour of playtime creates a cycle
- each cycle adds 20 minutes of forced PvP debt

## How It Works

1. A player accumulates normal playtime while online.
2. When their total playtime crosses a configured cycle threshold, the plugin records a processed cycle.
3. The player gains forced PvP debt for that cycle.
4. While debt is active, PvP cannot be turned off.
5. Debt only counts down when at least 2 players are online.
6. Logging out does not clear debt.

## Player Experience

When debt is active:

- `/pvp off` is blocked
- the player sees the configured forced-playtime message
- `/pvp status` shows the remaining debt time

Config message example:

```yaml
messages:
  pvp-forced-playtime: "&4&lForced PvP active! &f%time% &cremaining."
```

## Bypass Behavior

Players with `pvptoggle.bypass` are exempt from playtime-based forced PvP.

## Testing

Use the built-in simulation command to avoid waiting:

```bash
/pvpadmin simtime 3600
```

Then verify the result with:

```bash
/pvp status
```

## Admin Inspection

To inspect an affected player:

```bash
/pvpadmin player Steve info
```

To set or clear debt manually:

```bash
/pvpadmin player Steve setdebt 1200
/pvpadmin player Steve setdebt 0
```
