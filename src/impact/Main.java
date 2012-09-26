package impact;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import db.DatabaseConnector;;


public class Main {
	public static void main(String[] args) {
		System.out.println("Impact!");
		System.out.println();
		
		CommandLineParser parser = new GnuParser();
		try {
			if(args.length < 1) {
				printMan();
				return;
			}
			else {
				Options options = new Options();
				
				Option client = OptionBuilder.withArgName("c").create("c");
				Option server = OptionBuilder.withArgName("s").create("s");
				
				Option repo = OptionBuilder.withArgName("r").hasArg().create("r");
				
				Option user = OptionBuilder.withArgName("u").hasArg().create("u");
				
				Option email = OptionBuilder.withArgName("e").create("e");
				Option tweet = OptionBuilder.withArgName("t").create("t");
				
				options.addOption(repo);
				options.addOption(user);
				options.addOption(email);
				options.addOption(tweet);
				options.addOption(client);
				options.addOption(server);
				
				CommandLine line = parser.parse(options,  args);
				
				// Start the client daemon
				if(line.hasOption("c") && !line.hasOption("s")) {
					// Get the user name
					if(line.hasOption("u")) {
						String[] values = line.getOptionValues("u");
						if(values.length != 1) {
							System.out.println("You must specify your git email with the u flag.");
							return;
						}
						else {
							Resources.user = values[0];
						}
					}
					else {
						System.out.println("Please use the u flag to set your git email for daemon mode.");
						return;
					}
					
					System.out.println("Running Impact in client daemon mode.");
					
					// Setup the types of communication specified
					Resources.email = line.hasOption("e");
					Resources.tweet = line.hasOption("t");
					
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
						System.out.println("Using repository: " + values[0]);
						Daemon dae = new Daemon(db);
						dae.runClient();
						
						db.close();
					}
					else {
						System.out.println("You must specify a git repository using the r flag.");
						return;
					}
					
				}
				
				// Start the server daemon
				else if(line.hasOption("s") && !line.hasOption("c")) {
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
						
						System.out.println("Running Impact in server daemon mode.");
							
						// Connect to the database
						DatabaseConnector db = new DatabaseConnector();
						db.connect("impact");
						db.createDatabase("test");
						db.connect("test");
						
						// Run the daemon starting from here
						System.out.println("Using repository: " + values[0]);
						Daemon dae = new Daemon(db);
						dae.runServer();
						
						db.close();
					}
					else {
						System.out.println("You must specify a git repository using the r flag.");
						return;
					}
				}
				
				// Cannot run client and server
				else {
					System.out.println("The c and s flags cannot be used together.");
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
