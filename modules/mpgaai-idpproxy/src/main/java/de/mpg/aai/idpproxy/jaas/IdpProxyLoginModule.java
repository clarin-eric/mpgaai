package de.mpg.aai.idpproxy.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.connector.AuthConnector;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;
import de.mpg.aai.security.auth.model.BasePrincipal;

/**
 * simple {@link LoginModule} implementation for idp-proxy authentication 
 * @author megger
 */
public class IdpProxyLoginModule implements LoginModule {
	/** identity container */
	private Subject			subject;
	/** actual user to authenticate */
	private Principal		user;
	/** handler providing user credentials */
	private CallbackHandler	handler;
	/** identifier, used to identify the proper config target:
	 * distinguish and segregate different configs in multiple parallel installations */
	private String			id;
	
	
	/**
	 * default constructor
	 */
	public IdpProxyLoginModule() {
	}
	
	
	/**
	 * @param configID the identifier, used to load proper config target
	 */
	private void setId(Map<String, ?> options) {
		this.id = options != null 
			? (String) options.get("id") 
			: null;
	}
	/**
	 * @return this instance's (used config-) ID
	 */
	private String getId() {
		return this.id;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void initialize(Subject subj, CallbackHandler callbackHandler, 
	@SuppressWarnings("unused") Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subj;
		this.handler = callbackHandler;
		this.setId(options);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean login() throws LoginException {
		this.user = null;
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
		String loginName = ((NameCallback) callbacks[0]).getName();
		if(loginName == null || loginName.trim().isEmpty()) {
			throw new AccountException("no login name provided");
		}
		char[] provided = ((PasswordCallback) callbacks[1]).getPassword();
		String providedPw = provided != null ? new String(provided) : null;
		
		try {
			ConfigContext configCtx = ConfigContext.getActive(this.getId());
			AuthConnector connector = new AuthConnector(configCtx.getConn());
			AuthResponse response = connector.authenticate(loginName, providedPw);
			this.user = new BasePrincipal(response.getRemoteUser());
		} catch(ConnectorException cE) {
			LoginException lE = new LoginException(cE.getMessage());
			lE.initCause(cE);
			throw lE;
		}
		return true;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean commit() /*throws LoginException*/ {
		if(this.isLoginSucceeded())
			this.subject.getPrincipals().add(this.user);
		else 
			this.clearState();
		return true;
	}
	
	
	/** {@inheritDoc} */
//	@Override
	public boolean logout() throws LoginException {
		// remove principals added by this LoginModule
		if(this.isLoginSucceeded()) {
			if(this.subject.isReadOnly())
				throw new LoginException ("Subject is read-only");
			this.subject.getPrincipals().remove(this.user);
		}
//		this.clearState();
		return true;
	}
	
	
	/** {@inheritDoc} */
//	@Override
	public boolean abort() /*throws LoginException*/ {
		if(this.isLoginSucceeded())
			this.clearState();
		return true;
	}
	
	
	/**
	 * resets the current status: {@link #user} and {@link #loginSucceeded}
	 */
	private void clearState() {
		this.subject = null;
		this.handler = null;
		this.user = null;
	}
	
	
	/**
	 * @return true if a (logged in) user exists
	 */
	private boolean isLoginSucceeded() {
		return this.user != null;
	}
}
