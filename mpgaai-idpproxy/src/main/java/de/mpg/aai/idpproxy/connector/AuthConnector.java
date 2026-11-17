package de.mpg.aai.idpproxy.connector;

import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.config.connector.ConnectorConfig;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;
import de.mpg.aai.idpproxy.connector.auth.BaseAuth;


public class AuthConnector {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(AuthConnector.class);
	
	/** the connector configuration */
	private ConnectorConfig connConfig;
	
	
	/**
	 * constuctor, initializes config-context 
	 * @param cfg the current config-context (config VO) 
	 */
	public AuthConnector(ConnectorConfig cfg) {
		this.connConfig = cfg;
	}
	
	
	/**
	 * authenticates given username with given credential 
	 * @param username the username
	 * @param credentials the password
	 * @return AuthResponse
	 * @throws ConnectorException
	 */
	public AuthResponse authenticate(String username, String credentials) throws ConnectorException {
		try {
			ConnectorLoader loader = new ConnectorLoader(this.connConfig);
			log.debug("try authenticate user '{}'",  username);
			String remoteUser = null;
			String realm = null;
			if(username.indexOf("@") == -1) {
				log.info("no realm in username, using default realm");
				realm = this.connConfig.getDefaultConnector();
				remoteUser = username;
			} else {
				String []nameTeile = username.split("@");
				remoteUser = nameTeile[0];
				realm = nameTeile[1];
			}
			log.debug("using realm ({})", realm);
			BaseAuth auth = loader.loadAuthConnector(realm);
			log.debug("using/loaded auth class "+auth.getClass());
			AuthContext authContext = new AuthContext();
			authContext.setUserName(remoteUser);
			authContext.setPassword(credentials);
			authContext.setRealm(realm);
			AuthResponse response = auth.login(authContext);
			log.debug("login succeeded for remoteUser {}", response.getRemoteUser());
			response.setRemoteUser(response.getRemoteUser()+"@"+realm);
			return response;
		} catch(ConfigException cE) {
			throw new ConnectorException("failed during authenticate due to config error", cE);
		}
	}
}
