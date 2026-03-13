package org.PolarNL.PolarPvP.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.PolarNL.PolarPvP.PvPTogglePlugin;
import org.PolarNL.PolarPvP.model.PvPZone;
import org.PolarNL.PolarPvP.util.YamlUtil;

public class ZoneManager {

    private final PvPTogglePlugin plugin;
    private final Map<String, PvPZone> zones = new LinkedHashMap<>();      // key = lowercase name
    private final Map<UUID, Location[]> selections = new HashMap<>();      // [0]=pos1, [1]=pos2
    
    // lru cache for zone lookups with automatic eviction
    // wrapped in synchronizedMap for thread-safety across all operations
    private final Map<String, Boolean> zoneCache = Collections.synchronizedMap(
        new LinkedHashMap<String, Boolean>(10000, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > 10000; // lru eviction when cache exceeds 10k entries
            }
        }
    );
    // sync writes to the zone file
    private final Object saveLock = new Object();

    public ZoneManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }
    
    private void clearZoneCache() {
        zoneCache.clear();
    }
    
    private String getLocationCacheKey(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            return "null:0:0:0"; // fallback for null worlds
        }
        String worldName = world.getName();
        // pre-allocate capacity: world name plus delimiters and coordinates (30 extra chars for colons and coords)
        return new StringBuilder(worldName.length() + 30)
            .append(worldName)
            .append(':')
            .append(loc.getBlockX())
            .append(':')
            .append(loc.getBlockY())
            .append(':')
            .append(loc.getBlockZ())
            .toString();
    }

    // set wand selection
    public void setPosition(UUID playerId, int positionIndex, Location loc) {
        selections.computeIfAbsent(playerId, k -> new Location[2])[positionIndex] = loc.clone();
    }

    // get wand selection
    public Location[] getSelection(UUID playerId) {
        return selections.get(playerId);
    }

    public boolean createZone(String name, UUID playerUUID) {
        Location[] selection = selections.get(playerUUID);
        if (selection == null || selection[0] == null || selection[1] == null) return false;
        World worldA = selection[0].getWorld();
        World worldB = selection[1].getWorld();
        if (worldA == null || worldB == null) return false;
        if (!worldA.getName().equals(worldB.getName())) return false;

        PvPZone zone = new PvPZone(name, worldA.getName(), new PvPZone.Corners(
                selection[0].getBlockX(), selection[0].getBlockY(), selection[0].getBlockZ(),
                selection[1].getBlockX(), selection[1].getBlockY(), selection[1].getBlockZ()));
        synchronized (saveLock) {
            zones.put(name.toLowerCase(), zone);
            clearZoneCache(); // clear cache when zones change
        }
        saveZonesAsync();
        return true;
    }

    public boolean deleteZone(String name) {
        boolean removed;
        synchronized (saveLock) {
            removed = zones.remove(name.toLowerCase()) != null;
            if (removed) {
                clearZoneCache(); // clear cache when zones change
            }
        }
        if (removed) {
            saveZonesAsync();
            return true;
        }
        return false;
    }

    public PvPZone getZone(String name) {
        return zones.get(name.toLowerCase());
    }

    public Collection<PvPZone> getZones() {
        synchronized (saveLock) {
            return Collections.unmodifiableCollection(new java.util.ArrayList<>(zones.values()));
        }
    }

    public Set<String> getZoneNames() {
        synchronized (saveLock) {
            return Collections.unmodifiableSet(
                    new java.util.LinkedHashSet<>(zones.keySet()));
        }
    }

    public boolean isInForcedPvPZone(Location location) {
        if (location == null || location.getWorld() == null) return false;
        
        // check cache first
        String cacheKey = getLocationCacheKey(location);
        Boolean cached = zoneCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // not in cache, check all zones with a snapshot to avoid CME
        Collection<PvPZone> zoneSnapshot;
        synchronized (saveLock) {
            zoneSnapshot = new java.util.ArrayList<>(zones.values());
        }
        
        boolean inZone = false;
        for (PvPZone zone : zoneSnapshot) {
            if (zone.contains(location)) {
                inZone = true;
                break;
            }
        }
        
        // cache the result
        zoneCache.put(cacheKey, inZone);
        
        return inZone;
    }

    // zones.yml i/o

    public void loadZones() {
        ConfigurationSection section = YamlUtil.loadSection(plugin.getDataFolder(), "zones.yml", "zones");
        if (section == null) return;

        int loadedCount;
        synchronized (saveLock) {
            // clear existing zones to remove any deleted from zones.yml
            zones.clear();
            
            for (String key : section.getKeys(false)) {
                ConfigurationSection zoneSection = section.getConfigurationSection(key);
                if (zoneSection == null) continue;
                zones.put(key.toLowerCase(), new PvPZone(
                        zoneSection.getString("name", key),
                        zoneSection.getString("world", "world"),
                        new PvPZone.Corners(
                                zoneSection.getInt("x1"), zoneSection.getInt("y1"), zoneSection.getInt("z1"),
                                zoneSection.getInt("x2"), zoneSection.getInt("y2"), zoneSection.getInt("z2"))
                ));
            }
            loadedCount = zones.size();
            clearZoneCache(); // clear cache when zones are reloaded
        }
        plugin.getLogger().log(Level.INFO, "Loaded {0} PvP zone(s).", loadedCount);
    }

    public void saveZones() {
        synchronized (saveLock) {
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, PvPZone> entry : zones.entrySet()) {
                PvPZone zone = entry.getValue();
                String path = "zones." + entry.getKey();
                config.set(path + ".name",  zone.getName());
                config.set(path + ".world", zone.getWorldName());
                config.set(path + ".x1", zone.getX1());
                config.set(path + ".y1", zone.getY1());
                config.set(path + ".z1", zone.getZ1());
                config.set(path + ".x2", zone.getX2());
                config.set(path + ".y2", zone.getY2());
                config.set(path + ".z2", zone.getZ2());
            }
            YamlUtil.saveConfig(config, plugin.getDataFolder(), "zones.yml",
                    plugin.getLogger(), "Failed to save zones");
        }
    }
    
    private void saveZonesAsync() {
        // snapshot zones under synchronization to prevent ConcurrentModificationException
        final Map<String, PvPZone> zoneSnapshot;
        synchronized (saveLock) {
            zoneSnapshot = new LinkedHashMap<>(zones);
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveZonesSnapshot(zoneSnapshot));
    }
    
    private void saveZonesSnapshot(Map<String, PvPZone> zoneSnapshot) {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, PvPZone> entry : zoneSnapshot.entrySet()) {
            PvPZone zone = entry.getValue();
            String path = "zones." + entry.getKey();
            config.set(path + ".name", zone.getName());
            config.set(path + ".world", zone.getWorldName());
            config.set(path + ".x1", zone.getX1());
            config.set(path + ".y1", zone.getY1());
            config.set(path + ".z1", zone.getZ1());
            config.set(path + ".x2", zone.getX2());
            config.set(path + ".y2", zone.getY2());
            config.set(path + ".z2", zone.getZ2());
        }
        YamlUtil.saveConfig(config, plugin.getDataFolder(), "zones.yml",
                plugin.getLogger(), "Failed to save zones");
    }
}
