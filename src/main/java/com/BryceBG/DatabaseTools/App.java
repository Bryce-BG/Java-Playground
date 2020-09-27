package com.BryceBG.DatabaseTools;


import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.ui.MainWindow;
import com.BryceBG.DatabaseTools.utils.Utils;

/**
 * Entry point to application. Decides to display UI or to run silently via
 * command-line.
 * 
 * As the "controller" to all other classes, it parses command line parameters.
 */
public class App {

	private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(App.class);

	/**
	 * Where everything starts. Takes in, and tries to parse as many commandline
	 * arguments as possible. Otherwise, it launches a GUI.
	 *
	 * @param args Array of command line arguments.
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.

		parseCmdArgs(args);
//		System.out.println("Version DEBUG: " + Utils.getThisJarVersion()); //TODO DEBUG
//		JOptionPane.showMessageDialog(null, Utils.getThisJarVersion());

		System.out.println("DEBUG FROM APP: " + Utils.getConfigString("app.dbpass", null));

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


        version.setRequired(false);
        options.addOption(version);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        

        
        if (args.length > 0 && cmd.hasOption('v')){
            System.out.println(Utils.getThisJarVersion());
            System.exit(0);
        }

        
        

        
        	
	}





	

}