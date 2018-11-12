package de.mpg.aai.idpproxy.config.connector.auth.imp;

import java.security.cert.Certificate;

import de.mpg.aai.idpproxy.config.connector.auth.AuthFactory;


public abstract class Shib1AuthFactory extends AuthFactory{

	private Certificate	cert;
	private String		ssoHandler;
	private Boolean		sendWithRealm = new Boolean(false);
	
	
	public Certificate getCert() {
		return this.cert;
	}
	public void setCert(Certificate crt) {
		this.cert = crt;
	}
	
	
	public String getSSOHandler() {
		return this.ssoHandler;
	}
	public void setSSOHandler(String handler) {
		this.ssoHandler = handler;
	}
	
	
	public Boolean getSendWithRealm() {
		return this.sendWithRealm;
	}
	public void setSendWithRealm(Boolean val) {
		this.sendWithRealm = val;
	}
}
