package com.pvptoggle.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
import com.pvptoggle.util.CommandUtil;
import com.pvptoggle.util.ConfigUtil;
import com.pvptoggle.util.MessageUtil;

// /pvpadmin wand | zone create/delete/list/info | player <name> info/reset/setdebt | reload
public class PvPAdminCommand implements TabExecutor {

    private static final String PLAYERS_ONLY = "&cOnly players can use this.";
    private static final String SUB_PLAYER = "player";
    private static final String SUB_DELETE = "delete";

    private final PvPTogglePlugin plugin;

    public PvPAdminCommand(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand"     -> handleWand(sender);
            case "zone"     -> handleZone(sender, args);
            case SUB_PLAYER -> handlePlayer(sender, args);
            case "reload"   -> handleReload(sender);
            case "simtime"  -> handleSimtime(sender, args);
            default         -> { return false; }
        }
        return true;
    }

    private void handleWand(CommandSender sender) {
        Player player = CommandUtil.requirePlayer(sender, PLAYERS_ONLY);
        if (player == null) return;

        Material wandMat = ConfigUtil.getWandMaterial(plugin.getConfig());

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

    private void handleZone(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin zone <create|delete|list|info> [name]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "create"    -> zoneCreate(sender, args);
            case SUB_DELETE  -> zoneDelete(sender, args);
            case "list"      -> zoneList(sender);
            case "info"      -> zoneInfo(sender, args);
            default -> MessageUtil.send(sender, "&cUsage: /pvpadmin zone <create|delete|list|info> [name]");
        }
    }

    private void zoneCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin zone create <name>");
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, PLAYERS_ONLY);
        if (player == null) return;
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
        for (PvPZone zone : zones) {
            MessageUtil.send(sender, "&7 • &f" + zone.getName() + " &7(" + zone.getWorldName() + ")");
        }
    }

    private void zoneInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin zone info <name>");
            return;
        }
        PvPZone zone = plugin.getZoneManager().getZone(args[2]);
        if (zone == null) {
            MessageUtil.send(sender, "&cZone '&f" + args[2] + "&c' not found.");
            return;
        }
        MessageUtil.send(sender, "&6&l══════ Zone: " + zone.getName() + " ══════");
        MessageUtil.send(sender, "&7World: &f" + zone.getWorldName());
        MessageUtil.send(sender, "&7Corner 1: &f(" + zone.getX1() + ", " + zone.getY1() + ", " + zone.getZ1() + ")");
        MessageUtil.send(sender, "&7Corner 2: &f(" + zone.getX2() + ", " + zone.getY2() + ", " + zone.getZ2() + ")");
    }

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

    // temp test command - adds fake playtime so you don't have to wait an hour
    private void handleSimtime(CommandSender sender, String[] args) {
        Player player = CommandUtil.requirePlayer(sender, PLAYERS_ONLY);
        if (player == null) return;
        if (args.length < 2) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin simtime <seconds>");
            return;
        }
        try {
            long seconds = Long.parseLong(args[1]);
            PlayerData data = plugin.getPvPManager().getPlayerData(player.getUniqueId());
            data.setTotalPlaytimeSeconds(data.getTotalPlaytimeSeconds() + seconds);
            MessageUtil.send(player, "&aAdded &f" + MessageUtil.formatTime(seconds)
                    + " &ato your playtime. Total: &f"
                    + MessageUtil.formatTime(data.getTotalPlaytimeSeconds()));
            MessageUtil.send(player, "&7(Debt will trigger on the next tick if a cycle threshold is crossed)");
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "&cInvalid number: &f" + args[1]);
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadPluginConfig();
        MessageUtil.send(sender, "&aConfiguration reloaded!");
    }

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
        MessageUtil.send(sender, "&e/pvpadmin simtime <seconds> &7— add fake playtime (testing)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1 -> completions.addAll(Arrays.asList("wand", "zone", SUB_PLAYER, "reload", "simtime"));
            case 2 -> {
                if (args[0].equalsIgnoreCase("zone")) {
                    completions.addAll(Arrays.asList("create", SUB_DELETE, "list", "info"));
                } else if (args[0].equalsIgnoreCase(SUB_PLAYER)) {
                    Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("zone")
                        && (args[1].equalsIgnoreCase(SUB_DELETE) || args[1].equalsIgnoreCase("info"))) {
                    completions.addAll(plugin.getZoneManager().getZoneNames());
                } else if (args[0].equalsIgnoreCase(SUB_PLAYER)) {
                    completions.addAll(Arrays.asList("info", "reset", "setdebt"));
                }
            }
            default -> { /* no completions */ }
        }

        String last = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(last))
                .toList();
    }
}
