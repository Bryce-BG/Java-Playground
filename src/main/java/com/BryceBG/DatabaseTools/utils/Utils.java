package com.BryceBG.DatabaseTools.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.Logger;


import spark.resource.ClassPathResource;

public class Utils {
	private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(Utils.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
	private static final String DEFAULT_VERSION = "0.0.5";
    private static PropertiesConfiguration config;

	private static final String CONFIG_FILE = "library.properties"; // default config if not specified

	static {

		try {
			String configPath = getConfigFilePath();
			File file = new File(configPath);

			if (!file.exists()) {
				// Use default bundled with .jar
				configPath = CONFIG_FILE;
			}
			config = new PropertiesConfiguration(configPath);
			
			LOGGER.info("Loaded " + config.getPath());

			if (file.exists()) {
				// Config was loaded from file
				if (configIsMissingRequiredKeys(config)) {
					// Config is missing key fields
					// Need to reload the default config
					// See https://github.com/4pr0n/ripme/issues/158
					LOGGER.warn("Config does not contain key fields, deleting old config");
					file.delete();
					config = new PropertiesConfiguration(CONFIG_FILE);
					
					LOGGER.info("Loaded " + config.getPath());
				}
			}
		} catch (Exception e) {
			LOGGER.error("[!] Failed to load properties file from " + CONFIG_FILE, e);
		}
	}

	/**
	 * Helper function to load the config and ensure that our required keys are in
	 * the file currently.
	 * 
	 * @param config
	 * @return
	 */
	private static boolean configIsMissingRequiredKeys(PropertiesConfiguration config) {
		return !config.containsKey("app.dbhost") || !config.containsKey("app.dbport")
				|| !config.containsKey("app.dbname") || !config.containsKey("app.dbpass")
				|| !config.containsKey("app.dbuser");
	}

	/**
	 * Function used to determine version of the program running.
	 * 
	 * @return
	 */
	public static String getThisJarVersion() {
		String thisVersion = Utils.class.getPackage().getImplementationVersion();
		if (thisVersion == null) {
			// Version is null if we're not running from the JAR
			thisVersion = DEFAULT_VERSION; // Super-high version number
		}
		return thisVersion;
	}

	/**
	 * Gets the value of a specific config key.
	 *
	 * @param key The name of the config parameter you want to find.
	 * @param defaultValue What the default value would be.
	 */
	public static String getConfigString(String key, String defaultValue) {
		return config.getString(key, defaultValue); 
	}

    /**
     * Gets the path to the configuration file.
     */
    private static String getConfigFilePath() {
        return getConfigDir() + File.separator + CONFIG_FILE;
    
    }
    /**
     * Gets the directory of the config directory, for all systems.
     */
    public static String getConfigDir() {
        if (portableMode()) {
            try {
                return getJarDirectory().getCanonicalPath();
            } catch (Exception e) {
                return ".";
            }
        }
        if (isWindows())
            return getWindowsConfigDir();
        if (isMacOS())
            return getMacOSConfigDir();
        if (isUnix())
            return getUnixConfigDir();

        try {
            return getJarDirectory().getCanonicalPath();
        } catch (Exception e) {
            return ".";
        }
    }
    /**
     * Determines if the app is running in a portable mode. i.e. on a USB stick
     */
    private static boolean portableMode() {
        try {
            File file = new File(getJarDirectory().getCanonicalPath() + File.separator + CONFIG_FILE);
            if (file.exists() && !file.isDirectory()) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }
    private static File getJarDirectory() {
        File jarDirectory = Utils.class.getResource("/" + CONFIG_FILE).toString().contains("jar:")
                ? new File(System.getProperty("java.class.path")).getParentFile()
                : new File(System.getProperty("user.dir"));

        if (jarDirectory == null)
            jarDirectory = new File(".");

        return jarDirectory;
    }

    /**
     * Determines if your current system is a Windows system.
     */
    public static boolean isWindows() {
        return OS.contains("win");
    }

    /**
     * Determines if your current system is a Mac system
     */
    private static boolean isMacOS() {
        return OS.contains("mac");
    }

    /**
     * Determines if current system is based on UNIX
     */
    private static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("bsd");
    }

        
    /**
     * Gets the directory of where the config file is stored on a Windows machine.
     */
    private static String getWindowsConfigDir() {
        return System.getenv("LOCALAPPDATA") + File.separator + "ripme";
    }

    /**
     * Gets the directory of where the config file is stored on a UNIX machine.
     */
    private static String getUnixConfigDir() {
        return System.getProperty("user.home") + File.separator + ".config" + File.separator + "ripme";
    }

    /**
     * Gets the directory of where the config file is stored on a Mac machine.
     */
    private static String getMacOSConfigDir() {
        return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support"
                + File.separator + "ripme";
    }
   

}
