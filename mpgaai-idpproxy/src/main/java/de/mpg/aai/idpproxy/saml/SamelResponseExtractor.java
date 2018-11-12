package de.mpg.aai.idpproxy.saml;


import java.io.InputStream;
import java.security.cert.Certificate;

import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.NameIdentifier;
import de.mpg.aai.idpproxy.saml.imp.Samel1ResponseExtractor;

public abstract class SamelResponseExtractor {

	public static SamelResponseExtractor getInstance()
	{
		return new Samel1ResponseExtractor();
	}
	
	public abstract NameIdentifier extract(InputStream stream,Certificate cert) throws ConnectorException;

	
}
