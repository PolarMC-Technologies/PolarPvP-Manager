package org.PolarNL.PolarPvP.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YamlUtil {

    private static final Logger LOGGER = Logger.getLogger(YamlUtil.class.getName());

    private YamlUtil() {}

    public static ConfigurationSection loadSection(File dataFolder, String filename, String sectionKey) {
        File file = new File(dataFolder, filename);
        if (!file.exists()) {
            // file doesn't exist - expected on first run
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection(sectionKey);
        
        if (section == null) {
            // file exists but section is missing - log for debugging
            LOGGER.log(
                Level.FINE, 
                "Configuration section ''{0}'' not found in {1}", 
                new Object[]{sectionKey, filename}
            );
        }
        
        return section;
    }

    public static void saveConfig(YamlConfiguration config, File dataFolder, String filename, 
                                   Logger logger, String errorMessage) {
        try {
            config.save(new File(dataFolder, filename));
        } catch (IOException e) {
            logger.log(Level.SEVERE, errorMessage, e);
        }
    }
}
