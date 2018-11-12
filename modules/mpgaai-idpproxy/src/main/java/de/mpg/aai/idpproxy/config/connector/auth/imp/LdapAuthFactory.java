package de.mpg.aai.idpproxy.config.connector.auth.imp;

import de.mpg.aai.idpproxy.config.connector.auth.AuthFactory;
import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.auth.imp.LdapAuth;


public class LdapAuthFactory extends AuthFactory{
	private String	ldapUrl;
	private String	ldapAdmin;
	private String	ldapPassword;
	
	
	public void setLdapUrl(String url) {
		this.ldapUrl = url;
	}
	public void setLdapAdmin(String admin) {
		this.ldapAdmin = admin;
	}
	public void setLdapPassword(String password) {
		this.ldapPassword = password;
	}
	
	
	/** {@inheritDoc} */
	@Override
	protected BaseConnector internCreateNewInstance() {
		LdapAuth auth = new LdapAuth();
		auth.setLDAPUrl(this.ldapUrl);
		auth.setLDAPAdmin(this.ldapAdmin);
		auth.setLdapPassword(this.ldapPassword);
		return auth;
	}
}
