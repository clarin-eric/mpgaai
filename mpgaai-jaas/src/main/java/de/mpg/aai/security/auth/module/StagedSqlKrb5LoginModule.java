package de.mpg.aai.security.auth.module;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import com.sun.security.auth.module.Krb5LoginModule;

import de.mpg.aai.security.auth.callback.InterstageSqlCallbackHandler;

/**
 * Extension to Sun's {@link Krb5LoginModule} 
 * which simply wraps the given initial CallbackHandler
 * into an {@link InterstageSqlCallbackHandler}
 * with the goal to retrieve a sql-mapped actual-username from the given login-name
 * which then in turn is used for the actual authentication,
 * 
 * <p>
 * this resolving is all done by this "tiered" {@link InterstageSqlCallbackHandler}
 * (otherwise this would lead to repeated NameCallbacks during {@link #login()})
 * </p>
 * 
 * <p><b>NOTE:</b><br />
 * this used callbackhandler tries to resolve via an sql query to a database. 
 * therefore it - and so this LoginHandler - needs to have the database- &amp; query-options configured!<br /> 
 * for details of the parameter please refer to {@link InterstageSqlCallbackHandler} 
 * </p>
 * 
 * @author	last modified by $Author$, created by megger
 * @version	$Revision$
 */
public class StagedSqlKrb5LoginModule extends Krb5LoginModule /*implements LoginModule*/ {
	
	
	/**
	 * default constructor
	 */
	public StagedSqlKrb5LoginModule() {
	}
	
	
	/**
	 * wraps given callbackHandler into an InterstageSqlCallbackHandler
	 * an then proceeds it to/with this method's super-class' implementation
	 * @see com.sun.security.auth.module.Krb5LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
	 */
	@SuppressWarnings({"unchecked"})
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String,?>  sharedState, Map<String,?>  options) {
		try {
			CallbackHandler interstageCbH = new InterstageSqlCallbackHandler(callbackHandler, (Map<String, String>) options);
			super.initialize(subject, interstageCbH, sharedState, options);
		} catch(ClassCastException ccE) {
			throw new IllegalArgumentException("options map of type Map<String, String> exptected");
		}
	}
}
