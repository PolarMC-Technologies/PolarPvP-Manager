package com.pvptoggle.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.util.MessageUtil;

/**
 * Handles player join / quit:
 * <ul>
 *   <li>Ensures a {@link PlayerData} record exists on join.</li>
 *   <li>Notifies the player if they still have forced-PvP debt.</li>
 *   <li>Persists data on quit so nothing is lost if the server crashes.</li>
 * </ul>
 */
public class PlayerListener implements Listener {

    private final PvPTogglePlugin plugin;

    public PlayerListener(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Make sure a record exists (lazy-creates with defaults if first time)
        PlayerData data = plugin.getPvPManager().getPlayerData(event.getPlayer().getUniqueId());

        // If they have outstanding debt, remind them a couple seconds after login
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
        plugin.getPvPManager().saveData();
    }
}
