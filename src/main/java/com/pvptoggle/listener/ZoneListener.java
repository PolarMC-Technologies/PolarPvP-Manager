package com.pvptoggle.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.util.ConfigUtil;
import com.pvptoggle.util.MessageUtil;

public class ZoneListener implements Listener {

    private final PvPTogglePlugin plugin;
    private final Map<UUID, Long> chatExitCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> actionbarExitCooldowns = new ConcurrentHashMap<>();
    private Material wandMaterial; // Cached wand material
    private long chatCooldownMillis;
    private long actionbarCooldownMillis;

    public ZoneListener(PvPTogglePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Load and cache config values (called on plugin enable and reload)
     */
    public void loadConfig() {
        this.wandMaterial = ConfigUtil.getWandMaterial(plugin.getConfig());
        int chatCooldownSeconds = plugin.getConfig().getInt("zone-exit-cooldowns.chat", 3);
        int actionbarCooldownSeconds = plugin.getConfig().getInt("zone-exit-cooldowns.actionbar", 0);

        if (chatCooldownSeconds < 0) {
            plugin.getLogger().warning("[PvPToggle] Invalid negative value for 'zone-exit-cooldowns.chat' (" 
                    + chatCooldownSeconds + "); using 0 instead.");
            chatCooldownSeconds = 0;
        }

        if (actionbarCooldownSeconds < 0) {
            plugin.getLogger().warning("[PvPToggle] Invalid negative value for 'zone-exit-cooldowns.actionbar' (" 
                    + actionbarCooldownSeconds + "); using 0 instead.");
            actionbarCooldownSeconds = 0;
        }
        this.chatCooldownMillis = chatCooldownSeconds * 1000L;
        this.actionbarCooldownMillis = actionbarCooldownSeconds * 1000L;
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
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            
            if (isCooldownReady(chatExitCooldowns, playerId, chatCooldownMillis, currentTime)) {
                MessageUtil.send(player, "&a&l✓ You left the forced PvP zone.");
            }
            
            if (isCooldownReady(actionbarExitCooldowns, playerId, actionbarCooldownMillis, currentTime)) {
                MessageUtil.sendActionBar(player, "&a&l✓ You left the forced PvP zone.");
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up cooldown data when player leaves to prevent memory leak
        UUID playerId = event.getPlayer().getUniqueId();
        chatExitCooldowns.remove(playerId);
        actionbarExitCooldowns.remove(playerId);
    }

    private boolean isCooldownReady(Map<UUID, Long> cooldownMap, UUID playerId, long cooldownMillis, long currentTime) {
        if (cooldownMillis == 0) return true; // Early return when cooldown is disabled
        Long lastTime = cooldownMap.get(playerId);
        if (lastTime == null || (currentTime - lastTime) >= cooldownMillis) {
            cooldownMap.put(playerId, currentTime);
            return true;
        }
        return false;
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
