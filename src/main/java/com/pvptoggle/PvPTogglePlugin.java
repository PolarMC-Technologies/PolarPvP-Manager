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
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ZoneListener(this), this);

        // commands

        PvPCommand pvpCmd = new PvPCommand(this);
        Objects.requireNonNull(getCommand("plpvp")).setExecutor(pvpCmd);
        Objects.requireNonNull(getCommand("plpvp")).setTabCompleter(pvpCmd);

        PvPAdminCommand adminCmd = new PvPAdminCommand(this);
        Objects.requireNonNull(getCommand("plpvpadmin")).setExecutor(adminCmd);
        Objects.requireNonNull(getCommand("plpvpadmin")).setTabCompleter(adminCmd);

        playtimeManager.startTracking();

        UpdateChecker updateChecker = new UpdateChecker(this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        updateChecker.check();

        getLogger().log(Level.INFO, "PolarPvP-Manager v{0} enabled!", getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (playtimeManager != null) playtimeManager.stopTracking();

        if (pvpManager != null)  pvpManager.saveData();
        if (zoneManager != null) zoneManager.saveZones();

        getLogger().info("PvPToggle disabled, data saved.");
    }

    public PvPManager      getPvPManager()      { return pvpManager; }
    public ZoneManager     getZoneManager()     { return zoneManager; }
    public PlaytimeManager getPlaytimeManager() { return playtimeManager; }
}
