package de.mpg.aai.idpproxy.connector;

import de.mpg.aai.security.auth.AuthException;

public class ConnectorException extends AuthException {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = 2053695317329788472L;


	/**
	 * default constructor
	 */
	public ConnectorException() {
	}
	/** {@inheritDoc} */
	public ConnectorException(String message) {
		super(message);
	}
	/** {@inheritDoc} */
	public ConnectorException(Throwable cause) {
		super(cause);
	}
	/** {@inheritDoc} */
	public ConnectorException(String message, Throwable cause) {
		super(message, cause);
	}
}
