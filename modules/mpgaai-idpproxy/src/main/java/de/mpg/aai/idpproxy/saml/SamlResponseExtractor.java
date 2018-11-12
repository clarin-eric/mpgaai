package de.mpg.aai.idpproxy.saml;

import java.io.InputStream;
import java.security.cert.Certificate;

import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.NameIdentifier;
import de.mpg.aai.idpproxy.saml.imp.Saml1ResponseExtractor;


public abstract class SamlResponseExtractor {
	
	
	public static SamlResponseExtractor getInstance() {
		return new Saml1ResponseExtractor();
	}
	
	/**
	 * todo
	 * @param stream
	 * @param cert
	 * @return
	 * @throws ConnectorException
	 */
	public abstract NameIdentifier extract(InputStream stream,Certificate cert) throws ConnectorException;
}
