package com.pvptoggle.manager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;

// Player data + effective PvP state resolution + playerdata.yml persistence
public class PvPManager {

    private final PvPTogglePlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PvPManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    /** Returns the data for this player, creating defaults if needed. */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> {
            PlayerData data = new PlayerData();
            data.setPvpEnabled(plugin.getConfig().getBoolean("default-pvp-state", false));
            return data;
        });
    }

    /** Replace data with fresh defaults. */
    public void resetPlayerData(UUID uuid) {
        PlayerData data = new PlayerData();
        data.setPvpEnabled(plugin.getConfig().getBoolean("default-pvp-state", false));
        playerDataMap.put(uuid, data);
    }

    /** Read-only view of all stored data (for admin commands). */
    public Map<UUID, PlayerData> getAllPlayerData() {
        return Collections.unmodifiableMap(playerDataMap);
    }

    /**
     * Whether PvP is effectively on for this player right now,
     * considering their toggle, zones, and playtime debt.
     */
    public boolean isEffectivePvPEnabled(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());

        // Manual toggle
        if (data.isPvpEnabled()) return true;

        // Inside a forced-PvP zone (no bypass)
        if (plugin.getZoneManager().isInForcedPvPZone(player.getLocation())) return true;

        // Playtime debt (bypassable by permission)
        return data.getPvpDebtSeconds() > 0 && !player.hasPermission("pvptoggle.bypass");
    }

    /** True if player can't toggle PvP off right now (zone or debt). */
    public boolean isForcedPvP(Player player) {
        if (plugin.getZoneManager().isInForcedPvPZone(player.getLocation())) return true;
        PlayerData data = getPlayerData(player.getUniqueId());
        return data.getPvpDebtSeconds() > 0 && !player.hasPermission("pvptoggle.bypass");
    }

    // ---- playerdata.yml I/O ----

    public void loadData() {
        File file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection players = config.getConfigurationSection("players");
        if (players == null) return;

        for (String uuidStr : players.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection s = players.getConfigurationSection(uuidStr);
                if (s == null) continue;

                PlayerData data = new PlayerData();
                data.setPvpEnabled(s.getBoolean("pvp-enabled", false));
                data.setTotalPlaytimeSeconds(s.getLong("total-playtime-seconds", 0));
                data.setProcessedCycles(s.getInt("processed-cycles", 0));
                data.setPvpDebtSeconds(s.getLong("pvp-debt-seconds", 0));
                playerDataMap.put(uuid, data);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Skipping invalid UUID in playerdata.yml: {0}", uuidStr);
            }
        }
        plugin.getLogger().log(Level.INFO, "Loaded data for {0} players.", playerDataMap.size());
    }

    public void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            String path = "players." + entry.getKey().toString();
            PlayerData d = entry.getValue();
            config.set(path + ".pvp-enabled",            d.isPvpEnabled());
            config.set(path + ".total-playtime-seconds", d.getTotalPlaytimeSeconds());
            config.set(path + ".processed-cycles",       d.getProcessedCycles());
            config.set(path + ".pvp-debt-seconds",       d.getPvpDebtSeconds());
        }
        try {
            config.save(new File(plugin.getDataFolder(), "playerdata.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data", e);
        }
    }
}
