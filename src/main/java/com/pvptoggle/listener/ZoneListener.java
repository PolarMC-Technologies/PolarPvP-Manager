package com.pvptoggle.listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.util.MessageUtil;

public class ZoneListener implements Listener {

    private final PvPTogglePlugin plugin;
    private Material wandMaterial; // Cached wand material

    public ZoneListener(PvPTogglePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Load and cache config values (called on plugin enable and reload)
     */
    public void loadConfig() {
        try {
            String matName = plugin.getConfig().getString("zone-wand-material");
            if (matName == null) matName = "BLAZE_ROD";
            this.wandMaterial = Material.valueOf(matName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Bad material in config — fall back to BLAZE_ROD
            this.wandMaterial = Material.BLAZE_ROD;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("pvptoggle.admin")) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isZoneWand(item)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getZoneManager().setPosition(player.getUniqueId(), 0, block.getLocation());
            MessageUtil.send(player,
                    "&ePosition 1 &7set to &f("
                            + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")");
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getZoneManager().setPosition(player.getUniqueId(), 1, block.getLocation());
            MessageUtil.send(player,
                    "&ePosition 2 &7set to &f("
                            + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) return;

        Location from = event.getFrom();

        // Only check when the player crosses a block boundary
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        boolean wasInZone = plugin.getZoneManager().isInForcedPvPZone(from);
        boolean isInZone  = plugin.getZoneManager().isInForcedPvPZone(to);

        if (!wasInZone && isInZone) {
            MessageUtil.send(event.getPlayer(), "&c&l⚔ You entered a forced PvP zone!");
            MessageUtil.sendActionBar(event.getPlayer(), "&c&l⚔ FORCED PVP ZONE ⚔");
        } else if (wasInZone && !isInZone) {
            MessageUtil.send(event.getPlayer(), "&a&l✓ You left the forced PvP zone.");
        }
    }

    private boolean isZoneWand(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (item.getType() != wandMaterial) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        return meta.getDisplayName().equals(ChatColor.YELLOW + "PvP Zone Selector");
    }
}
