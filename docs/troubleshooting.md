---
layout: default
title: Troubleshooting
description: Diagnose common PolarPvP-Manager setup and behavior issues
---

## Plugin Does Not Load

Check the following first:

- server is running Java 17+
- server jar is Bukkit-compatible
- jar is in the correct `plugins/` directory
- no startup errors appear in console

## Player Cannot Toggle PvP

Possible causes:

- missing `pvptoggle.use`
- player is inside a forced PvP zone
- player has active playtime debt

Validation steps:

```bash
/pvp status
/pvpadmin player Steve info
```

## Zone Will Not Create

Common reasons:

- selection was not fully set
- both corners are not in the same world
- a zone with that name already exists

Useful commands:

```bash
/pvpadmin wand
/pvpadmin zone list
/pvpadmin zone info arena
```

## Config Changes Do Not Apply

Reload the plugin after edits:

```bash
/pvpadmin reload
```

If behavior still looks wrong, restart the server and verify the config syntax.

## Debt Does Not Seem to Count Down

The playtime debt system only counts down while 2 or more players are online. That is intentional.

## Enable Debug Logging

For deeper diagnosis:

```yaml
debug: true
```

Then run:

```bash
/pvpadmin reload
```

Check console output, then disable debug mode again when finished.
