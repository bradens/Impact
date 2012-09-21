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
				
				Option daemon = OptionBuilder.withArgName("d").create("d");
				Option repo = OptionBuilder.withArgName("r").hasArg().create("r");
				
				Option config = OptionBuilder.withArgName("c").create("c");
				
				options.addOption(daemon);
				options.addOption(repo);
				options.addOption(config);
				
				CommandLine line = parser.parse(options,  args);
				
				if(line.hasOption("d") && !line.hasOption("c")) {
					System.out.println("Running Impact in daemon mode.");
					
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
							
							// Connect to the database
							DatabaseConnector db = new DatabaseConnector();
							db.connect("impact");
							db.createDatabase("test");
							
							// Run the daemon starting from here
							System.out.println("Using repository: " + values[0]);
							Daemon dae = new Daemon(db);
							dae.run();
							
							db.close();
						}
					}
					else {
						System.out.println("You must specify a git repository using the r flag.");
						return;
					}
					
				}
				else if(line.hasOption("c") && !line.hasOption("d")) {
					
				}
				else {
					System.out.println("The c and d flags cannot be used together.");
					return;
				}
			}
		}
		catch (Exception e) {
			printMan();
		}
		
	}
	
	private static void printMan() {
		System.out.println("Print the man page here.");
	}
}
