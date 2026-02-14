package com.pvptoggle.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandUtil {

    private CommandUtil() {}

    /**
     * Checks if the CommandSender is a Player and sends an error message if not.
     * @param sender The CommandSender to check
     * @param errorMessage The error message to send if sender is not a player
     * @return The Player if sender is a player, null otherwise
     */
    public static Player requirePlayer(CommandSender sender, String errorMessage) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, errorMessage);
            return null;
        }
        return player;
    }
}
