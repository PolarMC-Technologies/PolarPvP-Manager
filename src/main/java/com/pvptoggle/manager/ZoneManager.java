package com.pvptoggle.manager;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PvPZone;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages forced-PvP zones (cuboid regions) and per-player corner selections.
 */
public class ZoneManager {

    private final PvPTogglePlugin plugin;
    private final Map<String, PvPZone> zones = new LinkedHashMap<>();      // key = lowercase name
    private final Map<UUID, Location[]> selections = new HashMap<>();      // [0]=pos1, [1]=pos2

    public ZoneManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    /* ================================================================
     *  Selection helpers  (used by the zone-wand listener)
     * ================================================================ */

    /** Set position 1 (index 0) or position 2 (index 1). */
    public void setPosition(UUID playerUUID, int index, Location location) {
        selections.computeIfAbsent(playerUUID, k -> new Location[2])[index] = location.clone();
    }

    public Location[] getSelection(UUID playerUUID) {
        return selections.get(playerUUID);
    }

    /* ================================================================
     *  Zone CRUD
     * ================================================================ */

    /**
     * Create a zone from the player's current selection.
     * @return true on success, false if the selection is incomplete or cross-world.
     */
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
        saveZones();
        return true;
    }

    public boolean deleteZone(String name) {
        if (zones.remove(name.toLowerCase()) != null) {
            saveZones();
            return true;
        }
        return false;
    }

    /** @return the zone with the given name, or null. */
    public PvPZone getZone(String name) {
        return zones.get(name.toLowerCase());
    }

    public Collection<PvPZone> getZones() {
        return Collections.unmodifiableCollection(zones.values());
    }

    public Set<String> getZoneNames() {
        return Collections.unmodifiableSet(zones.keySet());
    }

    /* ================================================================
     *  Spatial query
     * ================================================================ */

    /** @return true if the location is inside any forced-PvP zone. */
    public boolean isInForcedPvPZone(Location location) {
        if (location == null) return false;
        for (PvPZone zone : zones.values()) {
            if (zone.contains(location)) return true;
        }
        return false;
    }

    /* ================================================================
     *  Persistence  (zones.yml)
     * ================================================================ */

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
