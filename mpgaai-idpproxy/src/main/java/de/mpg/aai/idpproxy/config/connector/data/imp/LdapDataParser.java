package de.mpg.aai.idpproxy.config.connector.data.imp;

import org.w3c.dom.Element;

import de.mpg.aai.idpproxy.config.connector.data.DataFactory;
import de.mpg.aai.idpproxy.config.connector.data.DataParser;


public class LdapDataParser extends DataParser {
	
	
	/** {@inheritDoc} */
	@Override
	public DataFactory doParse(Element element) {
		LdapDataFactory factory = new LdapDataFactory();
		String LDAPURL = element.getElementsByTagName("LDAPURL").item(0).getTextContent();
		factory.setLdapUrl(LDAPURL.trim());
		String LDAPAdmin = element.getElementsByTagName("LDAPAdmin").item(0).getTextContent();
		factory.setLdapAdmin(LDAPAdmin.trim());
		String LDAPPassword = element.getElementsByTagName("LDAPPassword").item(0).getTextContent();
		factory.setLdapPassword(LDAPPassword.trim());
		return factory;
	}
}
