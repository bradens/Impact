package ast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import db.DatabaseConnector;

import impact.Resources;

public class Parser
{
	private List<String> 	classpathEntries;
	private List<String> 	sourcepathEntries;
	private List<String> 	encodings;

	public Parser(String config) {
		classpathEntries = new ArrayList<String>();
		sourcepathEntries = new ArrayList<String>();
		encodings = new ArrayList<String>();

		init(config);
	}

	/**
	 * Initializes the configuration file to set up 
	 * parsing environment variables.
	 * @param config
	 */
	private void init(String config) {
		boolean classpath = false;
		try {
			FileInputStream fstream = new FileInputStream(config);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = br.readLine()) != null) {
				if(line.contains(Resources.classpath))
					classpath = true;
				else if(line.contains(Resources.sourcepath))
					classpath = false;
				else
					addPathToEnvironment(line, classpath);
			}
			br.close();
		}
		catch(Exception e) {

		}
	}

	/**
	 * Adds the path to the appropriate environment variable.
	 * @param path
	 * @param classpath If true then add to classpath. If false then add to sourcepath.
	 */
	private void addPathToEnvironment(String path, boolean classpath) {
		if(classpath) {
			classpathEntries.add(path);
		}
		else {
			sourcepathEntries.add(path);
			encodings.add(Resources.encoding);
		}
	}

	/**
	 * Parsers a given file using the ASTParser.
	 * @param file
	 */
	public void parseFile(String file, DatabaseConnector db) {
		// Create parse for JRE 1.0 - 1.6
		ASTParser parser= ASTParser.newParser(AST.JLS3);

		// Set up parser
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		Hashtable<String, String> options = JavaCore.getDefaultOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		parser.setCompilerOptions(options);
		parser.setEnvironment(classpathEntries.toArray(new String[classpathEntries.size()]), 
				sourcepathEntries.toArray(new String[sourcepathEntries.size()]), 
				encodings.toArray(new String[encodings.size()]), true);

		// Set up the file to parse
		String fileRaw = readFile(file);
		if(fileRaw == null)
			return;
		parser.setSource(fileRaw.toCharArray());
		String unitName = generateUnitName(file);
		if(unitName == null)
			return;
		parser.setUnitName(unitName);


		// Parse
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);


		// Visit the syntax tree
		Visitor visitor = new Visitor(unitName, unit, db);
		unit.accept(visitor);
	}


	/**
	 * This function reads a file into a String.
	 * @param path The absolute path.
	 * @return
	 */
	private String readFile(String path) {
		try {
			FileInputStream stream = new FileInputStream(new File(path));
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Generates the unit name needed for the AST parser based on
	 * the repository and file absolute path.
	 * @param file The absolute path.
	 * @return
	 */
	private String generateUnitName(String file) {
		// Get repository name
		String repoName = Resources.repository.substring(Resources.repository.lastIndexOf("/")+1);

		// Get starting point of unit name
		int start = file.indexOf(Resources.repository)
				+ Resources.repository.length() - repoName.length();

		// Get the unit name
		String unitName = file.substring(start);

		return unitName;
	}
}