package org.PolarNL.PolarPvP.util;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigUtil {

    private ConfigUtil() {}

    public static Material getWandMaterial(FileConfiguration config) {
        try {
            String matConfig = config.getString("zone-wand-material");
            return Material.valueOf((matConfig != null ? matConfig : "BLAZE_ROD").toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.BLAZE_ROD;
        }
    }
}
