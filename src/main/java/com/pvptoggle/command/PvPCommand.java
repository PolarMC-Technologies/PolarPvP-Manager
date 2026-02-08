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
        PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
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
                                "&c&lYou are in a forced PvP zone! PvP cannot be disabled here."));
            } else {
                PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
                String template = Objects.requireNonNullElse(
                        plugin.getConfig().getString("messages.pvp-forced-playtime"),
                        "&c&lPvP is forced due to playtime! &f%time% &cremaining.");
                String msg = template.replace("%time%", MessageUtil.formatTime(data.getPvpDebtSeconds()));
                MessageUtil.send(player, msg);
            }
            return;
        }

        PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
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
            }
        }

        MessageUtil.send(player, "&7Total playtime: &f" + MessageUtil.formatTime(data.getTotalPlaytimeSeconds()));
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
