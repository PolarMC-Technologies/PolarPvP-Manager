package com.pvptoggle.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.pvptoggle.PvPTogglePlugin;
import com.pvptoggle.model.PlayerData;
import com.pvptoggle.model.PvPZone;
import com.pvptoggle.util.MessageUtil;

/**
 * <pre>
 * /pvpadmin wand                          — get the zone-selection wand
 * /pvpadmin zone create &lt;name&gt;           — create zone from selection
 * /pvpadmin zone delete &lt;name&gt;           — delete a zone
 * /pvpadmin zone list                     — list all zones
 * /pvpadmin zone info   &lt;name&gt;           — show zone details
 * /pvpadmin player &lt;name&gt; info           — show player data
 * /pvpadmin player &lt;name&gt; reset          — reset player data
 * /pvpadmin player &lt;name&gt; setdebt &lt;sec&gt; — set PvP debt
 * /pvpadmin reload                        — reload config
 * </pre>
 */
public class PvPAdminCommand implements TabExecutor {

    private final PvPTogglePlugin plugin;

    public PvPAdminCommand(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    /* ================================================================
     *  Command dispatch
     * ================================================================ */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand"   -> handleWand(sender);
            case "zone"   -> handleZone(sender, args);
            case "player" -> handlePlayer(sender, args);
            case "reload" -> handleReload(sender);
            default       -> sendHelp(sender);
        }
        return true;
    }

    /* ================================================================
     *  /pvpadmin wand
     * ================================================================ */

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return;
        }

        Material wandMat;
        try {
            wandMat = Material.valueOf(
                    plugin.getConfig().getString("zone-wand-material", "BLAZE_ROD").toUpperCase());
        } catch (IllegalArgumentException e) {
            wandMat = Material.BLAZE_ROD;
        }

        ItemStack wand = new ItemStack(wandMat);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "PvP Zone Selector");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Left click: Set position 1",
                    ChatColor.GRAY + "Right click: Set position 2"
            ));
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        MessageUtil.send(player, "&eYou received the &6PvP Zone Selector &ewand!");
    }

    /* ================================================================
     *  /pvpadmin zone ...
     * ================================================================ */

    private void handleZone(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin zone <create|delete|list|info> [name]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "create" -> zoneCreate(sender, args);
            case "delete" -> zoneDelete(sender, args);
            case "list"   -> zoneList(sender);
            case "info"   -> zoneInfo(sender, args);
            default -> MessageUtil.send(sender, "&cUsage: /pvpadmin zone <create|delete|list|info> [name]");
        }
    }

    private void zoneCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin zone create <name>");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return;
        }
        String name = args[2];
        if (plugin.getZoneManager().getZone(name) != null) {
            MessageUtil.send(player, "&cA zone named '&f" + name + "&c' already exists.");
            return;
        }
        if (plugin.getZoneManager().createZone(name, player.getUniqueId())) {
            MessageUtil.send(player, "&aZone '&f" + name + "&a' created successfully!");
        } else {
            MessageUtil.send(player,
                    "&cFailed to create zone. Make sure you selected both positions in the same world.");
        }
    }

    private void zoneDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin zone delete <name>");
            return;
        }
        if (plugin.getZoneManager().deleteZone(args[2])) {
            MessageUtil.send(sender, "&aZone '&f" + args[2] + "&a' deleted.");
        } else {
            MessageUtil.send(sender, "&cZone '&f" + args[2] + "&c' not found.");
        }
    }

    private void zoneList(CommandSender sender) {
        Collection<PvPZone> zones = plugin.getZoneManager().getZones();
        if (zones.isEmpty()) {
            MessageUtil.send(sender, "&7No PvP zones defined.");
            return;
        }
        MessageUtil.send(sender, "&6&l══════ PvP Zones ══════");
        for (PvPZone z : zones) {
            MessageUtil.send(sender, "&7 • &f" + z.getName() + " &7(" + z.getWorldName() + ")");
        }
    }

    private void zoneInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin zone info <name>");
            return;
        }
        PvPZone z = plugin.getZoneManager().getZone(args[2]);
        if (z == null) {
            MessageUtil.send(sender, "&cZone '&f" + args[2] + "&c' not found.");
            return;
        }
        MessageUtil.send(sender, "&6&l══════ Zone: " + z.getName() + " ══════");
        MessageUtil.send(sender, "&7World: &f" + z.getWorldName());
        MessageUtil.send(sender, "&7Corner 1: &f(" + z.getX1() + ", " + z.getY1() + ", " + z.getZ1() + ")");
        MessageUtil.send(sender, "&7Corner 2: &f(" + z.getX2() + ", " + z.getY2() + ", " + z.getZ2() + ")");
    }

    /* ================================================================
     *  /pvpadmin player ...
     * ================================================================ */

    @SuppressWarnings("deprecation")
    private void handlePlayer(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin player <name> <info|reset|setdebt>");
            return;
        }

        String playerName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            MessageUtil.send(sender, "&cPlayer '&f" + playerName + "&c' has never joined this server.");
            return;
        }

        UUID uuid = target.getUniqueId();
        PlayerData data = plugin.getPvPManager().getPlayerData(uuid);

        switch (args[2].toLowerCase()) {
            case "info" -> {
                MessageUtil.send(sender, "&6&l══════ Player: " + playerName + " ══════");
                MessageUtil.send(sender, "&7PvP toggle: " + (data.isPvpEnabled() ? "&aOn" : "&cOff"));
                MessageUtil.send(sender, "&7Total playtime: &f" + MessageUtil.formatTime(data.getTotalPlaytimeSeconds()));
                MessageUtil.send(sender, "&7Processed cycles: &f" + data.getProcessedCycles());
                MessageUtil.send(sender, "&7PvP debt: &f" + MessageUtil.formatTime(data.getPvpDebtSeconds()));
            }
            case "reset" -> {
                plugin.getPvPManager().resetPlayerData(uuid);
                MessageUtil.send(sender, "&aAll data for '&f" + playerName + "&a' has been reset.");
            }
            case "setdebt" -> {
                if (args.length < 4) {
                    MessageUtil.send(sender, "&cUsage: /pvpadmin player <name> setdebt <seconds>");
                    return;
                }
                try {
                    long seconds = Long.parseLong(args[3]);
                    data.setPvpDebtSeconds(seconds);
                    MessageUtil.send(sender,
                            "&aPvP debt for '&f" + playerName + "&a' set to &f"
                                    + MessageUtil.formatTime(data.getPvpDebtSeconds()));
                } catch (NumberFormatException e) {
                    MessageUtil.send(sender, "&cInvalid number: &f" + args[3]);
                }
            }
            default -> MessageUtil.send(sender, "&cUsage: /pvpadmin player <name> <info|reset|setdebt>");
        }
    }

    /* ================================================================
     *  /pvpadmin reload
     * ================================================================ */

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        MessageUtil.send(sender, "&aConfiguration reloaded!");
    }

    /* ================================================================
     *  Help
     * ================================================================ */

    private void sendHelp(CommandSender sender) {
        MessageUtil.send(sender, "&6&l══════ PvPToggle Admin ══════");
        MessageUtil.send(sender, "&e/pvpadmin wand &7— get zone selection wand");
        MessageUtil.send(sender, "&e/pvpadmin zone create <name> &7— create a zone");
        MessageUtil.send(sender, "&e/pvpadmin zone delete <name> &7— delete a zone");
        MessageUtil.send(sender, "&e/pvpadmin zone list &7— list all zones");
        MessageUtil.send(sender, "&e/pvpadmin zone info <name> &7— zone details");
        MessageUtil.send(sender, "&e/pvpadmin player <name> info &7— player info");
        MessageUtil.send(sender, "&e/pvpadmin player <name> reset &7— reset player data");
        MessageUtil.send(sender, "&e/pvpadmin player <name> setdebt <sec> &7— set PvP debt");
        MessageUtil.send(sender, "&e/pvpadmin reload &7— reload config");
    }

    /* ================================================================
     *  Tab completion
     * ================================================================ */

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("wand", "zone", "player", "reload"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "zone"   -> completions.addAll(Arrays.asList("create", "delete", "list", "info"));
                case "player" -> Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("zone")
                    && (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("info"))) {
                completions.addAll(plugin.getZoneManager().getZoneNames());
            } else if (args[0].equalsIgnoreCase("player")) {
                completions.addAll(Arrays.asList("info", "reset", "setdebt"));
            }
        }

        String last = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(last))
                .collect(Collectors.toList());
    }
}
