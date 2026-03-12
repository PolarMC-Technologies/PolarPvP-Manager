# Testing Guide for Performance Improvements

This document provides testing instructions to validate the performance improvements made to PolarPvP-Manager.

## Prerequisites

- Java 17+
- Bukkit/Spigot/Paper/Purpur server 1.20.4+
- Multiple test accounts (recommend 10+ for load testing)
- Server monitoring tools (recommended: Spark profiler, Timings)

## Building the Plugin

```bash
mvn clean package
```

The jar will be in `target/PolarPvP-Manager-1.0.0.jar`

## Test Cases

### 1. Config Caching Test

**Objective:** Verify config values are cached and not read every tick

**Steps:**
1. Enable debug mode in `config.yml`: `debug: true`
2. Start the server and observe logs
3. Modify `playtime.hours-per-cycle` in config without reloading
4. Wait for a playtime cycle to complete
5. Run `/pvpadmin reload`
6. Verify the new config value is now used

**Expected Result:**
- Config changes only take effect after reload command
- No performance degradation even with debug enabled

### 2. Action Bar Throttling Test

**Objective:** Verify action bar messages are throttled to 1/second

**Steps:**
1. Set a player's PvP debt: `/pvpadmin player <name> setdebt 300`
2. Have the player log in
3. Observe action bar updates in-game
4. Use F3 debug screen to monitor network traffic (optional)

**Expected Result:**
- Action bar updates once per second (smooth, not flickering)
- Reduced network packet spam
- No visible lag or stutter

### 3. Async Save Performance Test

**Objective:** Verify saves don't block the main thread

**Steps:**
1. Set `save-interval: 1` in config (save every minute)
2. Have 10+ players online simultaneously
3. Monitor server TPS during save operations
4. Use profiler to check for main thread blocking

**Expected Result:**
- No TPS drops during periodic saves
- Main thread shows no blocking I/O operations
- All player data saved correctly

### 4. Zone Cache Performance Test

**Objective:** Verify zone lookups are cached and fast

**Steps:**
1. Create 10+ PvP zones with `/pvpadmin zone create`
2. Have players walk through zones repeatedly
3. Monitor zone entry/exit messages
4. Use profiler to check zone lookup performance

**Expected Result:**
- Zone entry/exit messages appear correctly
- Minimal CPU usage for zone checks
- Cache hits avoid full zone iteration

### 5. Player Quit Save Test

**Objective:** Verify data saves on player disconnect

**Steps:**
1. Give a player PvP debt: `/pvpadmin player <name> setdebt 120`
2. Have the player disconnect
3. Wait 5 seconds
4. Check `playerdata.yml` file
5. Have the player reconnect
6. Verify debt is still present

**Expected Result:**
- Data saved correctly on disconnect
- No data loss
- Async save doesn't cause logout lag

### 6. Server Shutdown Save Test

**Objective:** Verify synchronous saves on shutdown prevent data loss

**Steps:**
1. Have multiple players online with various debt amounts
2. Make zone changes
3. Stop the server with `/stop`
4. Restart the server
5. Verify all data is preserved

**Expected Result:**
- All player data saved
- All zone data saved
- No corruption in YAML files

### 7. Memory Leak Test

**Objective:** Verify no memory leaks from player tracking

**Steps:**
1. Have 20+ players join and quit repeatedly
2. Monitor heap memory usage over time
3. Check action bar tracking map doesn't grow unbounded
4. Use profiler to inspect memory usage

**Expected Result:**
- Memory usage remains stable
- Disconnected player UUIDs are cleaned up
- No OutOfMemoryError over extended testing

### 8. Multi-Player Load Test

**Objective:** Test performance with realistic player counts

**Steps:**
1. Have 20+ players online simultaneously
2. Players should:
   - Walk through zones
   - Attack each other
   - Use `/pvp` commands
   - Have various debt amounts
3. Monitor server TPS
4. Check for lag or stuttering

**Expected Result:**
- Server maintains 20 TPS
- No lag spikes
- Smooth gameplay experience

### 9. Config Reload Test

**Objective:** Verify all caches update on config reload

**Steps:**
1. Start server with default config
2. Modify config values:
   - `debug: true`
   - `zone-wand-material: STICK`
   - `playtime.hours-per-cycle: 2`
3. Run `/pvpadmin reload`
4. Test each modified feature

**Expected Result:**
- Debug logs now appear
- Zone wand is now a stick
- Playtime cycle reflects new value

### 10. Thread Safety Test

**Objective:** Verify no race conditions in concurrent operations

**Steps:**
1. Have multiple players quit simultaneously
2. Trigger zone creates/deletes during saves
3. Modify player data during background saves
4. Monitor for exceptions or corruption

**Expected Result:**
- No ConcurrentModificationException
- No file corruption
- All operations complete successfully

## Performance Benchmarks

### Before Improvements
- **Tick Time**: ~3-6ms with 10 players
- **Action Bar Spam**: 20 packets/second per player with debt
- **Save Operations**: 50-200ms blocking main thread
- **Zone Lookups**: O(n) iteration through all zones

### After Improvements (Expected)
- **Tick Time**: ~1-3ms with 10 players (2-3ms improvement)
- **Action Bar Spam**: 1 packet/second per player (95% reduction)
- **Save Operations**: <1ms (async, non-blocking)
- **Zone Lookups**: <0.1ms (cached O(1) hits)

## Profiling Tools

### Spark (Recommended)
```
/spark profiler --timeout 120
/spark profiler --stop
```

### Timings (Spigot/Paper)
```
/timings on
/timings paste
```

## Monitoring Commands

```bash
# Check player data
/pvpadmin player <name> info

# Check zones
/pvpadmin zone list
/pvpadmin zone info <name>

# Test playtime simulation
/pvpadmin simtime 3600

# Reload config
/pvpadmin reload
```

## Known Limitations

1. **Async Saves on Quit**: If server force-kills immediately after player quit, async save may not complete. Server shutdown performs synchronous save as safety net.

2. **Cache Size**: Zone cache limited to 10,000 entries with LRU eviction. This should handle most use cases, but extremely large worlds may benefit from increased limit.

3. **Action Bar Throttle**: Fixed at 1 update/second. If finer control needed, make throttle configurable.

## Troubleshooting

### Issue: Config changes not applying
**Solution**: Run `/pvpadmin reload` after modifying config.yml

### Issue: Zone detection not working
**Solution**: 
- Check zone creation with `/pvpadmin zone info <name>`
- Verify both corners are in same world
- Clear cache by reloading zones

### Issue: Data not saving
**Solution**:
- Check file permissions on plugin folder
- Look for IOExceptions in server logs
- Verify YAML files are not corrupted

### Issue: Memory usage growing
**Solution**:
- Check for leaked tracking maps
- Monitor zone cache size
- Verify player cleanup on quit

## Reporting Issues

When reporting issues, include:
1. Server version (Bukkit/Spigot/Paper)
2. Java version
3. Number of players online
4. Relevant config settings
5. Profiler data or timings report
6. Server logs with debug enabled
7. Steps to reproduce

## Success Criteria

All tests pass if:
- ✅ No data loss or corruption
- ✅ Server maintains 20 TPS under load
- ✅ No memory leaks over extended testing
- ✅ Zone detection accurate and fast
- ✅ Config reloads update all caches
- ✅ No exceptions or errors in logs
- ✅ Measurable performance improvements
