package com.pvptoggle.manager;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Ticks once per second for every online player:
 * <ol>
 *   <li>Increments cumulative playtime.</li>
 *   <li>Checks if a new hour-milestone was reached → adds forced-PvP debt.</li>
 *   <li>Counts down debt <b>only while 2+ players are online</b>
 *       (solo time still accumulates hours but the debt timer is paused).</li>
 *   <li>Shows an action-bar HUD while debt is active.</li>
 * </ol>
 */
public class PlaytimeManager {

    private final PvPTogglePlugin plugin;
    private BukkitTask tickTask;
    private BukkitTask saveTask;

    public PlaytimeManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    /* ---- Lifecycle ---- */

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

    /* ---- Core tick ---- */

    private void tick() {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        int hoursPerCycle = plugin.getConfig().getInt("playtime.hours-per-cycle", 1);
        int forcedMinutes = plugin.getConfig().getInt("playtime.forced-minutes", 20);
        long cycleSeconds = hoursPerCycle * 3600L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());

            // 1) Always increment playtime
            data.setTotalPlaytimeSeconds(data.getTotalPlaytimeSeconds() + 1);

            // 2) Check for new cycle milestones
            int currentCycles = (int) (data.getTotalPlaytimeSeconds() / cycleSeconds);
            if (currentCycles > data.getProcessedCycles()) {
                int newCycles = currentCycles - data.getProcessedCycles();
                data.setProcessedCycles(currentCycles);

                // Only generate debt if the player doesn't have the bypass permission
                if (!player.hasPermission("pvptoggle.bypass")) {
                    long additionalDebt = newCycles * forcedMinutes * 60L;
                    data.setPvpDebtSeconds(data.getPvpDebtSeconds() + additionalDebt);
                    MessageUtil.send(player,
                            "&c&l⚔ Forced PvP activated! &7Duration: &f"
                                    + MessageUtil.formatTime(data.getPvpDebtSeconds()));
                }
            }

            // 3) Count down debt (only when 2+ players online)
            if (data.getPvpDebtSeconds() > 0 && !player.hasPermission("pvptoggle.bypass")) {
                if (onlineCount >= 2) {
                    data.setPvpDebtSeconds(data.getPvpDebtSeconds() - 1);
                }

                if (data.getPvpDebtSeconds() <= 0) {
                    data.setPvpDebtSeconds(0);
                    MessageUtil.send(player, "&a&l⚔ Your forced PvP period has ended!");
                    MessageUtil.sendActionBar(player, "&a✓ Forced PvP ended");
                } else {
                    // Show action-bar HUD
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
    }
}
