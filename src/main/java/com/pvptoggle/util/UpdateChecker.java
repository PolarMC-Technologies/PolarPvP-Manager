package com.pvptoggle.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.pvptoggle.PvPTogglePlugin;

public class UpdateChecker implements Listener {

    private static final String GITHUB_API =
            "https://api.github.com/repos/SSoggyTacoBlud/PolarPvP-Manager/releases/latest";

    private final PvPTogglePlugin plugin;
    private volatile String latestVersion = null;

    public UpdateChecker(PvPTogglePlugin plugin) {
        this.plugin = plugin;
    }

    public void check() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) URI.create(GITHUB_API).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() != 200) return;

                StringBuilder jsonResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) jsonResponse.append(line);
                }

                // Ghetto JSON parse - just grab "tag_name":"vX.X.X"
                String json = jsonResponse.toString();
                String tag = extractTag(json);
                if (tag == null) return;

                // Strip leading 'v' if present
                String remote = tag.startsWith("v") ? tag.substring(1) : tag;
                String current = plugin.getDescription().getVersion();

                if (!remote.equals(current) && isNewer(remote, current)) {
                    latestVersion = remote;
                    plugin.getLogger().log(Level.WARNING, "A new version is available: v{0} (you''re on v{1})",
                            new Object[]{remote, current});
                    plugin.getLogger().warning("Download: https://github.com/SSoggyTacoBlud/PolarPvP-Manager/releases/latest");
                }
            } catch (java.io.IOException e) {
                plugin.getLogger().log(Level.FINE, "Update check failed", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (latestVersion == null) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("pvptoggle.admin")) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                MessageUtil.send(player, "&e&lPolarPvP &8» &7Update available: &av" + latestVersion
                        + " &7(you're on &cv" + plugin.getDescription().getVersion() + "&7)");
            }
        }, 60L); // 3 seconds after join
    }

    private String extractTag(String json) {
        String key = "\"tag_name\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int start = json.indexOf('"', idx + key.length() + 1);
        int end = json.indexOf('"', start + 1);
        if (start == -1 || end == -1) return null;
        return json.substring(start + 1, end);
    }

    // Simple version comparison: 1.1.0 > 1.0.0
    private boolean isNewer(String remote, String current) {
        String[] remoteParts = remote.split("\\.");
        String[] currentParts = current.split("\\.");
        int len = Math.max(remoteParts.length, currentParts.length);
        for (int i = 0; i < len; i++) {
            int remoteVersion = i < remoteParts.length ? parseOr(remoteParts[i], 0) : 0;
            int currentVersion = i < currentParts.length ? parseOr(currentParts[i], 0) : 0;
            if (remoteVersion > currentVersion) return true;
            if (remoteVersion < currentVersion) return false;
        }
        return false;
    }

    private int parseOr(String s, int fallback) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return fallback; }
    }
}
