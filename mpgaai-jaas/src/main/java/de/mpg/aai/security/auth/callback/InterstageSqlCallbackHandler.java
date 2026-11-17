package de.mpg.aai.security.auth.callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.spi.LoginModule;

import de.mpg.aai.security.auth.util.DbConnector;
import de.mpg.aai.security.auth.util.LoginQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this implementation of a callbackhandler is a kind of interstage handler 
 * to a given underlying callbackhandler for authentication. 
 * it retrieves a loginname from the underlying handler 
 * and then resolves an "actual username" by this given loginname via a sql query.
 * 
 * <p>
 * intercepts for NameCallbacks only: 
 * each NameCallback to handle is first delegated to the underlying callbackhandler 
 * and then the "real username" is resolved by this retrieved loginname
 * and set to the given NamecallBack.
 * in other words: it overrides the NameCallback result/value from the underlying handler
 * with the resolved username.  
 * 
 * any other callbacks are delegated to the underlying handler and left unmodified.
 * 
 * since this is a specific callbackHandler for authentication,  
 * it is assumed that PasswordCallbacks are properly handled by the underlying handler.
 * </p>
 * 
 * <p>
 * <b>NOTE: as for the sql resolving: database connection parameter must be configured as options 
 * to the invoking {@link LoginModule}:</b>
 * <ul>
 * 	<li>jndiName : jndi datasource name to retrieve (pooled) database connection</li>
 * 	<li>jdbcUrl : database URL</li>
 * 	<li>jdbcUser : database user to connect to DB</li>
 * 	<li>jdbcPassword : database-user password (to connect to DB)</li>
 * 	<li>userQuery : sql query to resolve the real-username: for available variables see below</li>
 * 	<li>defaultScope : default security domain of the user-name</li>
 * 	<li>delimiter : (optional) value to separate user-identifier from the security domain in a scoped-user-ID, 
 * 		defaults to '@' if not explicitly configured</li>
 * </ul>
 * </p>
 * 
 * <p>
 * for the configurable (sql)userQuery for resolving 
 * there is a basic set of variables available 
 * one can use in the userQuery:
 * see {@link LoginQueryParser}
 * </p>
 * 
 * <p>example query:<br /> 
 * your application specifies users' email address as login-name 
 * but you still need to authenticate with the classical uid against krb5 in the background: <br />
 * you can configure the database connection and query to resolve the real username
 * by the entered email-address (as login-name).<br />
 * sample query using variables:<br /> 
 * <code>select username from user_table where email = $loginname</code>
 * </p>
 * 
 * <p>example LoginModule JAAS config:<br />
 * <pre>
 * ShibUserPassAuth {
 *	de.mpg.aai.security.auth.module.StagedSqlKrb5LoginModule required
 * 		java.security.krb5.realm="FOO.XYZ.ORG"
 * 		jdbcDriver="com.mysql.jdbc.Driver"
 * 		jdbcUrl="jdbc:mysql://dbserver.xyz.org/userdb"
 * 		jdbcUser="dbuser"
 * 		jdbcPassword="secret"
 * 		userQuery="select username from user_table where email = $loginname"
 * 		defaultScope=foo.xyz.org"
 *	;
 * };
 * </pre>
 * </p>
 */
public class InterstageSqlCallbackHandler implements CallbackHandler {
	/** the logger */
	private static Logger	_log = LoggerFactory.getLogger(InterstageSqlCallbackHandler.class);
	
	/** underlying callbackhandler to be delegated to */
	private CallbackHandler	underlyingCbHandler = null;
	/** the retrieved loginname */
	private String	loginName;
	/** resolved username */
	private String	resolvedName;
	/** holds db-connection parameter */
	private DbConnector	db;
	/** holds and parses user-login-query */
	private LoginQueryParser queryParser;
	
	
	/**
	 * constructor to initialize with the underlying callbackhandler
	 * @param underlyingCbH the underlying callbackhandler to use
	 */
	public InterstageSqlCallbackHandler(CallbackHandler underlyingCbH, Map<String, String>  options) {
		this.underlyingCbHandler = underlyingCbH;
		this.initOptions(options);
	}
	
	/**
	 * initializes member variables by the given options
	 * @param options options-map to initialize this instance's configuration
	 */
	private void initOptions(Map<String, String>  options)  {
		_log.trace("got config options: " + options);
		try {
			this.db = new DbConnector(options);
		} catch (ConfigurationException cE) {
			throw new IllegalArgumentException("illegal db-connection configuration parameter", cE);
		}
		this.queryParser = new LoginQueryParser();
		this.queryParser.init(LoginQueryParser.QUERYNAME_USER, options);
	}
	
	
	/**
	 * recognizes NameCallback and PasswordCallback 
	 * TextOutputCallback pass untreated
	 * other Callbacks will cause UnsupportedCallbackException
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	@Override
	public void handle(Callback[] callbacks) throws UnsupportedCallbackException, IOException {
		// first let underlying handler do its job
		// to keep the original intended behavior: 
		this.underlyingCbHandler.handle(callbacks);
		
		// now intercept and check the username/nameCallback
		for(int ii = 0 ; ii < callbacks.length ; ii++) {
			Callback callback = callbacks[ii];
			// handle nameCallback: resolve real username
			if(callback instanceof NameCallback)
				this.handleName((NameCallback) callback);
			// ignore all others callbacks
		}
	}
	
	
	/**
	 * handles the given NameCallback
	 * @param nameCb (Name)Callback to handle
	 * @throws IOException note: throws AccountNotFoundExceptions wrapped in IOExceptions: 
	 * 	if something within (sql) username resolving failed
	 * @see #resolve()
	 */
	private void handleName(NameCallback nameCb) throws IOException {
		try {
			// loginname	foundName	action
			// 	null		null		(sync: ncb.name->loginname), (resolve->null), reset: resolvedname->namecb
			//	null		val0		sync, resolve(val0), reset
			//	val0		null		sync, resolve->null, reset
			//	val0		val0		(sync), (resolve), reset
			//	val0		val1		sync, resolve(val1), reset
			if(this.syncLoginNames(nameCb.getName()))
				this.resolve();	// only (re-)resolve if really something changed
			this.syncResolvedNames(nameCb);
		} catch(AccountNotFoundException anfE) {
			// anfE would be OK, 
			//	but need to wrap it into an ioE to obey the CallbackHandler interface
			//	(ioE seems more appropriate then UnsupportedCallbackE)
			throw new IOException(anfE);
		}
	}
	
	
	/**
	 * sets loginName to given foundName
	 * @param foundName value to set as loginName
	 * @return true if actual modified, false if foundName equals loginName (or both null)
	 * @see #loginName
	 */
	private boolean syncLoginNames(String foundName) {
		// do nothing if nothing changed 
		if(foundName == null && this.loginName == null)
			return false;
		if(foundName.equals(this.loginName))
			return false;
		
		this.loginName = foundName;
		return true;
	}
	/**
	 * sets given nameCallback (name)value to resolvedName
	 * @param ncb the nameCallback to update
	 * @see #resolvedName 
	 */
	private void syncResolvedNames(final NameCallback ncb) {
		ncb.setName(this.resolvedName);
	}
	
	
	/**
	 * resolves the "real username" by the loginName" via a sql query 
	 * @throws AccountNotFoundException if no (valid) username could be found
	 * (or other E occur: wrapped into anfE)
	 */
	private void resolve() throws AccountNotFoundException {
		this.resolvedName = null;
		String name = this.loginName;
		// accept no empty(=invalid) loginname
		if(name == null || name.isEmpty())
			throw new AccountNotFoundException("null/empty loginname, nothing to resolve.");
		
		// ACTUAL RESOVING: 
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = this.db.connect();			
			String query = this.queryParser.parse(name);
			PreparedStatement prepStmt = connection.prepareStatement(query);
			this.queryParser.prepareValues(prepStmt);
			
			// get result
			_log.trace(prepStmt.toString());
			rs = prepStmt.executeQuery();
			while(rs.next()) {
				this.resolvedName = rs.getString(1);
			}
			
			// close resource
			rs.close();
			prepStmt.close();
			connection.close();
			
			if(this.resolvedName == null || this.resolvedName.isEmpty()) {
				this.resolvedName = null;	// in case whitespaces only
				throw new AccountNotFoundException("found no username for loginname " + name);
			}
			StringBuffer msg = new StringBuffer("resolved ");
			msg.append(name).append(" to ").append(this.resolvedName);
			_log.debug(msg.toString());
		} catch(Exception eE) {
			StringBuffer msg = new StringBuffer("failed to resolve username for ");
			msg.append(name).append(": ").append(eE.getMessage());
			throw new AccountNotFoundException(msg.toString());
		}
	}
}
