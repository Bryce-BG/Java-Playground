package com.BryceBG.DatabaseTools;



import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.Database.LibraryDB;
import com.BryceBG.DatabaseTools.ui.MainWindow;
import com.BryceBG.DatabaseTools.utils.Utils;


/**
 * Entry point to application. Decides to display UI or to run silently via
 * command-line.
 * 
 * As the "controller" to all other classes, it parses command line parameters.
 */
public class App {

	private static final Logger logger = LogManager.getLogger(App.class.getName());

	

    
	/**
	 * Where everything starts. Takes in, and tries to parse as many commandline
	 * arguments as possible. Otherwise, it launches a GUI.
	 *
	 * @param args Array of command line arguments.
	 */
	public static void main(String[] args) {
        Utils.initializeAppLogger("app.log","%d %p %c [%t] %m%n"); //sets up our logger instance for the program
		logger.info("Loaded app version: " + Utils.getThisJarVersion());

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		parseCmdArgs(args);

        logger.info("App Log4j2 system initialized");

        //TODO check if database exists if not call createDB?
        
        
        

        
		MainWindow mw = new MainWindow();
		SwingUtilities.invokeLater(mw);

	}

	/**
     * 
     * @param args
     */
	private static void parseCmdArgs(String[] args) {
		Options options = new Options();

        Option version = new Option("v", "version", false, "Version of the program");
        Option initialize = new Option("c", "initilize_database", true, "create the postgresql database for our system");

        version.setRequired(false);
        options.addOption(version);
        
        initialize.setRequired(false);
        options.addOption(initialize);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
        	logger.fatal("system crashed due to invalid input options: " + e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        if (args.length > 0 && cmd.hasOption('v')){
            logger.info("The app version is: " + Utils.getThisJarVersion());//print to our log (and also usually to stdout
            System.exit(0);
        }

        if (args.length > 0 && cmd.hasOption('c')){
        	String newLibraryName = cmd.getOptionValue("c");
        	System.out.println(String.format("Our library name parsed is %s", newLibraryName));

            if(LibraryDB.createDB(newLibraryName))
            	System.out.println("Database creation was successful");
            else
            	System.out.println("Database creation was un-successful");
            System.exit(0);
        }
        
	}
	





	

}
