package de.mpg.aai.security.auth.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;


/**
 * simple {@link CallbackHandler} implementation providing user credentials information: username & password 
 * note: his handler only supports {@link NameCallback} and {@link PasswordCallback}.
 * @author megger
 */
public class CredentialCallbackHandler extends NameCallbackHandler /*implements CallbackHandler*/ {
	/** the user password to authenticate */
	private String	passwd;
	
	/**
	 * constructor, initializes user credentials: username & password
	 * @param username 
	 * @param passwd
	 */
	public CredentialCallbackHandler(String uid, String pw) {
		super(uid);
		this.passwd = pw;
	}
	
	/**
	 * supported callbacks:  NameCallback, PasswordCallback
	 * {@inheritDoc}
	 */
	@Override
	public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        if (callbacks == null || callbacks.length == 0) {
            return;
        }
        for (Callback cb : callbacks) {
            if(cb instanceof NameCallback) {
                ((NameCallback) cb).setName(this.getUsername());
                return;
            }
            if(cb instanceof PasswordCallback) {
                ((PasswordCallback) cb).setPassword(this.passwd.toCharArray());
                return;
            }
            throw new UnsupportedCallbackException(cb, "only NameCallback & PasswordCallback are supported");
        }
	}
}
