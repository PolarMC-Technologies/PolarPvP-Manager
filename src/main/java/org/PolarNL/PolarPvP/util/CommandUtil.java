package org.PolarNL.PolarPvP.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandUtil {

    private CommandUtil() {}

    public static Player requirePlayer(CommandSender sender, String errorMessage) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, errorMessage);
            return null;
        }
        return player;
    }
}
