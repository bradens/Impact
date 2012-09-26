package db;

import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.auth.AccessToken;

import models.Message;
import models.Method;
import models.Pair;

import db.util.ISetter;
import db.util.ISetter.IntSetter;
import db.util.PreparedStatementExecutionItem;
import db.util.ISetter.StringSetter;
import db.util.ISetter.FloatSetter;

import impact.Resources;

public class DatabaseConnector extends DbConnection {
	
	public void createDatabase(String dbName) {
		try {
			// Drop the DB if it already exists
			String query = "DROP DATABASE IF EXISTS " + dbName;
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, null);
			addExecutionItem(ei);
			ei.waitUntilExecuted();

			// First create the DB.
			query = "CREATE DATABASE " + dbName + ";";
			ei = new PreparedStatementExecutionItem(query, null);
			addExecutionItem(ei);
			ei.waitUntilExecuted();

			// Reconnect to our new database.
			close();
			connect(dbName.toLowerCase());

			// load our schema			
			runScript(new InputStreamReader(this.getClass().getResourceAsStream("schema.sql")));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method inserts a method into the methods table. If the method already
	 * exists then it will update the start and end line numbers.
	 * @param method
	 * @return
	 */
	public int insertMethod(Method method) {
		int id = getMethodID(method);
		if(id != -1)
			return updateMethodLines(method, id);

		// Set up parameters
		String parameters = "{";
		if(!method.getParameters().isEmpty()) {
			for(String param: method.getParameters()) {
				parameters += "\"" + param + "\", ";
			}
			parameters = parameters.substring(0, parameters.length()-2);
		}
		parameters += "}";

		String query = "INSERT INTO methods (file_name, package_name, class_type, method_name, " +
				"parameters, start_line, end_line, id) VALUES " +
				"(?, ?, ?, ?, \'" + parameters + "\', " + method.getStart() + ", " + method.getEnd() + ", default)";
		ISetter[] params = {
				new StringSetter(1,method.getFile()),
				new StringSetter(2,method.getPkg()),
				new StringSetter(3,method.getClazz()),
				new StringSetter(4,method.getName())
		};

		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
		addExecutionItem(ei);
		ei.waitUntilExecuted();
		return getSequenceValue("method_id_seq"); 
	}
	
	/**
	 * This method will update the start and end line numbers of
	 * a given method inside the methods table.
	 * @param method
	 * @param id
	 * @return
	 */
	private int updateMethodLines(Method method, int id) {
		if(method.getStart() != -1 && method.getEnd() != -1 &&
				method.getFile() != null){
			String query = "UPDATE methods SET file_name=?, start_line=?, end_line=? WHERE id=?";
			ISetter[] params = {
				new StringSetter(1,method.getFile()),
				new IntSetter(2,method.getStart()),
				new IntSetter(3,method.getEnd()),
				new IntSetter(4,id)
			};

			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
		}

		return id;
	}

	/**
	 * Get Sequence Id for a sequence table
	 * @param sequence
	 * @return sequence id, -1 of none found or there is exception
	 */
	private int getSequenceValue(String sequence) {
		try 
		{
			// Get the ID
			String sql = "SELECT currval(?)"; 
			ISetter[] params = {new StringSetter(1, sequence)};
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			ResultSet rs = ei.getResult();
			if(rs != null && rs.next())
				return rs.getInt("currval");
			return -1;
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * This method checks for a pre-existing method and returns its ID if
	 * it does exist. Returns -1 if DNE.
	 * @param method
	 * @return
	 */
	public int getMethodID(Method method) {
		try {
			// Set up parameters
			String parameters = "{";
			if(!method.getParameters().isEmpty()) {
				for(String param: method.getParameters()) {
					parameters += "\"" + param + "\", ";
				}
				parameters = parameters.substring(0, parameters.length()-2);
			}
			parameters += "}";

			String query = "SELECT id FROM methods WHERE package_name=? AND" +
					" class_type=? AND method_name=? AND parameters=\'" + parameters + "\'";
			ISetter[] params = {
					new StringSetter(1,method.getPkg()),
					new StringSetter(2,method.getClazz()),
					new StringSetter(3,method.getName())
			};

			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(eifirst);
			eifirst.waitUntilExecuted();
			ResultSet rs = eifirst.getResult();

			if(rs.next())
				return rs.getInt("id");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * This inserts a new record into the invokes table.
	 * @param caller
	 * @param callee
	 */
	public void insertInvokes(int caller, int callee) {
		String query = "INSERT INTO invokes (caller, callee, id) VALUES " +
				"(?, ?, default)";
		ISetter[] params = {
				new IntSetter(1,caller),
				new IntSetter(2,callee)
		};

		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
		addExecutionItem(ei);
		ei.waitUntilExecuted();
	}
	
	public int getUserID(String email) {
		try {
			String sql = "SELECT * FROM users WHERE email=?"; 

			ISetter[] params = {
					new StringSetter(1, email)
			};
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			ResultSet rs = ei.getResult();
			if(rs.next())
				return rs.getInt("uid");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public void insertMessage(Message message) {
		int to = getUserID(message.getTo());
		int from = getUserID(message.getFrom());
		String query = "INSERT INTO messages (mid, uid_to, uid_from, m_changed, m_impacted, weight, impact_scale) VALUES " +
				"(default, ?, ?, ?, ?, ?, ?)";
		ISetter[] params = {
				new IntSetter(1,to),
				new IntSetter(2,from),
				new StringSetter(3, message.getChange()),
				new StringSetter(4, message.getImpacted()),
				new FloatSetter(5, message.getWeight()),
				new StringSetter(6, message.getImpactScale().toString())
		};
		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
		addExecutionItem(ei);
		ei.waitUntilExecuted();
	}
	
	public int messageExists(Message message) {
		int to = getUserID(message.getTo());
		int from = getUserID(message.getFrom());
		try {
			String sql = "SELECT * FROM message WHERE " +
					"uid_to=? AND uid_from=? AND m_changed=? AND m_impacted=? AND weight=? AND impact_scale=?"; 

			ISetter[] params = {
					new IntSetter(1,to),
					new IntSetter(2,from),
					new StringSetter(3, message.getChange()),
					new StringSetter(4, message.getImpacted()),
					new FloatSetter(5, message.getWeight()),
					new StringSetter(6, message.getImpactScale().toString())
			};
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			ResultSet rs = ei.getResult();
			if(rs.next())
				return rs.getInt("mid");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}

	/**
	 * This method removes all information to deal with
	 * the current call graph in the database.
	 */
	public void deleteCallGraph() {
		String query = "DELETE FROM invokes";
		ISetter[] params = {};
		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
		addExecutionItem(ei);
		ei.waitUntilExecuted();

		query = "DELETE FROM methods";
		ei = new PreparedStatementExecutionItem(query, params);
		addExecutionItem(ei);
		ei.waitUntilExecuted();
	}
	
	/**
	 * This function will return all methods that call a given method.
	 * Note: the parameter method must have a valid ID.
	 * @param method
	 * @return
	 */
	public List<Method> getCallersOfMethod(Method method) {
		List<Method> callers = new ArrayList<Method>();

		try 
		{
			// Get intersecting methods
			String sql = "SELECT * FROM methods JOIN invokes ON (methods.id=invokes.caller)" +
					" WHERE callee=?"; 

			ISetter[] params = {
					new IntSetter(1, method.getId())
			};
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			ResultSet rs = ei.getResult();
			while(rs.next()) {
				callers.add(new Method(rs.getString("file_name"), rs.getString("package_name"), rs.getString("class_type"),
						rs.getString("method_name"), 
						convertParametersToList((String[])rs.getArray("parameters").getArray()), 
						rs.getInt("start_line"), rs.getInt("end_line"), rs.getInt("id")));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return callers;
	}
	
	/**
	 * This method simply converts the parameters arrays from the methods
	 * table to a usable list of strings.
	 * @param parameters
	 * @return
	 */
	private List<String> convertParametersToList(String[] parameters) {
		List<String> params = new ArrayList<String>();
		for(String param: parameters) {
			params.add(param);
		}
		return params;
	}
	
	/**
	 * This function returns the commit ID that the database
	 * call graph currently represents.
	 * @return
	 */
	public String getCurrentCommit() {
		try {
			String query = "SELECT commit_id FROM properties WHERE repository=?";
			ISetter[] params = {
					new StringSetter(1,Resources.repository)
			};

			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(eifirst);
			eifirst.waitUntilExecuted();
			ResultSet rs = eifirst.getResult();

			if(rs.next())
				return rs.getString("commit_id");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateProperties(String commit_id) {
		if(!propertiesExist()) {
			String query = "INSERT INTO properties (repository, commit_id) VALUES " +
					"(?, ?)";
			ISetter[] params = {
					new StringSetter(1,Resources.repository),
					new StringSetter(2,commit_id)
			};
	
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
		}
		else {
			String query = "UPDATE properties SET commit_id=? WHERE repository=?" +
					"(?, ?)";
			ISetter[] params = {
					new StringSetter(1,commit_id),
					new StringSetter(2,Resources.repository)
			};
	
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
		}
	}
	
	private boolean propertiesExist() {
		try {
			String query = "SELECT * FROM properties WHERE repository=?";
			ISetter[] params = {
					new StringSetter(1,Resources.repository)
			};

			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(eifirst);
			eifirst.waitUntilExecuted();
			ResultSet rs = eifirst.getResult();

			if(rs.next())
				return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void deleteProperties() {
		String query = "DELETE FROM properties";
		ISetter[] params = {};
		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
		addExecutionItem(ei);
		ei.waitUntilExecuted();
	}
	
	/**
	 * This method will return all methods inside a given file that overlap
	 * with the given start and end line numbers with a percent of overlap.
	 * @param file
	 * @param start
	 * @param end
	 * @return
	 */
	public List<Pair<Method, Float>> getChangedMethods(String file, int start, int end) {
		List<Pair<Method, Float>> changedMethods = new ArrayList<Pair<Method, Float>>();

		try 
		{
			// Get intersecting methods
			String sql = "SELECT * FROM methods WHERE file_name=? AND start_line < ? AND ? < end_line"; 

			ISetter[] params = {
					new StringSetter(1, file),
					new IntSetter(2, end),
					new IntSetter(3, start)
			};
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			ResultSet rs = ei.getResult();
			while(rs.next()) {
				// Create method with fake weight
				Pair<Method, Float> pair = new Pair<Method, Float>(
						new Method(rs.getString("file_name"), rs.getString("package_name"), rs.getString("class_type"),
								rs.getString("method_name"), 
								convertParametersToList((String[])rs.getArray("parameters").getArray()), 
								rs.getInt("start_line"), rs.getInt("end_line"), rs.getInt("id")), 0.0f);
				// Set real weight
				pair.setSecond((Math.min(end, pair.getFirst().getEnd()) - 
						Math.max(start, pair.getFirst().getStart()) + 1)/
						(float)(pair.getFirst().getEnd() - pair.getFirst().getStart() + 1));
				if((Math.min(end, pair.getFirst().getEnd()) - 
						Math.max(start, pair.getFirst().getStart()) + 1)/
						(float)(pair.getFirst().getEnd() - pair.getFirst().getStart() + 1) > 1.0f) {
					int x = 0;
					x = x + x;
				}

				// Add new pair to list
				changedMethods.add(pair);
			}
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}

		return changedMethods;
	}
	
	public void storeTwitterAccessToken(AccessToken accessToken) {
		String query = "UPDATE users SET twitter_accesstoken=?, " +
				"twitter_accesstokensecret=? WHERE email=?";
		
		ISetter[] params = {
				new StringSetter(1,accessToken.getToken()),
				new StringSetter(2,accessToken.getTokenSecret()),
				new StringSetter(3,Resources.user)
		};

		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
		addExecutionItem(ei);
		ei.waitUntilExecuted();
	}
	
	public AccessToken getTwitterAccessToken() {
		try {
			String query = "SELECT * FROM users WHERE email=?";
			ISetter[] params = {
					new StringSetter(1,Resources.user)
			};

			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(eifirst);
			eifirst.waitUntilExecuted();
			ResultSet rs = eifirst.getResult();

			if(rs.next() && rs.getString("twitter_accesstoken") != null) {
				return new AccessToken(rs.getString("twitter_accesstoken"), 
						rs.getString("twitter_accesstokensecret"));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
