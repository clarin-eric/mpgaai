package de.mpg.aai.idpproxy.config;

import de.mpg.aai.security.auth.AuthException;


public class ConfigException  extends AuthException {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = 8075246003127682649L;
	
	
	/**
	 * default constructor
	 */
	public ConfigException() {
	}
	/** {@inheritDoc} */
	public ConfigException(String message) {
		super(message);
	}
	/** {@inheritDoc} */
	public ConfigException(Throwable cause) {
		super(cause);
	}
	/** {@inheritDoc} */
	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}
}
