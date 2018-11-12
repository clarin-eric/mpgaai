package de.mpg.aai.idpproxy.config.connector.data.imp;

import de.mpg.aai.idpproxy.config.connector.data.DataFactory;
import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.data.imp.LdapData;


public class LdapDataFactory extends DataFactory {

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
		LdapData data = new LdapData();
		data.setLdapUrl(this.ldapUrl);
		data.setLdapAdmin(this.ldapAdmin);
		data.setLdapPassword(this.ldapPassword);
		return data;
	}
}
