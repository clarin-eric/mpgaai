package de.mpg.aai.security.auth.util;

import java.util.Map;


/**
 * simple util class to split scoped values into its components;
 * 
 * provides static methods for quick usage,
 * instances can be used to parse repeatedly with similar default-scope (and delimiter-) values
 * 
 * @author	last modified by $Author$, created by megger
 * @version	$Revision$
 */
public class ScopeParser {
	/** default delimiter to parse/split the given scoped-value by */
	public static final String	DEFAULT_DELIMITER =		"@";
	/** delimiter to parse/split the given scoped-value by */
	private String	delimiter		= DEFAULT_DELIMITER;
	/** fallback scope used/provided when no scope found during parsing */
	private String	defaultScope	= "defaultScope";
	
	
	/**
	 * default constructor
	 */
	public ScopeParser() {
	}
	/**
	 * constructur, initializes this parser with given options, see {@link #init(Map)}
	 * @param options  Map providing "defaultScope" and "delimiter" values
	 * @see #init(Map)
	 */
	public ScopeParser(Map<String, ?> options) {
		this.init(options);
	}
	
	/**
	 * initializes default-scope and delimiter 
	 * @param options Map providing "defaultScope" and "delimiter" values 
	 */
	public void init(Map<String, ?> options) {
		String scope = (String) options.get("defaultScope");
		if(scope != null)
			this.setDefaultScope(scope);
		String delim = (String) options.get("delimiter");
		if(delim != null)
			this.setDelimiter(delim);
	}
	
	
	/**
	 * parses the given target and splits it 
	 * by the default-delimiter ({@link #DEFAULT_DELIMITER}) 
	 * into its value- and {@link #defaultScope} component
	 * @param target the value to be parsed/split  
	 * @return String[2] of signature [unscoped-value][scope]
	 */
	public String[] parse(String target) {
		String[] result = parse(target, this.defaultScope, this.delimiter);
		return result;
	}
	
	
	/**
	 * parses the given target and splits it 
	 * by the default-delimiter ({@link #DEFAULT_DELIMITER}) 
	 * into its value- and scope-components
	 * @param target the value to be parsed/split  
	 * @param defaultScope default value for scope if none was found in the given target
	 * @return String[2] of signature [unscoped-value][scope]
	 */
//	public static String[] parse(String val, String defaultScope) {
//		return parse(val, defaultScope, DEFAULT_DELIMITER);
//	}
	
	
	/**
	 * parses the given target and splits it
	 * by the given delimiter 
	 * into its value- and scope-parts
	 * @param target the value to be parsed/split  
	 * @param defaultScope default value for scope if none was found in the given target
	 * @param delimiter delimiter to use to parse/split the given target
	 * @return String[2] of signature [unscoped-value][scope]
	 */
	public static String[] parse(String target, String defaultScope, String delimiter) {
		if(delimiter == null || delimiter.isEmpty()) {
			return new String[]{target, defaultScope};
		}
		int idx = target.indexOf(delimiter);		// -1 if not found
		if(idx < 0)	// no delimiter => no scope
			return new String[]{target, defaultScope};
		// else: split up by delimiter
		String[] result = new String[2];
		result[0] = target.substring(0, idx++);
		result[1] = idx < target.length()
			? target.substring(idx)
			: defaultScope;	// delimiter is last char => no scope
		return result;
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return this.delimiter;
	}
	/**
	 * @param delim the delimiter to set
	 */
	public void setDelimiter(String delim) {
		this.delimiter = delim;
	}

	/**
	 * @return the defaultScope
	 */
	public String getDefaultScope() {
		return this.defaultScope;
	}
	/**
	 * @param scope the defaultScope to set
	 */
	public void setDefaultScope(String scope) {
		this.defaultScope = scope;
	}
}
