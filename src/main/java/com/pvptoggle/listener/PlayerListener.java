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

        // If they have debt, remind them after login
        if (data.getPvpDebtSeconds() > 0 && !event.getPlayer().hasPermission("pvptoggle.bypass")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (event.getPlayer().isOnline()) {
                    MessageUtil.send(event.getPlayer(),
                            "&c&l⚔ You have forced PvP time remaining: &f"
                                    + MessageUtil.formatTime(data.getPvpDebtSeconds()));
                }
            }, 40L); // 2 seconds after join
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Persist immediately so the player can't dodge debt by leaving
        // Uses async to prevent blocking the main thread during logout
        // Note: If server shuts down immediately after quit, this may not complete.
        // However, onDisable() performs a synchronous save to handle shutdown case.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getPvPManager().saveData();
        });
    }
}
