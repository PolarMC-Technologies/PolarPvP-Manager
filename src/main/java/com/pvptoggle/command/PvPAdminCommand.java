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
            case "wand"      -> handleWand(sender);
            case "zone"      -> handleZone(sender, args);
            case SUB_PLAYER  -> handlePlayer(sender, args);
            case "reload"    -> handleReload(sender);
            case "simtime"   -> handleSimtime(sender, args);
            case "pvptimer"  -> handlePvpTimer(sender, args);
            default          -> { return false; }
        }
        return true;
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, PLAYERS_ONLY);
            return;
        }

        Material wandMat;
        try {
            String matConfig = plugin.getConfig().getString("zone-wand-material");
            wandMat = Material.valueOf((matConfig != null ? matConfig : "BLAZE_ROD").toUpperCase());
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
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, PLAYERS_ONLY);
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
                MessageUtil.send(sender, "&7PvP-off accumulator: &f" + MessageUtil.formatTime(data.getPvpOffAccumulator()));
                MessageUtil.send(sender, "&7Forced PvP elapsed: &f" + MessageUtil.formatTime(data.getForcedPvpElapsed()));
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
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, PLAYERS_ONLY);
            return;
        }
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

    // ─── PvP Timer admin commands ──────────────────────────────────────────

    private void handlePvpTimer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin pvptimer <status|debug|simulate|reset> [player] [value]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "status"   -> pvpTimerStatus(sender, args);
            case "debug"    -> pvpTimerDebug(sender, args);
            case "simulate" -> pvpTimerSimulate(sender, args);
            case "reset"    -> pvpTimerReset(sender, args);
            default -> MessageUtil.send(sender,
                    "&cUsage: /pvpadmin pvptimer <status|debug|simulate|reset> [player] [value]");
        }
    }

    @SuppressWarnings("deprecation")
    private void pvpTimerStatus(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 2);
        if (target == null) return;

        PlayerData data = plugin.getPvPManager().getPlayerData(target.getUniqueId());
        boolean forced = plugin.getPvPManager().isForcedPvP(target);
        boolean inZone = plugin.getZoneManager().isInForcedPvPZone(target.getLocation());

        MessageUtil.send(sender, "&6&l══════ PvP Timer: " + target.getName() + " ══════");
        MessageUtil.send(sender, "&7PvP toggle: " + (data.isPvpEnabled() ? "&aOn" : "&cOff"));
        MessageUtil.send(sender, "&7Forced PvP: " + (forced ? "&c&lYes" : "&aNo"));
        MessageUtil.send(sender, "&7In forced zone: " + (inZone ? "&cYes" : "&aNo"));
        MessageUtil.send(sender, "&7PvP debt: &f" + MessageUtil.formatTime(data.getPvpDebtSeconds()));
        MessageUtil.send(sender, "&7PvP-off accumulator: &f" + MessageUtil.formatTime(data.getPvpOffAccumulator()));
        MessageUtil.send(sender, "&7Forced PvP elapsed: &f" + MessageUtil.formatTime(data.getForcedPvpElapsed()));
        MessageUtil.send(sender, "&7Total playtime: &f" + MessageUtil.formatTime(data.getTotalPlaytimeSeconds()));
        MessageUtil.send(sender, "&7Online players: &f" + Bukkit.getOnlinePlayers().size());

        boolean timerEnabled = plugin.getConfig().getBoolean("pvp-force-timer.enabled", false);
        MessageUtil.send(sender, "&7Force timer mode: " + (timerEnabled ? "&aDebt-ratio" : "&eLegacy cycle"));
    }

    @SuppressWarnings("deprecation")
    private void pvpTimerDebug(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 2);
        if (target == null) return;

        PlayerData data = plugin.getPvPManager().getPlayerData(target.getUniqueId());
        boolean timerEnabled = plugin.getConfig().getBoolean("pvp-force-timer.enabled", false);

        MessageUtil.send(sender, "&6&l══════ Timer Debug: " + target.getName() + " ══════");
        MessageUtil.send(sender, "&7─── Config ───");
        MessageUtil.send(sender, "&7  Force timer enabled: " + (timerEnabled ? "&aYes" : "&cNo"));

        if (timerEnabled) {
            MessageUtil.send(sender, "&7  Debt ratio: &f"
                    + plugin.getConfig().getInt("pvp-force-timer.debt-ratio", 5) + " min off = 1 min debt");
            MessageUtil.send(sender, "&7  Max debt: &f"
                    + plugin.getConfig().getInt("pvp-force-timer.max-debt", 60) + " min");
            MessageUtil.send(sender, "&7  Min forced duration: &f"
                    + plugin.getConfig().getInt("pvp-force-timer.minimum-forced-duration", 20) + " min");
            MessageUtil.send(sender, "&7  Exempt manual PvP: &f"
                    + plugin.getConfig().getBoolean("pvp-force-timer.exemptions.manual-pvp", true));
            MessageUtil.send(sender, "&7  Exempt forced zones: &f"
                    + plugin.getConfig().getBoolean("pvp-force-timer.exemptions.forced-zones", true));
            MessageUtil.send(sender, "&7  Exempt solo server: &f"
                    + plugin.getConfig().getBoolean("pvp-force-timer.exemptions.solo-server", true));
        } else {
            MessageUtil.send(sender, "&7  Hours per cycle: &f"
                    + plugin.getConfig().getInt("playtime.hours-per-cycle", 1));
            MessageUtil.send(sender, "&7  Forced minutes per cycle: &f"
                    + plugin.getConfig().getInt("playtime.forced-minutes", 20));
        }

        MessageUtil.send(sender, "&7─── Player State ───");
        MessageUtil.send(sender, "&7  Total playtime: &f" + data.getTotalPlaytimeSeconds() + "s ("
                + MessageUtil.formatTime(data.getTotalPlaytimeSeconds()) + ")");
        MessageUtil.send(sender, "&7  PvP debt: &f" + data.getPvpDebtSeconds() + "s ("
                + MessageUtil.formatTime(data.getPvpDebtSeconds()) + ")");
        MessageUtil.send(sender, "&7  PvP-off accumulator: &f" + data.getPvpOffAccumulator() + "s ("
                + MessageUtil.formatTime(data.getPvpOffAccumulator()) + ")");
        MessageUtil.send(sender, "&7  Forced elapsed: &f" + data.getForcedPvpElapsed() + "s ("
                + MessageUtil.formatTime(data.getForcedPvpElapsed()) + ")");
        MessageUtil.send(sender, "&7  Processed cycles: &f" + data.getProcessedCycles());
        MessageUtil.send(sender, "&7  Manual PvP: " + (data.isPvpEnabled() ? "&aOn" : "&cOff"));
        MessageUtil.send(sender, "&7  Has bypass perm: "
                + (target.hasPermission("pvptoggle.bypass") ? "&aYes" : "&cNo"));

        MessageUtil.send(sender, "&7─── Exemptions ───");
        boolean inZone = plugin.getZoneManager().isInForcedPvPZone(target.getLocation());
        MessageUtil.send(sender, "&7  In forced zone: " + (inZone ? "&cYes" : "&aNo"));
        MessageUtil.send(sender, "&7  Solo server: "
                + (Bukkit.getOnlinePlayers().size() < 2 ? "&eYes (alone)" : "&aNo (" + Bukkit.getOnlinePlayers().size() + " online)"));
    }

    @SuppressWarnings("deprecation")
    private void pvpTimerSimulate(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 2);
        if (target == null) return;

        if (args.length < (sender instanceof Player && args.length >= 3 && isOnlinePlayer(args[2]) ? 4 : 3)) {
            MessageUtil.send(sender, "&cUsage: /pvpadmin pvptimer simulate [player] <seconds>");
            return;
        }

        String secondsArg = args[args.length - 1];
        try {
            long seconds = Long.parseLong(secondsArg);
            PlayerData data = plugin.getPvPManager().getPlayerData(target.getUniqueId());
            data.setPvpOffAccumulator(data.getPvpOffAccumulator() + seconds);
            MessageUtil.send(sender,
                    "&aAdded &f" + MessageUtil.formatTime(seconds)
                            + " &ato PvP-off accumulator for &f" + target.getName()
                            + "&a. Total: &f" + MessageUtil.formatTime(data.getPvpOffAccumulator()));
            MessageUtil.send(sender, "&7(Debt will convert on the next tick based on ratio)");
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "&cInvalid number: &f" + secondsArg);
        }
    }

    @SuppressWarnings("deprecation")
    private void pvpTimerReset(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 2);
        if (target == null) return;

        PlayerData data = plugin.getPvPManager().getPlayerData(target.getUniqueId());
        data.setPvpDebtSeconds(0);
        data.setPvpOffAccumulator(0);
        data.setForcedPvpElapsed(0);
        MessageUtil.send(sender,
                "&aAll PvP timer data for &f" + target.getName() + " &ahas been reset (debt, accumulator, forced elapsed).");
    }

    /** Resolve target player: use args[index] if present and is an online player, otherwise fall back to sender. */
    private Player resolveTarget(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            Player target = Bukkit.getPlayerExact(args[index]);
            if (target != null) return target;
            // If the argument doesn't look like a number, treat it as a player name that's not online
            if (!isNumeric(args[index])) {
                MessageUtil.send(sender, "&cPlayer '&f" + args[index] + "&c' is not online.");
                return null;
            }
            // Numeric argument — not a player name, fall through to sender
        }
        if (sender instanceof Player player) return player;
        MessageUtil.send(sender, "&cSpecify an online player name.");
        return null;
    }

    private boolean isNumeric(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOnlinePlayer(String s) {
        return Bukkit.getPlayerExact(s) != null;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
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
        MessageUtil.send(sender, "&e/pvpadmin pvptimer status [player] &7— timer status");
        MessageUtil.send(sender, "&e/pvpadmin pvptimer debug [player] &7— detailed timer debug");
        MessageUtil.send(sender, "&e/pvpadmin pvptimer simulate [player] <sec> &7— add PvP-off time");
        MessageUtil.send(sender, "&e/pvpadmin pvptimer reset [player] &7— reset timer data");
        MessageUtil.send(sender, "&e/pvpadmin reload &7— reload config");
        MessageUtil.send(sender, "&e/pvpadmin simtime <seconds> &7— add fake playtime (testing)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1 -> completions.addAll(
                    Arrays.asList("wand", "zone", SUB_PLAYER, "pvptimer", "reload", "simtime"));
            case 2 -> {
                if (args[0].equalsIgnoreCase("zone")) {
                    completions.addAll(Arrays.asList("create", SUB_DELETE, "list", "info"));
                } else if (args[0].equalsIgnoreCase(SUB_PLAYER)) {
                    Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
                } else if (args[0].equalsIgnoreCase("pvptimer")) {
                    completions.addAll(Arrays.asList("status", "debug", "simulate", "reset"));
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("zone")
                        && (args[1].equalsIgnoreCase(SUB_DELETE) || args[1].equalsIgnoreCase("info"))) {
                    completions.addAll(plugin.getZoneManager().getZoneNames());
                } else if (args[0].equalsIgnoreCase(SUB_PLAYER)) {
                    completions.addAll(Arrays.asList("info", "reset", "setdebt"));
                } else if (args[0].equalsIgnoreCase("pvptimer")) {
                    Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
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
