package de.mpg.aai.idpproxy.config.connector.auth.imp;

import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.auth.imp.Shib1ApacheAuth;


public class Shib1ApacheAuthFactory extends Shib1AuthFactory {
	
	
	/** {@inheritDoc} */
	@Override
	protected BaseConnector internCreateNewInstance() {
		Shib1ApacheAuth auth = new Shib1ApacheAuth();
		auth.setSsoHandler(this.getSSOHandler());
		auth.setCert(this.getCert());
		return auth;
	}
}
