package com.pvptoggle.manager;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.util.MessageUtil;


public class PlaytimeManager {

    private final PvPTogglePlugin plugin;
    private BukkitTask tickTask;
    private BukkitTask saveTask;

    public PlaytimeManager(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    public void startTracking() {
        // 1 second tick
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
        boolean forceTimerEnabled = plugin.getConfig().getBoolean("pvp-force-timer.enabled", false);
        boolean debug = plugin.getConfig().getBoolean("debug", false);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
            data.setTotalPlaytimeSeconds(data.getTotalPlaytimeSeconds() + 1);

            if (forceTimerEnabled) {
                tickForceTimer(player, data, onlineCount, debug);
            } else {
                // Legacy cycle-based mode
                long cycleSeconds = plugin.getConfig().getInt("playtime.hours-per-cycle", 1) * 3600L;
                int forcedMinutes = plugin.getConfig().getInt("playtime.forced-minutes", 20);
                checkCycleMilestones(player, data, cycleSeconds, forcedMinutes);
                tickDebtLegacy(player, data, onlineCount);
            }
        }
    }

    // ─── Debt-ratio mode (pvp-force-timer) ───────────────────────────────

    private void tickForceTimer(Player player, PlayerData data, int onlineCount, boolean debug) {
        if (player.hasPermission("pvptoggle.bypass")) return;

        int debtRatio = plugin.getConfig().getInt("pvp-force-timer.debt-ratio", 5);
        long maxDebtSeconds = plugin.getConfig().getInt("pvp-force-timer.max-debt", 60) * 60L;
        long minForcedSeconds = plugin.getConfig().getInt("pvp-force-timer.minimum-forced-duration", 20) * 60L;
        boolean exemptManualPvp = plugin.getConfig().getBoolean("pvp-force-timer.exemptions.manual-pvp", true);
        boolean exemptForcedZones = plugin.getConfig().getBoolean("pvp-force-timer.exemptions.forced-zones", true);
        boolean exemptSolo = plugin.getConfig().getBoolean("pvp-force-timer.exemptions.solo-server", true);

        boolean currentlyForced = data.getPvpDebtSeconds() > 0;

        // ── Phase 1: debt accumulation ──
        if (!currentlyForced) {
            boolean exempt = false;

            // Solo server exemption
            if (exemptSolo && onlineCount < 2) {
                exempt = true;
                if (debug) {
                    plugin.getLogger().log(Level.INFO,
                            "[DEBUG] {0}: exempt from debt — solo server", player.getName());
                }
            }

            // Manual PvP exemption
            if (!exempt && exemptManualPvp && data.isPvpEnabled()) {
                exempt = true;
                if (debug) {
                    plugin.getLogger().log(Level.INFO,
                            "[DEBUG] {0}: exempt from debt — manual PvP on", player.getName());
                }
            }

            // Forced zone exemption
            if (!exempt && exemptForcedZones
                    && plugin.getZoneManager().isInForcedPvPZone(player.getLocation())) {
                exempt = true;
                if (debug) {
                    plugin.getLogger().log(Level.INFO,
                            "[DEBUG] {0}: exempt from debt — in forced PvP zone", player.getName());
                }
            }

            if (!exempt) {
                // Accumulate PvP-off time
                data.setPvpOffAccumulator(data.getPvpOffAccumulator() + 1);

                // Convert accumulated time to debt using ratio
                long ratioSeconds = debtRatio * 60L; // ratio is in minutes
                if (data.getPvpOffAccumulator() >= ratioSeconds) {
                    long earnedDebt = 60L; // 1 minute of debt per ratio-block
                    long newDebt = Math.min(data.getPvpDebtSeconds() + earnedDebt, maxDebtSeconds);
                    data.setPvpDebtSeconds(newDebt);
                    data.setPvpOffAccumulator(data.getPvpOffAccumulator() - ratioSeconds);

                    if (debug) {
                        plugin.getLogger().log(Level.INFO,
                                "[DEBUG] {0}: +{1}s debt (total {2}s, cap {3}s)",
                                new Object[]{player.getName(), earnedDebt,
                                        data.getPvpDebtSeconds(), maxDebtSeconds});
                    }
                }

                // When debt first becomes positive, start the forced block
                if (data.getPvpDebtSeconds() > 0 && data.getForcedPvpElapsed() == 0) {
                    // Enforce minimum forced duration: bump debt up if below minimum
                    if (data.getPvpDebtSeconds() < minForcedSeconds) {
                        data.setPvpDebtSeconds(minForcedSeconds);
                    }
                    MessageUtil.send(player,
                            "&c&l⚔ Forced PvP activated! &7Duration: &f"
                                    + MessageUtil.formatTime(data.getPvpDebtSeconds()));
                }
            }
        }

        // ── Phase 2: debt payoff ──
        if (data.getPvpDebtSeconds() > 0) {
            // Solo server exemption — pause debt countdown when alone
            if (exemptSolo && onlineCount < 2) {
                MessageUtil.sendActionBar(player,
                        "&e⚔ Forced PvP &7(paused — solo) &7| &f"
                                + MessageUtil.formatTime(data.getPvpDebtSeconds()) + " &7remaining");
                return;
            }

            data.setPvpDebtSeconds(data.getPvpDebtSeconds() - 1);
            data.setForcedPvpElapsed(data.getForcedPvpElapsed() + 1);

            if (data.getPvpDebtSeconds() <= 0) {
                // Forced period ended
                data.setPvpDebtSeconds(0);
                data.setForcedPvpElapsed(0);
                data.setPvpOffAccumulator(0);
                MessageUtil.send(player, "&a&l⚔ Your forced PvP period has ended!");
                MessageUtil.sendActionBar(player, "&a✓ Forced PvP ended");

                if (debug) {
                    plugin.getLogger().log(Level.INFO,
                            "[DEBUG] {0}: forced PvP period ended", player.getName());
                }
            } else {
                MessageUtil.sendActionBar(player,
                        "&c⚔ Forced PvP &7| &f"
                                + MessageUtil.formatTime(data.getPvpDebtSeconds())
                                + " &7remaining");
            }
        }
    }

    // ─── Legacy cycle-based mode ─────────────────────────────────────────

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

    private void tickDebtLegacy(Player player, PlayerData data, int onlineCount) {
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
