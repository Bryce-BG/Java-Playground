package com.BryceBG.DatabaseTools;



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
import com.BryceBG.DatabaseTools.Database.Book.BookController;
import com.BryceBG.DatabaseTools.Database.Index.IndexController;
import com.BryceBG.DatabaseTools.Database.Login.LoginController;
import com.BryceBG.DatabaseTools.utils.Filters;
import com.BryceBG.DatabaseTools.utils.Path;
import com.BryceBG.DatabaseTools.utils.Utils;
import com.BryceBG.DatabaseTools.utils.ViewUtil;

import io.javalin.Javalin;
import io.javalin.core.util.RouteOverviewPlugin;

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

        
        
        
        
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");
            config.registerPlugin(new RouteOverviewPlugin("/routes"));
        }).start(7000);

        app.routes(() -> {
            app.before(Filters.stripTrailingSlashes);
            app.before(Filters.handleLocaleChange);
            app.before(LoginController.ensureLoginBeforeViewingBooks);
            app.get(Path.Web.INDEX, IndexController.serveIndexPage);
            app.get(Path.Web.BOOKS, BookController.fetchAllBooks);
            app.get(Path.Web.ONE_BOOK, BookController.fetchOneBook);
            app.get(Path.Web.LOGIN, LoginController.serveLoginPage);
            app.post(Path.Web.LOGIN, LoginController.handleLoginPost);
            app.post(Path.Web.LOGOUT, LoginController.handleLogoutPost);
        });

        app.error(404, ViewUtil.notFound);
        
//		MainWindow mw = new MainWindow();
//		SwingUtilities.invokeLater(mw);

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
