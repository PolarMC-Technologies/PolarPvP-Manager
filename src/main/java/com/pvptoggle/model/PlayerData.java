package com.pvptoggle.model;

public class PlayerData {

    private boolean pvpEnabled;
    private long totalPlaytimeSeconds;
    private int processedCycles;   // how many cycles converted to debt (legacy mode)
    private long pvpDebtSeconds;
    private long pvpOffAccumulator; // seconds spent with PvP off (debt-ratio mode)
    private long forcedPvpElapsed;  // seconds elapsed in current forced-PvP block

    public PlayerData() {
        this.pvpEnabled = false;
        this.totalPlaytimeSeconds = 0;
        this.processedCycles = 0;
        this.pvpDebtSeconds = 0;
        this.pvpOffAccumulator = 0;
        this.forcedPvpElapsed = 0;
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

    public long getPvpOffAccumulator() {
        return pvpOffAccumulator;
    }

    public void setPvpOffAccumulator(long pvpOffAccumulator) {
        this.pvpOffAccumulator = Math.max(0, pvpOffAccumulator);
    }

    public long getForcedPvpElapsed() {
        return forcedPvpElapsed;
    }

    public void setForcedPvpElapsed(long forcedPvpElapsed) {
        this.forcedPvpElapsed = Math.max(0, forcedPvpElapsed);
    }
}
