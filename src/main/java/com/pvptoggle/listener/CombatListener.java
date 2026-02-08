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

/**
 * Cancels player-vs-player damage when either side does not have PvP enabled.
 * Traces projectiles (arrows, tridents, potions, etc.) and tamed animals back
 * to their owner for the PvP check.
 */
public class CombatListener implements Listener {

    private final PvPTogglePlugin plugin;

    public CombatListener(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolvePlayerAttacker(event.getDamager());
        if (attacker == null) return;           // not player-caused damage
        if (attacker.equals(victim)) return;    // self-damage (ender pearls, etc.)

        boolean attackerPvP = plugin.getPvPManager().isEffectivePvPEnabled(attacker);
        boolean victimPvP   = plugin.getPvPManager().isEffectivePvPEnabled(victim);

        if (!attackerPvP) {
            MessageUtil.send(attacker,
                    plugin.getConfig().getString("messages.pvp-blocked-attacker",
                            "&cYou have PvP disabled! Use /pvp on to enable it."));
            event.setCancelled(true);
            return;
        }

        if (!victimPvP) {
            MessageUtil.send(attacker,
                    plugin.getConfig().getString("messages.pvp-blocked-victim",
                            "&cThat player has PvP disabled!"));
            event.setCancelled(true);
        }
    }

    /**
     * Resolve the root player behind a damage source.
     * Handles direct hits, projectiles, and tamed-animal attacks.
     */
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
