package de.mpg.aai.idpproxy.config.connector.auth.imp;

import org.apache.xml.security.utils.Base64;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import sun.security.x509.X509CertImpl;
import de.mpg.aai.idpproxy.config.connector.auth.AuthFactory;
import de.mpg.aai.idpproxy.config.connector.auth.AuthParser;


public abstract class Shib1AuthParser extends AuthParser {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(Shib1AuthParser.class);
	
	
	protected AuthFactory doParse(Shib1AuthFactory factory, Element element) {
		// TODO: consistent case-syntax for all tags! (not some upper- others lower-case...; see "cert" below)  
		String SSOHandler = element.getElementsByTagName("SSOHandler").item(0).getTextContent();
		factory.setSSOHandler(SSOHandler.trim());
		
		Element sendWithRealmElement = (Element)element.getElementsByTagName("SendWithRealm").item(0);
		if(sendWithRealmElement != null)
			factory.setSendWithRealm(new Boolean(sendWithRealmElement.getTextContent()));
		
		String certString = element.getElementsByTagName("cert").item(0).getTextContent();
		if(certString != null) {
			certString = certString.trim();
			X509CertImpl cert;
			try {
				if(certString.contains("BEGIN CERTIFICATE")) {
					certString = certString.substring(certString.indexOf("\n"));
				}
				if(certString.contains("END CERTIFICATE")) {
					certString = certString.substring(0,certString.lastIndexOf("\n"));
				}
				cert = new X509CertImpl(Base64.decode(certString));
				factory.setCert(cert);
			} catch(Exception e) {
				// FIXME why not failfast? throw E instead, no sense to proceed if configured cert load fails 
				log.error("Fehler beim laden des Certifikates "+e.getLocalizedMessage());
			}
		}
		return factory;
	}
}
