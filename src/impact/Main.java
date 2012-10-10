package impact;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

import servlet.ServletRunner;

import db.DatabaseConnector;;


public class Main {
	public static void main(String[] args) {
		System.out.println("Impact!");
		System.out.println();
		
		ServletRunner runner = new ServletRunner();
		new Thread(runner).start();
		
		CommandLineParser parser = new GnuParser();
		try {
			if(args.length < 1) {
				printMan();
				return;
			}
			else {
				Options options = new Options();
				
				Option repo = OptionBuilder.withArgName("r").hasArg().create("r");
				
				options.addOption(repo);
				
				CommandLine line = parser.parse(options,  args);
				
				// Setup the repo path and run the daemon
				if(line.hasOption("r")) {
					String[] values = line.getOptionValues("r");
					if(values.length != 1) {
						System.out.println("You must specify the path to the given repository with the r flag. " +
								"Example: /home/user/repo");
						return;
					}
					else {
						Resources.dbName = "test";
						Resources.repository = values[0];
						Resources.branch = "master";
						Resources.configFile = "/home/jordan/config.txt";
						setRepositoryName(Resources.repository);
					}
						
					// Connect to the database
					DatabaseConnector db = new DatabaseConnector();
					db.connect("test");
					
					// Run the daemon starting from here
					/*
					System.out.println("Using repository: " + values[0]);
					Daemon dae = new Daemon(db);
					dae.runClient();
					*/
					
					db.close();
				}
				else {
					System.out.println("You must specify a git repository using the r flag.");
					return;
				}
					
			}
		}
		catch (Exception e) {
			printMan();
		}
		
	}
	
	private static void setRepositoryName(String path) {
		Resources.repositoryName = path.substring(path.lastIndexOf("/")+1);
	}
	
	private static void printMan() {
		System.out.println("Print the man page here.");
	}
}
