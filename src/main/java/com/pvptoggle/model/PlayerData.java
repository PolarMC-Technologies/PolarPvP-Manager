package com.pvptoggle.model;

// Per-player state that gets saved to playerdata.yml
public class PlayerData {

    private boolean pvpEnabled;
    private long totalPlaytimeSeconds;
    private int processedCycles;   // hour milestones already converted to debt
    private long pvpDebtSeconds;

    public PlayerData() {
        this.pvpEnabled = false;
        this.totalPlaytimeSeconds = 0;
        this.processedCycles = 0;
        this.pvpDebtSeconds = 0;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public long getTotalPlaytimeSeconds() {
        return totalPlaytimeSeconds;
    }

    public void setTotalPlaytimeSeconds(long totalPlaytimeSeconds) {
        this.totalPlaytimeSeconds = totalPlaytimeSeconds;
    }

    public int getProcessedCycles() {
        return processedCycles;
    }

    public void setProcessedCycles(int processedCycles) {
        this.processedCycles = processedCycles;
    }

    public long getPvpDebtSeconds() {
        return pvpDebtSeconds;
    }

    public void setPvpDebtSeconds(long pvpDebtSeconds) {
        this.pvpDebtSeconds = Math.max(0, pvpDebtSeconds);
    }
}
