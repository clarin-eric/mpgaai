package de.mpg.aai.idpproxy.config.connector.auth.imp;


import org.w3c.dom.Element;

import de.mpg.aai.idpproxy.config.connector.auth.AuthFactory;
import de.mpg.aai.idpproxy.config.connector.auth.AuthParser;

public class LdapAuthParser extends AuthParser{


	/** {@inheritDoc} */
	@Override
	public AuthFactory doParse(Element element) {
		LdapAuthFactory factory = new LdapAuthFactory();
		String LDAPURL = element.getElementsByTagName("LDAPURL").item(0).getTextContent();
		factory.setLdapUrl(LDAPURL.trim());
		String LDAPAdmin = element.getElementsByTagName("LDAPAdmin").item(0).getTextContent();
		factory.setLdapAdmin(LDAPAdmin.trim());
		String LDAPPassword = element.getElementsByTagName("LDAPPassword").item(0).getTextContent();
		factory.setLdapPassword(LDAPPassword.trim());
		return factory;
	}
}
