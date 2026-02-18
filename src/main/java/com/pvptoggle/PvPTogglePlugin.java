package com.pvptoggle;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.pvptoggle.command.PvPAdminCommand;
import com.pvptoggle.command.PvPCommand;
import com.pvptoggle.listener.CombatListener;
import com.pvptoggle.listener.PlayerListener;
import com.pvptoggle.listener.ZoneListener;
import com.pvptoggle.manager.PlaytimeManager;
import com.pvptoggle.manager.PvPManager;
import com.pvptoggle.manager.ZoneManager;
import com.pvptoggle.util.UpdateChecker;

public class PvPTogglePlugin extends JavaPlugin {

    private PvPManager pvpManager;
    private ZoneManager zoneManager;
    private PlaytimeManager playtimeManager;
    
    // Store listener references for config reloading
    private CombatListener combatListener;
    private ZoneListener zoneListener;

    @Override
    public void onEnable() {
        String version = getDescription().getVersion();
        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage("§b ____       _            ____       ____");
        getServer().getConsoleSender().sendMessage("§b|  _ \\ ___ | | __ _ _ __|  _ \\__   _|  _ \\");
        getServer().getConsoleSender().sendMessage("§b| |_) / _ \\| |/ _` | '__| |_) \\ \\ / / |_) |");
        getServer().getConsoleSender().sendMessage("§b|  __/ (_) | | (_| | |  |  __/ \\ V /|  __/");
        getServer().getConsoleSender().sendMessage("§b|_|   \\___/|_|\\__,_|_|  |_|     \\_/ |_|");
        getServer().getConsoleSender().sendMessage("§7  PolarPvP-Manager §fv" + version + " §7| §aBukkit/Spigot/Paper/Purpur");
        getServer().getConsoleSender().sendMessage("");

        saveDefaultConfig();

        pvpManager      = new PvPManager(this);
        zoneManager     = new ZoneManager(this);
        playtimeManager = new PlaytimeManager(this);
        pvpManager.loadData();
        zoneManager.loadZones();

        // listeners
        combatListener = new CombatListener(this);
        zoneListener = new ZoneListener(this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(zoneListener, this);

        // commands
        PvPCommand pvpCmd = new PvPCommand(this);
        Objects.requireNonNull(getCommand("pvp")).setExecutor(pvpCmd);
        Objects.requireNonNull(getCommand("pvp")).setTabCompleter(pvpCmd);

        PvPAdminCommand adminCmd = new PvPAdminCommand(this);
        Objects.requireNonNull(getCommand("pvpadmin")).setExecutor(adminCmd);
        Objects.requireNonNull(getCommand("pvpadmin")).setTabCompleter(adminCmd);

        playtimeManager.startTracking();

        UpdateChecker updateChecker = new UpdateChecker(this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        updateChecker.check();

        getLogger().log(Level.INFO, "PolarPvP-Manager v{0} enabled!", getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (playtimeManager != null) playtimeManager.stopTracking();

        // Synchronous saves on shutdown to ensure data is persisted
        // Async saves during normal operation, but shutdown requires completion
        if (pvpManager != null)  pvpManager.saveData();
        if (zoneManager != null) zoneManager.saveZones();

        getLogger().info("PvPToggle disabled, data saved.");
    }
    
    /**
     * Reload config and update all cached values
     */
    public void reloadPluginConfig() {
        reloadConfig();
        
        // Reload cached config values in managers and listeners
        if (playtimeManager != null) playtimeManager.loadConfigValues();
        if (zoneManager != null) zoneManager.loadZones();
        if (combatListener != null) combatListener.loadConfig();
        if (zoneListener != null) zoneListener.loadConfig();
    }

    public PvPManager      getPvPManager()      { return pvpManager; }
    public ZoneManager     getZoneManager()     { return zoneManager; }
    public PlaytimeManager getPlaytimeManager() { return playtimeManager; }
}
