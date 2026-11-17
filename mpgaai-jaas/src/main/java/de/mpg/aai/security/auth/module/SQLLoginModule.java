package de.mpg.aai.security.auth.module;

import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import de.mpg.aai.security.auth.model.BaseGroup;
import de.mpg.aai.security.auth.model.BasePrincipal;
import de.mpg.aai.security.auth.util.DbConnector;
import de.mpg.aai.security.auth.util.LoginQueryParser;


/**
 * A login module that loads security information from a SQL database.  Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 * <p/>
 * This requires database connectivity information (either 1: a dataSourceName and
 * optional dataSourceApplication or 2: a JDBC driver, URL, username, and password)
 * and 2 SQL queries.
 * <p/>
 * The userQuery query should return 2 values, the username and the password in
 * that order.  It should include one PreparedStatement parameter (a ?) which
 * will be filled in with the username.  In other words, the query should look
 * like: <tt>SELECT user, password FROM credentials WHERE username=?</tt>
 * <p/>
 * The groupQuery query should return 2 values, the username and the group name in
 * that order (but it may return multiple rows, one per group).  It should include
 * one PreparedStatement parameter (a ?) which will be filled in with the username.
 * In other words, the query should look like:
 * <tt>SELECT user, role FROM user_roles WHERE username=?</tt>
 * <p/>
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 * 
 * @author megger
 */
public class SQLLoginModule extends AbstractDigestModule implements LoginModule {
	
	/** holds db-connection parameter */
	private DbConnector	db; 
	/** holds and parses user-login-query */
	private LoginQueryParser userQueryParser;
	/** holds and parses the user's group/roles-query */
	private LoginQueryParser groupQueryParser;
	
	private boolean			loginSucceeded;
	private Subject			subject;
	private CallbackHandler	handler;
	
	private String					loginName;
	private final Set<String>		groupNames = new HashSet<String>();
	private final Set<Principal>	allPrincipals = new HashSet<Principal>();
	
	
	
	/**
	 * default constructor
	 */
	public SQLLoginModule() {
	}
	
	
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(Subject subj, CallbackHandler callbackHandler, @SuppressWarnings("unused") Map<String,?>  sharedState, Map<String,?>  options) {
		this.subject = subj;
		this.handler = callbackHandler;
		try {
			this.db = new DbConnector((Map<String, String>) options);
		} catch (ConfigurationException cE) {
			throw new IllegalArgumentException("illegal db-connection configuration parameter", cE);
		}
		this.userQueryParser = new LoginQueryParser();
		this.userQueryParser.init(LoginQueryParser.QUERYNAME_USER, options);
		if(options.get(LoginQueryParser.QUERYNAME_GROUP) != null) {
			this.groupQueryParser = new LoginQueryParser();
			this.groupQueryParser.init(LoginQueryParser.QUERYNAME_GROUP, options);
		}
		this.init(options);
	}
	
	
	/**
	 * This LoginModule is not to be ignored.  
	 * So, this method should never return false.
	 * @return true if authentication succeeds, 
	 * or throw a LoginException such as FailedLoginException if authentication fails
	 */
	@Override
	public boolean login() throws LoginException {
		this.loginSucceeded = false;
		// callback loginname & passwd
		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("Login Name");
		callbacks[1] = new PasswordCallback("Password", false);
		try {
			this.handler.handle(callbacks);
		} catch(IOException ioe) {
			throw(LoginException) new LoginException().initCause(ioe);
		} catch(UnsupportedCallbackException uce) {
			throw(LoginException) new LoginException().initCause(uce);
		}
		this.loginName = ((NameCallback) callbacks[0]).getName();
		if(this.loginName == null || this.loginName.trim().isEmpty()) {
			throw new AccountException("no login name");
		}
		char[] provided = ((PasswordCallback) callbacks[1]).getPassword();
		String providePw = provided != null ? new String(provided) : null;
		
		// query data & check pw
		try {
			Connection conn = this.db.connect();
			try {
				// USER-QUERY
				String query = this.userQueryParser.parse(this.loginName);
				PreparedStatement statement = conn.prepareStatement(query);
				try {
					this.userQueryParser.prepareValues(statement);
					ResultSet result = statement.executeQuery();
					try {
						boolean found = false;
						while(result.next()) {
							String userName = result.getString(1);
							String realPw = result.getString(2);
							if(!this.loginName.equals(userName))
								continue;
							if(!this.check(realPw, providePw))
								throw new FailedLoginException();
							found = true;
							break;
						}
						if(!found)
							throw new AccountNotFoundException();							
					} finally {
						result.close();
					}
				} finally {
					statement.close();
				}
				
				// GROUP-QUERY
				if(this.groupQueryParser != null) {
					query = this.groupQueryParser.parse(this.loginName);
					statement = conn.prepareStatement(query);
					try {
						this.groupQueryParser.prepareValues(statement);
						ResultSet result = statement.executeQuery();
						try {
							while(result.next()) {
								String userName = result.getString(1);
								String groupName = result.getString(2);
								if(!this.loginName.equals(userName))
									continue;
								this.groupNames.add(groupName);
							}
						} finally {
							result.close();
						}
					} finally {
						statement.close();
					}
				}
			} finally {
				conn.close();
			}
		} catch(LoginException lE) {
			this.clearState(false);
			throw lE;
		} catch(SQLException sqlE) {
			this.clearState(false);
			throw(LoginException) new LoginException("SQL error")
				.initCause(sqlE);
		} catch(Exception eE) {
			this.clearState(false);
			throw (LoginException) new LoginException("Could not access datasource")
				.initCause(eE);
		}
		this.loginSucceeded = true;
		return this.loginSucceeded;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean commit() /*throws LoginException*/ {
		if(this.loginSucceeded) {
			if(this.loginName != null) {
				this.allPrincipals.add(new BasePrincipal(this.loginName));
			}
			for(String group : this.groupNames) {
				this.allPrincipals.add(new BaseGroup(group));
			}
			this.subject.getPrincipals().addAll(this.allPrincipals);
		}
		this.clearState(false);
		return this.loginSucceeded;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean abort() /*throws LoginException*/ {
		if(this.loginSucceeded)
			this.clearState(true);
		this.loginSucceeded = false;
		return true;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean logout() throws LoginException {
		// clear out the private state
		this.clearState(true);
		this.loginSucceeded = false;
		// remove principals added by this LoginModule
		if(this.allPrincipals != null && !this.allPrincipals.isEmpty()) {
			if(this.subject.isReadOnly())
				throw new LoginException ("Subject is read-only");
			this.subject.getPrincipals().removeAll(this.allPrincipals);
		}
		return true;
	}
	
	
	/**
	 * resets the current state
	 * @param all flag to indicate whether to delete all collected principals, if false just groups are cleared
	 */
	private void clearState(boolean all) {
		this.loginName = null;
		this.groupNames.clear();
		if(all)
			this.allPrincipals.clear();
	}
}
