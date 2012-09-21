package ast;

import java.io.File;
import java.util.List;

import db.DatabaseConnector;

import impact.Resources;

import git.GitController;

public class CallGraphGenerator
{
	private GitController gc;
	private DatabaseConnector db;

	public CallGraphGenerator(DatabaseConnector db) {
		gc = new GitController();
		this.db = db;
	}

	/**
	 * Creates a new call graph in the database from scratch.
	 * @param commitID
	 */
	public void createCallGraphAtCommit(String commitID) {
		// Set the repo to the commit
		gc.reset(commitID);

		// Set up the config file
		FolderTraverser traversal = new FolderTraverser(new File(Resources.repository));
		traversal.traverse();

		// Get all the files
		List<String> files = gc.getAllFiles();

		// Parse each file
		for(String file: files) {
			Parser parser = new Parser(Resources.configFile);
			parser.parseFile(fileFullPath(file), db);
		}
	}

	private String fileFullPath(String file) {
		String fullPath;

		fullPath = Resources.repository + "/" + file;

		return fullPath;
	}
}