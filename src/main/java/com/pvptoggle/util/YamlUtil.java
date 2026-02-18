package com.pvptoggle.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YamlUtil {

    private static final Logger LOGGER = Logger.getLogger(YamlUtil.class.getName());

    private YamlUtil() {}

    /**
     * Loads a YAML configuration file and returns a specific configuration section.
     * @param dataFolder The plugin data folder
     * @param filename The name of the YAML file
     * @param sectionKey The configuration section key to retrieve
     * @return The ConfigurationSection or null if file doesn't exist or section doesn't exist
     */
    public static ConfigurationSection loadSection(File dataFolder, String filename, String sectionKey) {
        File file = new File(dataFolder, filename);
        if (!file.exists()) {
            // File doesn't exist - this is expected on first run
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection(sectionKey);
        
        if (section == null) {
            // File exists but section is missing - log for debugging
            LOGGER.log(
                Level.FINE, 
                "Configuration section ''{0}'' not found in {1}", 
                new Object[]{sectionKey, filename}
            );
        }
        
        return section;
    }

    /**
     * Saves a YAML configuration to a file with error handling.
     * @param config The YamlConfiguration to save
     * @param dataFolder The plugin data folder
     * @param filename The name of the YAML file
     * @param logger The logger to use for error messages
     * @param errorMessage The error message to log on failure
     */
    public static void saveConfig(YamlConfiguration config, File dataFolder, String filename, 
                                   Logger logger, String errorMessage) {
        try {
            config.save(new File(dataFolder, filename));
        } catch (IOException e) {
            logger.log(Level.SEVERE, errorMessage, e);
        }
    }
}
