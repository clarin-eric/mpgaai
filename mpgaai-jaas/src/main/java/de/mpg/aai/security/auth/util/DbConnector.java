package de.mpg.aai.security.auth.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple bean holding db-connection configuration parameter 
 * and providing the (java.sql)Connection 
 *
 * @author	last modified by $Author$, created by megger
 * @version	$Revision$
 */
public class DbConnector {
	/** the logger */
	private static Logger	_log = LoggerFactory.getLogger(DbConnector.class);

	/** name of the jndi datasource to acquire database-connection from */
	private String	jndiName		= null;
	/** jdbc driver name to be used*/
	private String  jdbcDriver;
	/** the database url */
	private String	jdbcUrl			= null;
	/** the database-user */
	private String	jdbcUser		= null;
	/** password of the database-user */
	private String	jdbcPassword	= null;
	
	
	/**
	 * default constructor
	 */
	public DbConnector() {
	}
	
	public DbConnector(Map<String,String>  options) throws ConfigurationException {
		this.init(options);
	}
	
	
	/**
	 * initializes member variables by the given options
	 * @param options options-map to initialize this instance's configuration.
	 * parameter are: 
	 * <ul>
	 * <li>jndiName</li>
	 * <li>jdbcDriver</li>
	 * <li>jdbcUser</li>
	 * <li>jdbcPassword</li>
	 * </ul>
	 */
	public void init(Map<String,String>  options) throws ConfigurationException {
		this.setJndiName(options.get("jndiName"));
		this.setJdbcDriver(options.get("jdbcDriver"));
		this.setJdbcUrl(options.get("jdbcUrl"));
		this.setJdbcUser(options.get("jdbcUser"));
		this.setJdbcPassword(options.get("jdbcPassword"));
		
		if(!this.isEmpty(this.jdbcUrl)) {
			if(this.isEmpty(this.jdbcDriver))
				throw new ConfigurationException("no 'jdbcDriver' configured");
			if(this.isEmpty(this.jdbcUser))
				_log.warn("no 'jdbcUser' configured");
			if(this.isEmpty(this.jdbcPassword))
				_log.warn("no 'jdbcPassword' configured");
		} else if(this.isEmpty(this.jndiName))
			throw new ConfigurationException("no database connection parameter configured, expecting one of 'jndiName'|'jdbcDriver'");
	}
	
	
	/**
	 * provides the configured database connection
	 * @return Connection 
	 * @throws NamingException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Connection connect() throws NamingException, SQLException, ClassNotFoundException {
		Connection result = null;
		String jndi = this.getJndiName();
		// jdbc mode
		if(this.isEmpty(jndi)) {
			String url = this.getJdbcUrl();
			_log.debug("connecting (jdbc) to db " + url);
			Class.forName(this.jdbcDriver);	// load driver first
			result = DriverManager.getConnection(url, this.getJdbcUser(), this.getJdbcPassword());
		} else {
			// jndi-mode: get connection from datasource 
			_log.debug("connecting (jndi) to db datasource" + jndi);
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(jndi);
			result = ds.getConnection();
		}
		_log.debug("connected to db " + result.getMetaData().getURL());
		return result;
	}
	
	
	/**
	 * little helper to identify empty-or-null strings
	 * @param val the string to be checked
	 * @return true if given string is null or empty or represents whitespaces only
	 */
	private boolean isEmpty(String val) {
		return val == null || val.trim().isEmpty();
	}

	
	/**
	 * @return the jndiName
	 */
	public String getJndiName() {
		return this.jndiName;
	}
	/**
	 * @param jndiName the jndiName to set
	 */
	public void setJndiName(String jndiname) {
		this.jndiName = jndiname;
	}
	
	
	/**
	 * @return the jdbcDriver
	 */
	public String getJdbcDriver() {
		return this.jdbcDriver;
	}
	/**
	 * @param jdbcDriver the jdbcDriver to set
	 */
	public void setJdbcDriver(String driver) {
		this.jdbcDriver = driver;
	}
	
	
	/**
	 * @return the jdbcUrl
	 */
	public String getJdbcUrl() {
		return this.jdbcUrl;
	}
	/**
	 * @param jdbcUrl the jdbcUrl to set
	 */
	public void setJdbcUrl(String url) {
		this.jdbcUrl = url;
	}
	
	
	/**
	 * @return the jdbcUser
	 */
	public String getJdbcUser() {
		return this.jdbcUser;
	}
	/**
	 * @param jdbcUser the jdbcUser to set
	 */
	public void setJdbcUser(String jdbcuser) {
		this.jdbcUser = jdbcuser;
	}
	
	
	/**
	 * @return the jdbcPassword
	 */
	public String getJdbcPassword() {
		return this.jdbcPassword;
	}
	/**
	 * @param jdbcPassword the jdbcPassword to set
	 */
	public void setJdbcPassword(String jdbcpw) {
		this.jdbcPassword = jdbcpw;
	}
	
}
