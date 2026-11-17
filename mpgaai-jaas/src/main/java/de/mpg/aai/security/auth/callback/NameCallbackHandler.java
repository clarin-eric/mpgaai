package de.mpg.aai.security.auth.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class NameCallbackHandler implements CallbackHandler {
	/** the user name ("uid") to login */
	private String	username;
	
	
	/**
	 * constructor, initializes username to provide later on callback 
	 * @param uid the username
	 */
	public NameCallbackHandler(String uid) {
		this.username = uid;
	}
	
	
	/**
	 * supported callbacks:  NameCallback
	 * {@inheritDoc}
	 */
	@Override
	public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        if (callbacks == null || callbacks.length == 0) {
            return;
        }
        for (Callback cb : callbacks) {
            if(cb instanceof NameCallback) {
                ((NameCallback) cb).setName(this.username);
                return;
            }
            throw new UnsupportedCallbackException(cb, "only NameCallback is supported");
        }
	}
	
	
	/**
	 * @return the username
	 */
	protected String getUsername() {
		return this.username;
	}

}
