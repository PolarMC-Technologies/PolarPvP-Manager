package org.PolarNL.PolarPvP.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.PolarNL.PolarPvP.PvPTogglePlugin;
import org.PolarNL.PolarPvP.model.PlayerData;
import org.PolarNL.PolarPvP.util.MessageUtil;

public class PlayerListener implements Listener {

    private final PvPTogglePlugin plugin;

    public PlayerListener(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData data = plugin.getPvPManager().getPlayerData(event.getPlayer().getUniqueId());

        // if they have debt, remind them after login
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
        // persist immediately so the player can't dodge debt by leaving
        // use async to avoid blocking the main thread during logout
        // note: if the server shuts down right after quit, this may not complete.
        // onDisable() still performs a sync save for shutdown cases.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getPvPManager().saveData();
        });
    }
}
