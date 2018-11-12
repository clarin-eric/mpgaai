package de.mpg.aai.idpproxy.connector.auth;

import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.ConnectorException;


public abstract class BaseAuth extends BaseConnector {
	/**
	 * todo
	 * @param context
	 * @return
	 * @throws ConnectorException
	 */
	public abstract AuthResponse login(AuthContext context) throws ConnectorException ;
}
