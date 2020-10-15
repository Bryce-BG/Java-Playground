package com.BryceBG.DatabaseTools.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

//Log4j imports
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;


//import spark.resource.ClassPathResource;

public class Utils {
	private static final Logger LOGGER = LogManager.getLogger(Utils.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
	private static final String DEFAULT_VERSION = "0.0.5-SNAPSHOT(not_a_jar)";
	private static final String[] REQUIRED_CONFIG_KEYS = {"app.dbhost", "app.dbport", "app.dbname", "app.dbpass", "app.dbuser"};
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
	 * Helper function to searcha PropertiesConfiguration and ensure it contains required keys for our app to run
	 * 
	 * @param config The currently loaded configuration.
	 * @return True if there are missing critical keys, False if all required keys were included.
	 */
	private static boolean configIsMissingRequiredKeys(PropertiesConfiguration config) {
		for (String x : REQUIRED_CONFIG_KEYS) {
			if(!config.containsKey(x)) {
				return true;
			}
		}
		return false; //all required keys were included
		
	}

	/**
	 * Function used to determine version of the program running.
	 * @return 0.0.5 if not compiled to a jar. Otherwise the current version number (specified in the pom.xml).
	 */
	public static String getThisJarVersion() {
		String thisVersion = Utils.class.getPackage().getImplementationVersion();
		if (thisVersion == null) {
			// Version is null if we're not running from the JAR
			thisVersion = DEFAULT_VERSION; // indicator that jar is not compiled
		}
		return thisVersion;
	}

	//#################CONFIG RELATED GETTERS AND SETTERS#################
    /**
     * Gets the value of a specific config key.
     *
     * @param key The name of the config parameter you want to find.
     * @param defaultValue What the default value would be if key is not found.
     */
    public static String getConfigString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public static String[] getConfigStringArray(String key) {
        String[] configStringArray = config.getStringArray(key);

        return configStringArray.length == 0 ? null : configStringArray;
    }

    public static int getConfigInteger(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public static List<String> getConfigList(String key) {
        List<String> result = new ArrayList<>();
        for (Object obj : config.getList(key, new ArrayList<String>())) {
            if (obj instanceof String) {
                result.add((String) obj);
            }
        }
        return result;
    }

    public static void setConfigBoolean(String key, boolean value) {
        config.setProperty(key, value);
    }

    public static void setConfigString(String key, String value) {
        config.setProperty(key, value);
    }

    public static void setConfigInteger(String key, int value) {
        config.setProperty(key, value);
    }

    public static void setConfigList(String key, List<Object> list) {
        config.clearProperty(key);
        config.addProperty(key, list);
    }

    public static void setConfigList(String key, Enumeration<Object> enumeration) {
        config.clearProperty(key);
        List<Object> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        config.addProperty(key, list);
    }

    public static void saveConfig() {
        try {
            config.save(getConfigFilePath());
            LOGGER.info("Saved configuration to " + getConfigFilePath());
        } catch (ConfigurationException e) {
            LOGGER.error("Error while saving configuration: ", e);
        }
    }
	//#################END CONFIG RELATED GETTERS AND SETTERS#################

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
    
    /**
     * DEBUG function I am using to verify resources are correct (remove in final app)
     * @param resourceName
     */
    public static String readResourceToString(String resourceName) {
    	//getClass().getResourceAsStream("/pos_config.properties");//for non static functions.
//    	return getClass().getResourceAsStream(resourceName);
    	
//    	Utils.class.getResourceAsStream("pos_config.properties")
    	
    	
    	String result = "resource not found";
//    	InputStream inputStream =  Utils.class.getResourceAsStream(resourceName); //WORKS but needs "/" included in front of name of resource
    	InputStream inputStream =  Utils.class.getClassLoader().getResourceAsStream(resourceName);//
    	Scanner s = null;
    	try {
        	s = new Scanner(inputStream).useDelimiter("\\A");
        	result = s.hasNext() ? s.next() : "";
    	}
    	catch(Exception e) {
    		System.out.println("crashed");
    	}
    	finally {
    		try {
    		s.close();
    		}
    		catch(Exception e2)
    		{
//    			System.out.println("crashed trying to close scanner");
    		}
    	}
    	
    	return result;
    	
    }
    /**
     * Configures root logger, either for FILE output or just console.
     */
    public static void initializeAppLogger(String fileName, String pattern) {
    	//https://www.studytonight.com/post/log4j2-programmatic-configuration-in-java-class#:~:text=Log4j2%20Programmatic%20Configuration%20for%20File,builder%20%3D%20ConfigurationBuilderFactory.
    	
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        
        Level ourLoggingLevel = Level.DEBUG; //level our logging system runs at (i.e it should report all messages of equal or higher priority
        
        
//        builder.setStatusLevel(Level.DEBUG);//status logger is events that occur IN the actual logging system (so we don't want this to be low)
        builder.setConfigurationName("DefaultLogger");

        // create a console appender
        AppenderComponentBuilder appenderBuilder = builder.newAppender("Console", "CONSOLE").addAttribute("target",
                ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern));
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(ourLoggingLevel);
        rootLogger.add(builder.newAppenderRef("Console"));
        builder.add(appenderBuilder);
        
        if(getConfigBoolean("log.save", false)) { //only add file logger appender if config file indicates we should be saving a more permanent log
        // create a rolling file appender
        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern);
        ComponentBuilder<?> triggeringPolicy = builder.newComponent("Policies")
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "1KB"));
        appenderBuilder = builder.newAppender("LogToRollingFile", "RollingFile")
                .addAttribute("fileName", fileName)
                .addAttribute("filePattern", fileName+"-%d{MM-dd-yy-HH-mm-ss}.log.")
                .add(layoutBuilder)
                .addComponent(triggeringPolicy);
        builder.add(appenderBuilder);
        rootLogger.add(builder.newAppenderRef("LogToRollingFile"));
        }
        builder.add(rootLogger);
        Configurator.reconfigure(builder.build());
    }
   

}
