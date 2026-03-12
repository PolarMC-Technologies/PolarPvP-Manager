package com.pvptoggle.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

public final class DebugUtil {

    private DebugUtil() {}

    /**
     * Logs a debug message if debug mode is enabled in the config.
     * @param config The plugin configuration
     * @param logger The logger to use
     * @param message The message to log
     * @param params Optional parameters for formatting
     */
    public static void logDebug(FileConfiguration config, Logger logger, String message, Object... params) {
        if (config.getBoolean("debug", false)) {
            logger.log(Level.INFO, "[DEBUG] " + message, params);
        }
    }
}
