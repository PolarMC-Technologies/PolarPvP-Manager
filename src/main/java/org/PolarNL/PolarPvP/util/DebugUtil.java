package org.PolarNL.PolarPvP.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

public final class DebugUtil {

    private DebugUtil() {}

    public static void logDebug(FileConfiguration config, Logger logger, String message, Object... params) {
        if (config.getBoolean("debug", false)) {
            logger.log(Level.INFO, "[DEBUG] " + message, params);
        }
    }
}
