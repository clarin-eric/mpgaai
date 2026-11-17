package de.mpg.aai.idpproxy.config.connector.auth.imp;

import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.auth.imp.Shib1TomcatAuth;


public class Shib1TomcatAuthFactory extends Shib1AuthFactory {
	
	
	/** {@inheritDoc} */
	@Override
	protected BaseConnector internCreateNewInstance() {
		Shib1TomcatAuth auth = new Shib1TomcatAuth();
		auth.setSsoHandler(this.getSSOHandler());
		auth.setCert(this.getCert());
		auth.setSendWithRealm(this.getSendWithRealm());
		return auth;
	}
}
