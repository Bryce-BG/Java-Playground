package com.BryceBG;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.BryceBG.DatabaseTools.ui.MainWindow;
import com.BryceBG.DatabaseTools.utils.Utils;

/**
 * Entry point to application. Decides to display UI or to run silently via
 * command-line.
 * 
 * As the "controller" to all other classes, it parses command line parameters.
 */
public class App {
	private static String root_Dir_Location;

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
