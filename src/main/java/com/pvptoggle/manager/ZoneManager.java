package com.pvptoggle.manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PvPZone;

public class ZoneManager {

    private final PvPTogglePlugin plugin;
    private final Map<String, PvPZone> zones = new LinkedHashMap<>();      // key = lowercase name
    private final Map<UUID, Location[]> selections = new HashMap<>();      // [0]=pos1, [1]=pos2
    
    // LRU cache for zone lookups with automatic eviction
    private final Map<String, Boolean> zoneCache = new LinkedHashMap<String, Boolean>(1000, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > 10000; // LRU eviction when cache exceeds 10k entries
        }
    };
    // Synchronize writes to zone file
    private final Object saveLock = new Object();

    public ZoneManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Clear the zone cache (called when zones are modified)
     */
    private void clearZoneCache() {
        synchronized (zoneCache) {
            zoneCache.clear();
        }
    }
    
    /**
     * Generate a cache key for a location
     * Uses StringBuilder for performance as this is called frequently
     */
    private String getLocationCacheKey(Location loc) {
        return new StringBuilder(64)
            .append(loc.getWorld().getName())
            .append(':')
            .append(loc.getBlockX())
            .append(':')
            .append(loc.getBlockY())
            .append(':')
            .append(loc.getBlockZ())
            .toString();
    }

    // set wand selection
    public void setPosition(UUID playerId, int idx, Location loc) {
        selections.computeIfAbsent(playerId, k -> new Location[2])[idx] = loc.clone();
    }

    // get wand selection
    public Location[] getSelection(UUID playerId) {
        return selections.get(playerId);
    }

    public boolean createZone(String name, UUID playerUUID) {
        Location[] sel = selections.get(playerUUID);
        if (sel == null || sel[0] == null || sel[1] == null) return false;
        World worldA = sel[0].getWorld();
        World worldB = sel[1].getWorld();
        if (worldA == null || worldB == null) return false;
        if (!worldA.getName().equals(worldB.getName())) return false;

        PvPZone zone = new PvPZone(name, worldA.getName(), new PvPZone.Corners(
                sel[0].getBlockX(), sel[0].getBlockY(), sel[0].getBlockZ(),
                sel[1].getBlockX(), sel[1].getBlockY(), sel[1].getBlockZ()));
        zones.put(name.toLowerCase(), zone);
        clearZoneCache(); // Clear cache when zones change
        saveZonesAsync();
        return true;
    }

    public boolean deleteZone(String name) {
        if (zones.remove(name.toLowerCase()) != null) {
            clearZoneCache(); // Clear cache when zones change
            saveZonesAsync();
            return true;
        }
        return false;
    }

    public PvPZone getZone(String name) {
        return zones.get(name.toLowerCase());
    }

    public Collection<PvPZone> getZones() {
        return Collections.unmodifiableCollection(zones.values());
    }

    public Set<String> getZoneNames() {
        return Collections.unmodifiableSet(zones.keySet());
    }

    public boolean isInForcedPvPZone(Location location) {
        if (location == null) return false;
        
        // Check cache first
        String cacheKey = getLocationCacheKey(location);
        synchronized (zoneCache) {
            Boolean cached = zoneCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        
        // Not in cache, check all zones
        boolean inZone = false;
        for (PvPZone zone : zones.values()) {
            if (zone.contains(location)) {
                inZone = true;
                break;
            }
        }
        
        // Cache the result
        synchronized (zoneCache) {
            zoneCache.put(cacheKey, inZone);
        }
        
        return inZone;
    }

    // zones.yml i/o

    public void loadZones() {
        File file = new File(plugin.getDataFolder(), "zones.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("zones");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);
            if (s == null) continue;
            zones.put(key.toLowerCase(), new PvPZone(
                    s.getString("name", key),
                    s.getString("world", "world"),
                    new PvPZone.Corners(
                            s.getInt("x1"), s.getInt("y1"), s.getInt("z1"),
                            s.getInt("x2"), s.getInt("y2"), s.getInt("z2"))
            ));
        }
        plugin.getLogger().log(Level.INFO, "Loaded {0} PvP zone(s).", zones.size());
    }

    public void saveZones() {
        synchronized (saveLock) {
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, PvPZone> entry : zones.entrySet()) {
                PvPZone z = entry.getValue();
                String path = "zones." + entry.getKey();
                config.set(path + ".name",  z.getName());
                config.set(path + ".world", z.getWorldName());
                config.set(path + ".x1", z.getX1());
                config.set(path + ".y1", z.getY1());
                config.set(path + ".z1", z.getZ1());
                config.set(path + ".x2", z.getX2());
                config.set(path + ".y2", z.getY2());
                config.set(path + ".z2", z.getZ2());
            }
            try {
                config.save(new File(plugin.getDataFolder(), "zones.yml"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save zones", e);
            }
        }
    }
    
    /**
     * Save zones asynchronously to prevent blocking the main thread
     */
    private void saveZonesAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveZones);
    }
}
