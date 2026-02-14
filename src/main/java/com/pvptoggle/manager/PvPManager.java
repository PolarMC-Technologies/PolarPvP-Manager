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

public class PvPManager {

    private final PvPTogglePlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    
    // Synchronize writes to player data file
    private final Object saveLock = new Object();

    public PvPManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    // grab or make player data
    public PlayerData getPlayerData(UUID id) {
        return playerDataMap.computeIfAbsent(id, k -> {
            PlayerData d = new PlayerData();
            d.setPvpEnabled(plugin.getConfig().getBoolean("default-pvp-state", false));
            return d;
        });
    }

    // reset everything for a player
    public void resetPlayerData(UUID id) {
        PlayerData d = new PlayerData();
        d.setPvpEnabled(plugin.getConfig().getBoolean("default-pvp-state", false));
        playerDataMap.put(id, d);
    }

    // for admin commands
    public Map<UUID, PlayerData> getAllPlayerData() {
        return Collections.unmodifiableMap(playerDataMap);
    }

    public boolean isEffectivePvPEnabled(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());

        boolean toggle = data.isPvpEnabled();
        boolean inZone = plugin.getZoneManager().isInForcedPvPZone(player.getLocation());
        boolean hasDebt = data.getPvpDebtSeconds() > 0 && !player.hasPermission("pvptoggle.bypass");

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().log(Level.INFO, "[DEBUG] PvP check for {0}: toggle={1}, inZone={2}, hasDebt={3}",
                    new Object[]{player.getName(), toggle, inZone, hasDebt});
        }

        return toggle || inZone || hasDebt;
    }

    public boolean isForcedPvP(Player player) {
        if (plugin.getZoneManager().isInForcedPvPZone(player.getLocation())) return true;
        PlayerData data = getPlayerData(player.getUniqueId());
        return data.getPvpDebtSeconds() > 0 && !player.hasPermission("pvptoggle.bypass");
    }

    // playerdata.yml i/o

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
        synchronized (saveLock) {
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
}
