package com.pvptoggle.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.util.MessageUtil;

public class CombatListener implements Listener {

    private final PvPTogglePlugin plugin;
    private boolean debugEnabled; // Cached debug flag

    public CombatListener(PvPTogglePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Load and cache config values (called on plugin enable and reload)
     */
    public void loadConfig() {
        this.debugEnabled = plugin.getConfig().getBoolean("debug", false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolvePlayerAttacker(event.getDamager());
        if (attacker == null) return;
        if (attacker.equals(victim)) return;

        if (debugEnabled) {
            plugin.getLogger().log(java.util.logging.Level.INFO, "[DEBUG] Combat: {0} -> {1} | damager type: {2}",
                    new Object[]{attacker.getName(), victim.getName(), event.getDamager().getType()});
        }

        boolean attackerPvP = plugin.getPvPManager().isEffectivePvPEnabled(attacker);
        boolean victimPvP   = plugin.getPvPManager().isEffectivePvPEnabled(victim);

        if (debugEnabled) {
            plugin.getLogger().log(java.util.logging.Level.INFO, "[DEBUG] Result: attackerPvP={0}, victimPvP={1}",
                    new Object[]{attackerPvP, victimPvP});
        }

        if (!attackerPvP) {
            MessageUtil.send(attacker,
                    plugin.getConfig().getString("messages.pvp-blocked-attacker",
                            "&c&l\u2718 &cYour PvP is off! &7Use &a/pvp on &7to fight."));
            event.setCancelled(true);
            return;
        }

        if (!victimPvP) {
            MessageUtil.send(attacker,
                    plugin.getConfig().getString("messages.pvp-blocked-victim",
                            "&c&l\u2718 &cThat player has PvP disabled!"));
            event.setCancelled(true);
        }
    }

    private Player resolvePlayerAttacker(Entity damager) {
        if (damager instanceof Player p) {
            return p;
        }
        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        if (damager instanceof Tameable tameable
                && tameable.isTamed()
                && tameable.getOwner() instanceof Player owner) {
            return owner;
        }
        return null;
    }
}
