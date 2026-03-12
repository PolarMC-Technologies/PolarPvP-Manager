package com.pvptoggle.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.util.MessageUtil;

import java.util.UUID;

public class PlaytimeManager {

    private final PvPTogglePlugin plugin;
    private BukkitTask tickTask;
    private BukkitTask saveTask;
    
    // Cached config values (updated on reload)
    private long cycleSeconds;
    private int forcedMinutes;

    public PlaytimeManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
        loadConfigValues();
    }
    
    /**
     * Load and cache config values to avoid reading config 20x per second
     */
    public void loadConfigValues() {
        int hoursPerCycle = plugin.getConfig().getInt("playtime.hours-per-cycle", 1);
        
        // Validate hours-per-cycle to prevent division by zero
        if (hoursPerCycle <= 0) {
            plugin.getLogger().warning("[PvPToggle] Invalid value for 'playtime.hours-per-cycle' (" 
                    + hoursPerCycle + "); using 1 instead.");
            hoursPerCycle = 1;
        }
        
        this.cycleSeconds = hoursPerCycle * 3600L;
        this.forcedMinutes = plugin.getConfig().getInt("playtime.forced-minutes", 20);
    }

    public void startTracking() {
        // 1 second tick
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerTimesAndDebt();
            }
        }.runTaskTimer(plugin, 20L, 20L);

        // Auto-save (async to prevent blocking)
        long saveIntervalTicks = plugin.getConfig().getInt("save-interval", 5) * 60L * 20L;
        saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveDataAsync();
            }
        }.runTaskTimer(plugin, saveIntervalTicks, saveIntervalTicks);
    }
    
    /**
     * Save data asynchronously to prevent blocking the main thread
     */
    private void saveDataAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getPvPManager().saveData();
            plugin.getZoneManager().saveZones();
        });
    }

    public void stopTracking() {
        if (tickTask != null) tickTask.cancel();
        if (saveTask != null) saveTask.cancel();
    }
    
    /**
     * Clean up action bar tracking for a player (called when they disconnect).
     * No longer performs any operation as throttling has been removed.
     * 
     * @param playerId the UUID of the player
     * @deprecated Since 1.0.0: removal of action bar throttling; no replacement needed as
     *             cleanup is no longer required. Safe to call but performs no operation.
     */
    @Deprecated(forRemoval = true, since = "1.0.0")
    public void cleanupPlayer(UUID playerId) {
        // No-op: throttling removed since task already runs at 1-second intervals
    }

    private void updatePlayerTimesAndDebt() {
        // Cache online player count once per tick instead of reading multiple times
        int onlinePlayerCount = Bukkit.getOnlinePlayers().size();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
            data.setTotalPlaytimeSeconds(data.getTotalPlaytimeSeconds() + 1);
            checkAndApplyCycleMilestones(player, data);
            decrementPlayerDebt(player, data, onlinePlayerCount);
        }
    }

    private void checkAndApplyCycleMilestones(Player player, PlayerData data) {
        int currentCycles = (int) (data.getTotalPlaytimeSeconds() / cycleSeconds);
        if (currentCycles <= data.getProcessedCycles()) return;

        int newCycles = currentCycles - data.getProcessedCycles();
        data.setProcessedCycles(currentCycles);

        if (!player.hasPermission("pvptoggle.bypass")) {
            long additionalDebt = newCycles * forcedMinutes * 60L;
            data.setPvpDebtSeconds(data.getPvpDebtSeconds() + additionalDebt);
            MessageUtil.send(player,
                    "&c&l⚔ Forced PvP activated! &7Duration: &f"
                            + MessageUtil.formatTime(data.getPvpDebtSeconds()));
        }
    }

    private void decrementPlayerDebt(Player player, PlayerData data, int onlinePlayerCount) {
        if (data.getPvpDebtSeconds() <= 0 || player.hasPermission("pvptoggle.bypass")) return;

        if (onlinePlayerCount >= 2) {
            data.setPvpDebtSeconds(data.getPvpDebtSeconds() - 1);
        }

        if (data.getPvpDebtSeconds() <= 0) {
            data.setPvpDebtSeconds(0);
            MessageUtil.send(player, "&a&l⚔ Your forced PvP period has ended!");
            MessageUtil.sendActionBar(player, "&a✓ Forced PvP ended");
        } else {
            // Action bar shown once per second (task runs every 20 ticks / 1 second)
            String status = (onlinePlayerCount >= 2)
                    ? "&c⚔ Forced PvP"
                    : "&e⚔ Forced PvP &7(paused — solo)";
            MessageUtil.sendActionBar(player,
                    status + " &7| &f"
                            + MessageUtil.formatTime(data.getPvpDebtSeconds())
                            + " &7remaining");
        }
    }
}
