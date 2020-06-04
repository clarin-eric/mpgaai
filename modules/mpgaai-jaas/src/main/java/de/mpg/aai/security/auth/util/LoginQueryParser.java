package de.mpg.aai.security.auth.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * simple parser which
 * sets for certain defined variable-names in a given query 
 * their proper values to the accordant java.sql prepared-statement
 * where those values are derived from a given "loginname":
 * 
 * <p>
 * there is a basic set of variables available one can use in the userQuery:
 * <ul> 
 * 	<li>$loginname : the "username"/value a user entered when prompted for authentication</li>
 * 	<li>$username : user-identifier part of a scoped userID (the left side of the delimiter)</li>
 * 	<li>$delimiter : value of the delimiter to separate user-identifier from the security domain in a scoped-user-ID</li>
 * 	<li>$scope : the security-domain part of a scoped-user-ID</li>
 * </ul>
 * </p>
 * <p>
 * an example user query would look like e.g:<br />
 * <code>select username,password from user_table where (uid = $username and domain = $scope) or email = $loginname</code>
 * <br />the prepared-statement query would then be accordingly: 
 * <code>select username,password from user_table where (uid = ? and domain = ?) or email = ?</code><br />
 * where the variables e.g. for a given login-name "user@somewhere"
 * would be $loginname="user@somewhere", $username="user", $scope="somewhere" ($delimiter="@")
 * set to the given preparedStatement at the proper positions.  
 * </p>
 * @author	last modified by $Author$, created by megger
 * @version	$Revision$
 */
public class LoginQueryParser {
	/** the logger */
	private static Logger	_log = LoggerFactory.getLogger(LoginQueryParser.class);
	
	/** name of the default query config parameter to lookup the configured actual query string by (see {@link #init(String, Map)} */
	public static final String		QUERYNAME_DEFAULT =	"query";
	/** name of the user-query config parameter to lookup the configured actual query string by (see {@link #init(String, Map)} */
	public static final String		QUERYNAME_USER =	"userQuery";
	/** name of the group-query config parameter to lookup the configured actual query string by (see {@link #init(String, Map)} */
	public static final String		QUERYNAME_GROUP =	"groupQuery";
	/** original sql query with variable(-name)s used */
	private String		varQuery;
	/** modified query for prepared statement: variables replaced by "?" */
	private String		prepQuery;
	/** mapping of variable-names -> values */
	private Map<String, String>			varMap;
	/** mapping of variable-names -> position (java sql-stmt "index") in the original query */
	private Map<String, Set<Integer>>	posMap;
	/** parser for handling scoped username */
	private ScopeParser	uidParser;
	
	
	/**
	 * default constructor
	 */
	public LoginQueryParser() {
	}
	
	
	/**
	 * initializes this parser, 
	 * look up properties: "query", {@link ScopeParser#init(Map)} init options
	 * @param options map of config parameter    
	 */
	public void init(Map<String, ?> options) {
		this.init(QUERYNAME_DEFAULT, options);
	}
	/**
	 * initializes this parser, 
	 * look up properties: query by given queryname, {@link ScopeParser#init(Map)} init options
	 * @param queryName name of the query config parameter to lookup (value by) in given options map
	 * @param options map of config parameter    
	 */
	public void init(String queryName, Map<String, ?> options) {
		this.varQuery = (String) options.get(queryName);
		if(this.varQuery == null || this.varQuery.isEmpty())
			throw new IllegalArgumentException("illegal query, expecting non-empty string");
		this.uidParser = new ScopeParser();
		this.uidParser.init(options);
	}
	
	
	/**
	 * parses the given query with the given loginname (using the given default-scope and delimiter) 
	 * @param query the query with variables used to be parsed 
	 * @param loginName "username" to parse variable-values from which in turn are used in the query (replacing variable-names with their values)  
	 * @param defaultScope optional: fallback value when no scope found in given loginName 
	 * @param delimiter optional: delimiter used to parse (split) given loginname in its components
	 * @return the "prepared query" = the original query but variable-names replaced with "?" for usage in java.sql PreparedStatement
	 */
	public String parse(String loginName) {
		this.initVariableMap(loginName);
		this.initPositionMap();
		return this.prepQuery;
	}
	
	
	/**
	 * provides the mapping of here defined variable-names 
	 * and their associated actual values.
	 * @param loginName the original login-value the user entered as "user-name"
	 */
	private void initVariableMap(String loginName) {
		String[] splitted = this.uidParser.parse(loginName);
		this.varMap = new HashMap<String, String>(4);
		this.varMap.put("$loginname", loginName);
		this.varMap.put("$username", splitted[0]);
		this.varMap.put("$scope", splitted[1]);
		this.varMap.put("$delimiter", this.uidParser.getDelimiter());
		_log.debug("resolved variables: " + this.varMap);
	}
	
	
	/**
	 * provides a mapping of the defined/applicable variables 
	 * and their positions in the query (used for setting values in a prepared statement)
	 * @param variables the set of defined/applicable variables
	 * @see #getVariableMap(String)
	 */
	private void initPositionMap() {
		Set<String> variables = this.getVarMap().keySet();
		String query = this.getVarQuery();
		this.posMap = new HashMap<String, Set<Integer>>();
		
		int pos = 1; 	// starts with 1 to match statement.set-value "indices" (also starting with 1)
		for(Iterator<String> iter=variables.iterator() ; iter.hasNext() ; ) {
			String var = iter.next();
			Set<Integer> positions = new HashSet<Integer>();
			int end = query.length() -1;
			for(int last=0 ; last >= 0 || last > end; last++) {
				last = query.indexOf(var, last);
				if(last < 0)
					break;
				positions.add(new Integer(pos++));	// increment only when actually found & added
			}
			// map only variables actually used
			if(positions.isEmpty())	// variable not used
				continue;
			this.posMap.put(var, positions);
			// replace variable name from original query with ? 
			//	to represent a java.sql prepared-statement query 
			String regex = "\\" + var;
			query = query.replaceAll(regex, "?");
		}
		this.prepQuery = query;
		_log.debug("variable usage at: " + this.posMap);
	}
	
	
	/**
	 * sets the values of the given map of defined variables 
	 * to the given prepared statement 
	 * at the proper positions given by the position-map  
	 * @param stmt the prepared-statement to set the values into 
	 * @param varMap mapping of variable-names and their actual values
	 * @param posMap mapping of variable-names to their actual positions in the given statement (query)  
	 * @throws SQLException
	 */
	public void prepareValues(PreparedStatement stmt) 
	throws SQLException {
		// iterate all variables and set their values to their positions in the prepared statement 
		for(Iterator<String> iter0=this.posMap.keySet().iterator() ; iter0.hasNext() ; ) {
			String varName = iter0.next();
			String varVal = this.varMap.get(varName);
			// set value of current variable to all its associated positions 
			Set<Integer> varPositions = this.posMap.get(varName);
			if(varPositions == null)
				continue;
			for(Iterator<Integer> iter1=varPositions.iterator() ; iter1.hasNext() ; ) {
				stmt.setString(iter1.next().intValue(), varVal);
			}
		}
	}
	
	
	/**
	 * @return the varQuery
	 */
	public String getVarQuery() {
		return this.varQuery;
	}
	/**
	 * @return the prepQuery
	 */
	public String getPrepQuery() {
		return this.prepQuery;
	}
	/**
	 * @return the varMap
	 */
	public Map<String, String> getVarMap() {
		return this.varMap;
	}
	/**
	 * @return the posMap
	 */
	public Map<String, Set<Integer>> getPosMap() {
		return this.posMap;
	}
}
