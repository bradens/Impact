package impact;

import ast.CallGraphGenerator;
import git.GitController;
import db.DatabaseConnector;
import diff.UnifiedDiffParser;

public class Daemon {
	
	private DatabaseConnector db;
	private GitController gc;
	private UnifiedDiffParser udp;
	private CallGraphGenerator cgg;
	
	public Daemon(DatabaseConnector db) {
		this.db = db;
		gc = new GitController();
		udp = new UnifiedDiffParser();
		cgg = new CallGraphGenerator(db);
	}
	
	public void run() {
		// Build the initial call graph on daemon start
		buildCallGraph();
		
		String currentCommit = db.getCurrentCommit();
		
		if(currentCommit == null || !currentCommit.equals(gc.getHead())) {
			// We need to rebuild the call graph
		}
		
		// Run the local diff
		
	}
	
	public void buildCallGraph() {
		System.out.println("Generating call graph for current HEAD of repository");
		db.deleteCallGraph();
		cgg.createCallGraphAtCommit(gc.getHead());
	}

}
