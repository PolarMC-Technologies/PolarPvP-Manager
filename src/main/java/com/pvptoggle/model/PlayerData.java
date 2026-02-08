package com.pvptoggle.model;

/**
 * Holds all persistent per-player data: PvP toggle, playtime, and forced-PvP debt.
 */
public class PlayerData {

    private boolean pvpEnabled;
    private long totalPlaytimeSeconds;
    private int processedCycles;   // how many hour-milestones have already been turned into debt
    private long pvpDebtSeconds;   // remaining forced-PvP time in seconds

    public PlayerData() {
        this.pvpEnabled = false;
        this.totalPlaytimeSeconds = 0;
        this.processedCycles = 0;
        this.pvpDebtSeconds = 0;
    }

    /* ---- PvP toggle ---- */

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    /* ---- Playtime ---- */

    public long getTotalPlaytimeSeconds() {
        return totalPlaytimeSeconds;
    }

    public void setTotalPlaytimeSeconds(long totalPlaytimeSeconds) {
        this.totalPlaytimeSeconds = totalPlaytimeSeconds;
    }

    /* ---- Processed cycles (hours that already generated debt) ---- */

    public int getProcessedCycles() {
        return processedCycles;
    }

    public void setProcessedCycles(int processedCycles) {
        this.processedCycles = processedCycles;
    }

    /* ---- PvP debt ---- */

    public long getPvpDebtSeconds() {
        return pvpDebtSeconds;
    }

    public void setPvpDebtSeconds(long pvpDebtSeconds) {
        this.pvpDebtSeconds = Math.max(0, pvpDebtSeconds);
    }
}
