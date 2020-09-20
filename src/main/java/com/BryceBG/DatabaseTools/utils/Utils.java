package com.BryceBG.DatabaseTools.utils;

public class Utils {
    private static final String DEFAULT_VERSION = "1.0.0";

    /**
     * Function used to determine version of the program running.
     * @return
     */
    public static String getThisJarVersion() {
        String thisVersion = Utils.class.getPackage().getImplementationVersion();
        if (thisVersion == null) {
            // Version is null if we're not running from the JAR
            thisVersion = DEFAULT_VERSION; 
        }
        return thisVersion;
    }

}
