/**
 * 
 */
package de.mpg.aai.security.auth.module;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import de.mpg.aai.security.auth.model.BasePrincipal;

/**
 * simple LoginModule "for administrators" to login as mimic on behalf of other user(account)s:
 * takes an encrypted string as config parameter as password for successful authentication
 * @author megger
 *
 */
public class MimicLoginModule extends AbstractDigestModule implements LoginModule {
	/** identity container */
	private Subject			subject;
	/** actual user to authenticate */
	private Principal		user;
	/** handler providing user credentials */
	private CallbackHandler	handler;
	/** configured mimic pw */
	private String			secret;
	
	
	/**
	 * default constructor
	 */
	public MimicLoginModule() {
	}
	
	
	/** {@inheritDoc} */
	@SuppressWarnings("unused")
	@Override
	public void initialize(Subject subj, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subj;
		this.handler = callbackHandler;
		this.init(options);
		String val = (String) options.get("secret");
		this.secret = val != null ? val.trim() : null;
		if(this.secret == null || this.secret.isEmpty())
			throw new IllegalArgumentException("secret must not be null/empty");
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
		if(!this.check(this.secret, providedPw))
			throw new FailedLoginException();
		
		this.user = new BasePrincipal(loginName);
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
	@Override
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

	/**
	 * encodes with given digest algorithm and given encoding the given secret:
	 * @param args [digest, encoding, secret] 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException {
		MimicLoginModule mod = new MimicLoginModule();
		mod.setDigest(args[0]);
		mod.setEncoding(args[1]);
		String result = mod.encode(args[2]);
		System.out.println(result);
	}
}