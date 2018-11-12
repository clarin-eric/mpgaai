package de.mpg.aai.idpproxy.saml;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


public class SamlNamespaceContext implements NamespaceContext {
	
	
	/** {@inheritDoc} */
	@Override
	public String getNamespaceURI(String prefix) {
		return prefix.equals("saml")
			? "urn:oasis:names:tc:SAML:1.0:assertion"
			: XMLConstants.NULL_NS_URI;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String getPrefix(String namespace) {
		return namespace.equals("urn:oasis:names:tc:SAML:1.0:assertion")
			? "saml"
			: null;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public Iterator<?> getPrefixes(@SuppressWarnings("unused") String namespace) {
		return null;
	}
}
