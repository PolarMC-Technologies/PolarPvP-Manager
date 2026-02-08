package com.pvptoggle.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.util.MessageUtil;

/*
 * Runs a 1-second tick for every online player: tracks cumulative playtime,
 * generates forced-PvP debt at hour milestones, and counts it down (only
 * when 2+ players are online). Also handles the action-bar HUD + auto-save.
 */
public class PlaytimeManager {

    private final PvPTogglePlugin plugin;
    private BukkitTask tickTask;
    private BukkitTask saveTask;

    public PlaytimeManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    public void startTracking() {
        // Main tick — every 20 ticks (1 second)
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 20L, 20L);

        // Auto-save
        long saveIntervalTicks = plugin.getConfig().getInt("save-interval", 5) * 60L * 20L;
        saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getPvPManager().saveData();
                plugin.getZoneManager().saveZones();
            }
        }.runTaskTimer(plugin, saveIntervalTicks, saveIntervalTicks);
    }

    public void stopTracking() {
        if (tickTask != null) tickTask.cancel();
        if (saveTask != null) saveTask.cancel();
    }

    private void tick() {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        long cycleSeconds = plugin.getConfig().getInt("playtime.hours-per-cycle", 1) * 3600L;
        int forcedMinutes = plugin.getConfig().getInt("playtime.forced-minutes", 20);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
            data.setTotalPlaytimeSeconds(data.getTotalPlaytimeSeconds() + 1);
            checkCycleMilestones(player, data, cycleSeconds, forcedMinutes);
            tickDebt(player, data, onlineCount);
        }
    }

    private void checkCycleMilestones(Player player, PlayerData data, long cycleSeconds, int forcedMinutes) {
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

    private void tickDebt(Player player, PlayerData data, int onlineCount) {
        if (data.getPvpDebtSeconds() <= 0 || player.hasPermission("pvptoggle.bypass")) return;

        if (onlineCount >= 2) {
            data.setPvpDebtSeconds(data.getPvpDebtSeconds() - 1);
        }

        if (data.getPvpDebtSeconds() <= 0) {
            data.setPvpDebtSeconds(0);
            MessageUtil.send(player, "&a&l⚔ Your forced PvP period has ended!");
            MessageUtil.sendActionBar(player, "&a✓ Forced PvP ended");
        } else {
            String status = (onlineCount >= 2)
                    ? "&c⚔ Forced PvP"
                    : "&e⚔ Forced PvP &7(paused — solo)";
            MessageUtil.sendActionBar(player,
                    status + " &7| &f"
                            + MessageUtil.formatTime(data.getPvpDebtSeconds())
                            + " &7remaining");
        }
    }
}
