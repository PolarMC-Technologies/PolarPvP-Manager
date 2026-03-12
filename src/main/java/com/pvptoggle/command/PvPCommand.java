package com.pvptoggle.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.util.MessageUtil;

// /pvp on|off|status
public class PvPCommand implements TabExecutor {

    private final PvPTogglePlugin plugin;

    public PvPCommand(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "&cThis command can only be used by players.");
            return false;
        }

        if (args.length == 0) {
            showStatus(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on"     -> toggleOn(player);
            case "off"    -> toggleOff(player);
            case "status" -> showStatus(player);
            default       -> { return false; }
        }
        return true;
    }

    private void toggleOn(Player player) {
        if (plugin.getPvPManager().isForcedPvP(player)) {
            MessageUtil.send(player, plugin.getConfig().getString("messages.pvp-already-forced",
                    "&7PvP is already &cforced on &7for you right now."));
            return;
        }
        PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
        if (data.isPvpEnabled()) {
            MessageUtil.send(player, plugin.getConfig().getString("messages.pvp-already-on",
                    "&7Your PvP is already &aenabled&7."));
            return;
        }
        data.setPvpEnabled(true);
        MessageUtil.send(player,
                plugin.getConfig().getString("messages.pvp-enabled", "&a&l⚔ PvP has been enabled!"));
    }

    private void toggleOff(Player player) {
        // Prevent toggling off while forced
        if (plugin.getPvPManager().isForcedPvP(player)) {
            if (plugin.getZoneManager().isInForcedPvPZone(player.getLocation())) {
                MessageUtil.send(player,
                        plugin.getConfig().getString("messages.pvp-forced-zone",
                                "&4&l\u26a0 &cYou're in a &4forced PvP zone&c! You can't disable PvP here."));
            } else {
                PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
                String template = Objects.requireNonNullElse(
                        plugin.getConfig().getString("messages.pvp-forced-playtime"),
                        "&4&l\u26a0 &cForced PvP active! &f%time% &cremaining.");
                String msg = template.replace("%time%", MessageUtil.formatTime(data.getPvpDebtSeconds()));
                MessageUtil.send(player, msg);
            }
            return;
        }

        PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
        if (!data.isPvpEnabled()) {
            MessageUtil.send(player, plugin.getConfig().getString("messages.pvp-already-off",
                    "&7Your PvP is already &cdisabled&7."));
            return;
        }
        data.setPvpEnabled(false);
        MessageUtil.send(player,
                plugin.getConfig().getString("messages.pvp-disabled", "&c⚔ PvP has been disabled."));
    }

    private void showStatus(Player player) {
        PlayerData data    = plugin.getPvPManager().getPlayerData(player.getUniqueId());
        boolean effective  = plugin.getPvPManager().isEffectivePvPEnabled(player);
        boolean forced     = plugin.getPvPManager().isForcedPvP(player);

        MessageUtil.send(player, "&6&l══════ PvP Status ══════");
        MessageUtil.send(player, "&7PvP: " + (effective ? "&a✓ Enabled" : "&c✗ Disabled"));
        MessageUtil.send(player, "&7Manual toggle: " + (data.isPvpEnabled() ? "&aOn" : "&cOff"));

        if (forced) {
            MessageUtil.send(player, "&7Forced: &c&lYes");
            if (plugin.getZoneManager().isInForcedPvPZone(player.getLocation())) {
                MessageUtil.send(player, "&7  Reason: &eForced PvP Zone");
            }
            if (data.getPvpDebtSeconds() > 0) {
                MessageUtil.send(player, "&7  Playtime debt: &f" + MessageUtil.formatTime(data.getPvpDebtSeconds()));
                if (data.getForcedPvpElapsed() > 0) {
                    MessageUtil.send(player, "&7  Forced time served: &f" + MessageUtil.formatTime(data.getForcedPvpElapsed()));
                }
            }
        } else if (data.getPvpDebtSeconds() > 0) {
            // Has debt but solo exemption is pausing enforcement
            MessageUtil.send(player, "&7Forced: &e(paused — solo server)");
            MessageUtil.send(player, "&7  Pending debt: &f" + MessageUtil.formatTime(data.getPvpDebtSeconds()));
        }

        MessageUtil.send(player, "&7Total playtime: &f" + MessageUtil.formatTime(data.getTotalPlaytimeSeconds()));

        if (plugin.getConfig().getBoolean("pvp-force-timer.enabled", false)
                && data.getPvpOffAccumulator() > 0) {
            int ratio = plugin.getConfig().getInt("pvp-force-timer.debt-ratio", 5);
            MessageUtil.send(player, "&7PvP-off time: &f" + MessageUtil.formatTime(data.getPvpOffAccumulator())
                    + " &7/ &f" + ratio + "m &7until debt");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("on", "off", "status")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
