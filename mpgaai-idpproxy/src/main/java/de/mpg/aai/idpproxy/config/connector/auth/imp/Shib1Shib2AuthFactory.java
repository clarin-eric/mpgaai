package de.mpg.aai.idpproxy.config.connector.auth.imp;

import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.auth.imp.Shib1Shib2Auth;


public class Shib1Shib2AuthFactory extends Shib1TomcatAuthFactory{
	private String authHandler;
	
	
	public String getAuthHandler() {
		return this.authHandler;
	}
	
	
	public void setAuthHandler(String handler) {
		this.authHandler = handler;
	}
	
	
	/** {@inheritDoc} */
	@Override
	protected BaseConnector internCreateNewInstance() {
		Shib1Shib2Auth auth = new de.mpg.aai.idpproxy.connector.auth.imp.Shib1Shib2Auth();
		auth.setAuthHandler(this.authHandler);
		auth.setSendWithRealm(this.getSendWithRealm());
		auth.setSsoHandler(this.getSSOHandler());
		auth.setCert(this.getCert());
		return auth;
	}
}
