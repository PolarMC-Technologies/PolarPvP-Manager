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

public class PvPTogglePlugin extends JavaPlugin {

    private PvPManager pvpManager;
    private ZoneManager zoneManager;
    private PlaytimeManager playtimeManager;

    @Override
    public void onEnable() {
        // Config
        saveDefaultConfig();

        // Managers
        pvpManager      = new PvPManager(this);
        zoneManager     = new ZoneManager(this);
        playtimeManager = new PlaytimeManager(this);

        // Load persisted data
        pvpManager.loadData();
        zoneManager.loadZones();

        // Listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ZoneListener(this), this);

        // Commands
        PvPCommand pvpCmd = new PvPCommand(this);
        Objects.requireNonNull(getCommand("pvp")).setExecutor(pvpCmd);
        Objects.requireNonNull(getCommand("pvp")).setTabCompleter(pvpCmd);

        PvPAdminCommand adminCmd = new PvPAdminCommand(this);
        Objects.requireNonNull(getCommand("pvpadmin")).setExecutor(adminCmd);
        Objects.requireNonNull(getCommand("pvpadmin")).setTabCompleter(adminCmd);

        // Start the 1-second playtime / debt ticker + auto-save
        playtimeManager.startTracking();

        getLogger().log(Level.INFO, "PolarPvP-Manager v{0} enabled!", getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Stop scheduled tasks first
        if (playtimeManager != null) playtimeManager.stopTracking();

        // Persist everything
        if (pvpManager != null)  pvpManager.saveData();
        if (zoneManager != null) zoneManager.saveZones();

        getLogger().info("PvPToggle disabled, data saved.");
    }

    public PvPManager      getPvPManager()      { return pvpManager; }
    public ZoneManager     getZoneManager()     { return zoneManager; }
    public PlaytimeManager getPlaytimeManager() { return playtimeManager; }
}
