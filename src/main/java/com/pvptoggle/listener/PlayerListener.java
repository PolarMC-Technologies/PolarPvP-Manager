package com.pvptoggle.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.util.MessageUtil;

public class PlayerListener implements Listener {

    private final PvPTogglePlugin plugin;

    public PlayerListener(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData data = plugin.getPvPManager().getPlayerData(event.getPlayer().getUniqueId());

        // Enforce PvP cap immediately on join
        if (!event.getPlayer().hasPermission("pvptoggle.bypass")) {
            com.pvptoggle.manager.PlaytimeManager.enforceDebtCap(plugin, event.getPlayer(), data);
            // If they still have debt (not forced), remind them after login
            if (data.getPvpDebtSeconds() > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (event.getPlayer().isOnline()) {
                        MessageUtil.send(event.getPlayer(),
                                "&c&l⚔ You have forced PvP time remaining: &f"
                                        + MessageUtil.formatTime(data.getPvpDebtSeconds()));
                    }
                }, 40L); // 2 seconds after join
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Persist immediately so the player can't dodge debt by leaving
        plugin.getPvPManager().saveData();
    }
}
