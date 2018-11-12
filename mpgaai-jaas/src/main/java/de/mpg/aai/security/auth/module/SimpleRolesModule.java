package de.mpg.aai.security.auth.module;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.mpg.aai.security.auth.model.BaseGroup;


/**
 * simple implementation of {@link LoginModule}
 * which simply always adds the (jaas) configured roles ({@link #mRoles}) to the subject 
 * 
 * @author matthias egger
 *
 */
public class SimpleRolesModule implements LoginModule {
	/** the logger */
	protected static Log	_log = LogFactory.getLog(SimpleRolesModule.class);
	/** the subject to be authenticated */
	private Subject			mSubject;
	/** the subject's roles to be authorized by */
	private Set<Principal>	mRoles;
		
	
	/**
	 * default constructor
	 */
	public SimpleRolesModule() {
	}
	
	
	/** {@inheritDoc} */
	public boolean abort() throws LoginException {
		return true;
	}
	
	
	/** {@inheritDoc} */
	public boolean commit() throws LoginException {
		return this.mSubject.getPrincipals().addAll(this.mRoles);
	}
	
	
	/**
	 * expects property "roles": commas separated list of destined role(name)s 
	 * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
	 */
	public void initialize(Subject subject, 
			@SuppressWarnings("unused") CallbackHandler callbackHandler, 
			@SuppressWarnings("unused")Map<String,?>  sharedState, 
			Map<String,?>  options) {
		_log.debug("initializing...");
		this.mSubject = subject;
		String roles = options != null 
			? (String) options.get("roles")
			: null;
		this.initRoles(roles);
	}
	
	
	/** {@inheritDoc} */
	public boolean login() throws LoginException {
		return true;
	}
	
	
	/** {@inheritDoc} */
	public boolean logout() throws LoginException {
		if(this.mRoles != null)
			this.mSubject.getPrincipals().removeAll(this.mRoles);
		return true;
	}
	
	
	/**
	 * initializes the roles by the given (list of) role names
	 * @param roleNames name(s) of assigned roles, can be a comma-separated string 
	 * @return Set of roles 
	 */
	private Set<Principal> initRoles(String roleNames) {
		_log.debug("initializing roles with values " + roleNames);
		String roles = null; 
		if(roleNames == null || roleNames.trim().length() <= 0) {
			roles= "role";
			_log.info("fallback to default roles: " + roles);
		} else {
			roles = roleNames;
		}
		this.mRoles = new HashSet<Principal>();
		String[] names = roles.split("[,;|]");
		for(int ii=0 ; ii < names.length ; ii++) {
			String name = names[ii].trim();
			if(name == null || name.length() <= 0) {
				_log.debug("ommitting \"empty\" role(name)");
				continue; 
			}
			Principal role = new BaseGroup(name);
			this.mRoles.add(role);
		}
		return this.mRoles;
	}
}
