package com.pvptoggle.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Helpers for sending colored chat messages, action-bar messages, and formatting durations.
 */
public final class MessageUtil {

    private MessageUtil() {}

    /** Send a color-coded chat message to any CommandSender. */
    public static void send(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /** Send a color-coded action-bar message (Spigot API, works on Paper/Purpur too). */
    @SuppressWarnings("deprecation")
    public static void sendActionBar(Player player, String message) {
        if (message == null || message.isEmpty()) return;
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colored));
    }

    /**
     * Format a duration given in seconds into a human-readable string.
     * Examples: "1h 23m 4s", "5m 10s", "42s"
     */
    public static String formatTime(long totalSeconds) {
        if (totalSeconds <= 0) return "0s";
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0)   sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
