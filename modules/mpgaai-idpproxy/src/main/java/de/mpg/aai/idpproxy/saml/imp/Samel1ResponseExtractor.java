package de.mpg.aai.idpproxy.saml.imp;



import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.InvalidCryptoException;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLResponse;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.NameIdentifier;
import de.mpg.aai.idpproxy.saml.SamelResponseExtractor;


public class Samel1ResponseExtractor extends SamelResponseExtractor{

	private static org.slf4j.Logger log = LoggerFactory.getLogger(Samel1ResponseExtractor.class);
	
	
	public NameIdentifier extract(InputStream stream,Certificate cert) throws ConnectorException {
		try {
			NameIdentifier nameIdentifier = new NameIdentifier();
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmlDom = db.parse(stream);
			XPath xpath = XPathFactory.newInstance().newXPath();
			String samlResponseValue = (String) xpath.evaluate("string(//input[@name='SAMLResponse']/@value)", xmlDom);
			if(samlResponseValue == "")
				throw new ConnectorException("could not extract SAMLResponse");
			log.debug("extracted SAMLResponse: {}", samlResponseValue);
	        InputStream inputStream = new ByteArrayInputStream(Base64.decodeBase64(samlResponseValue.getBytes()));
	        // parse SAML Assertion strings
	        SAMLResponse samlResponse = new SAMLResponse(inputStream);
			log.debug("parsed SAMLResponse: {}", samlResponse.toString());
	        if(cert == null)
	        	throw new ConnectorException("no certificate defined");
	        try {
		        samlResponse.verify(cert);
		        log.debug("Verify of Assertion was ok");
	        } catch(InvalidCryptoException icE) {
	        	log.error("Cannot Verify Assertion", icE);
	        	// FIXME proceed here ?!  -> shouldn't we just throw the icE?!
	        }
	        Iterator<SAMLAssertion> iterSAMLAssertion = samlResponse.getAssertions();
			
	        // extract NameIdentifiers
			while(iterSAMLAssertion.hasNext()) {
				SAMLAssertion samlAssertion = iterSAMLAssertion.next();
				Iterator<SAMLAuthenticationStatement>  iterSAMLStatement = samlAssertion.getStatements();
				while(iterSAMLStatement.hasNext()) {
					SAMLAuthenticationStatement samlAuthenticationStatement = iterSAMLStatement.next();
					SAMLNameIdentifier samlNameIdentifier = samlAuthenticationStatement.getSubject().getNameIdentifier();
					nameIdentifier.setFormat(samlNameIdentifier.getFormat());
					nameIdentifier.setName(samlNameIdentifier.getName());
					nameIdentifier.setQualifier(samlNameIdentifier.getNameQualifier());
				}
			}
			return nameIdentifier;
			//Login successful
		} catch(Exception e) {
			log.error(e.getMessage());
			throw new ConnectorException(e.getMessage(),e);
		}
	}
}
