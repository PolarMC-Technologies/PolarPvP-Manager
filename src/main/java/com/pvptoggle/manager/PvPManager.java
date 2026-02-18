package com.pvptoggle.manager;

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
import com.pvptoggle.util.DebugUtil;
import com.pvptoggle.util.YamlUtil;

public class PvPManager {

    private final PvPTogglePlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    
    // Synchronize writes to player data file
    private final Object saveLock = new Object();

    public PvPManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    // grab or make player data
    public PlayerData getPlayerData(UUID playerUuid) {
        return playerDataMap.computeIfAbsent(playerUuid, k -> {
            PlayerData data = new PlayerData();
            data.setPvpEnabled(plugin.getConfig().getBoolean("default-pvp-state", false));
            return data;
        });
    }

    // reset everything for a player
    public void resetPlayerData(UUID playerUuid) {
        PlayerData data = new PlayerData();
        data.setPvpEnabled(plugin.getConfig().getBoolean("default-pvp-state", false));
        playerDataMap.put(playerUuid, data);
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

        DebugUtil.logDebug(plugin.getConfig(), plugin.getLogger(),
                "PvP check for {0}: toggle={1}, inZone={2}, hasDebt={3}",
                player.getName(), toggle, inZone, hasDebt);

        return toggle || inZone || hasDebt;
    }

    public boolean isForcedPvP(Player player) {
        if (plugin.getZoneManager().isInForcedPvPZone(player.getLocation())) return true;
        PlayerData data = getPlayerData(player.getUniqueId());
        return data.getPvpDebtSeconds() > 0 && !player.hasPermission("pvptoggle.bypass");
    }

    // playerdata.yml i/o

    public void loadData() {
        ConfigurationSection players = YamlUtil.loadSection(plugin.getDataFolder(), "playerdata.yml", "players");
        if (players == null) return;

        for (String uuidStr : players.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection section = players.getConfigurationSection(uuidStr);
                if (section == null) continue;

                PlayerData data = new PlayerData();
                data.setPvpEnabled(section.getBoolean("pvp-enabled", false));
                data.setTotalPlaytimeSeconds(section.getLong("total-playtime-seconds", 0));
                data.setProcessedCycles(section.getInt("processed-cycles", 0));
                data.setPvpDebtSeconds(section.getLong("pvp-debt-seconds", 0));
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
                PlayerData data = entry.getValue();
                config.set(path + ".pvp-enabled",            data.isPvpEnabled());
                config.set(path + ".total-playtime-seconds", data.getTotalPlaytimeSeconds());
                config.set(path + ".processed-cycles",       data.getProcessedCycles());
                config.set(path + ".pvp-debt-seconds",       data.getPvpDebtSeconds());
            }
            YamlUtil.saveConfig(config, plugin.getDataFolder(), "playerdata.yml",
                    plugin.getLogger(), "Failed to save player data");
        }
    }
}
