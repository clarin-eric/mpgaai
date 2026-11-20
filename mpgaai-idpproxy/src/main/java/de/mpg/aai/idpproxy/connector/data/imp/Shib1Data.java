package de.mpg.aai.idpproxy.connector.data.imp;

import java.io.StringReader;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAttribute;
import org.opensaml.SAMLAttributeQuery;
import org.opensaml.SAMLAttributeStatement;
import org.opensaml.SAMLBinding;
import org.opensaml.SAMLBindingFactory;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLRequest;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLSubject;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.data.BaseData;
import de.mpg.aai.idpproxy.connector.data.DataContext;
import de.mpg.aai.idpproxy.connector.decoder.imp.Shib1RemoteUser;
import de.mpg.aai.idpproxy.connector.pool.PoolDataManager;
import de.mpg.aai.idpproxy.saml.SamlNamespaceContext;


public class Shib1Data extends BaseData{
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(Shib1Data.class);
	
	private String aaHandler;
	
	
	public String getAaHandler() {
		return this.aaHandler;
	}
	public void setAaHandler(String handler) {
		this.aaHandler = handler;
	}
	
	
	/**
	 *  provides a radom requestID
	 */
	private String getRequestId() {
		Random random = new Random();
		byte []bytes = new byte[16];
		random.nextBytes(bytes);
		String result = toHexString(bytes);
		return "_"+result.toLowerCase();
	}
	
	
	private BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i=0 ; i<bytes.length*8 ; i++) {
            if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }
	
	
	private String toHexString(byte[] bytes) {
		BitSet bits = fromByteArray(bytes);
		StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bits.length() / 4 * 4; i += 4) {
        	int wert = (bits.get(i) ? 1 : 0) * 8 + (bits.get(i + 1) ? 1 : 0)
        		* 4 + (bits.get(i + 2) ? 1 : 0) * 2
        		+ (bits.get(i + 3) ? 1 : 0);
        	if(wert < 10)
        		sb.append((char) (48 + wert));
        	else
            	sb.append((char) (65 + wert - 10));

        }
        return sb.toString();
	}
	
	
	/** {@inheritDoc} */
	@Override
	public List<Attribute> loadAttributes(DataContext context) throws ConnectorException {
		try {
			log.debug("loading attributes...");
			ConfigContext configCtx = this.getConfigContext();
			
			// load remote user from datapool
			PoolDataManager poolDataManager = PoolDataManager.getInstance();
			Shib1RemoteUser decodeShib1RemoteUser = (Shib1RemoteUser)poolDataManager.searchPrincipal(context.getRemoteUser(),context.getRealm());
			
			// create attribute requestes
			SAMLRequest request = new SAMLRequest();
			request.setId(getRequestId());
			SAMLAttributeQuery attributeQuerry = new SAMLAttributeQuery();

			// set ID of the proxy
			attributeQuerry.setResource(configCtx.getSPProviderId());
			request.setQuery(attributeQuerry);
			SAMLSubject samlSubject = new SAMLSubject();
			attributeQuerry.setSubject(samlSubject);
			
			// set NameIdentifiers
			SAMLNameIdentifier samlNameIdentifier = new SAMLNameIdentifier();
			samlSubject.setNameIdentifier(samlNameIdentifier);
			samlNameIdentifier.setFormat(decodeShib1RemoteUser.getNameFormat());
			samlNameIdentifier.setName(decodeShib1RemoteUser.getNameIdentifier());
			if(decodeShib1RemoteUser.getNameQualifier() != null 
					&& !decodeShib1RemoteUser.getNameQualifier().equals("null")) {
				samlNameIdentifier.setNameQualifier(decodeShib1RemoteUser.getNameQualifier());
			}
			SAMLBinding binding = SAMLBindingFactory.getInstance(SAMLBinding.SOAP);
			
			Collection<Certificate> certs = new ArrayList<Certificate>();
			certs.add(configCtx.getMyCert());
			request.sign("http://www.w3.org/2000/09/xmldsig#rsa-sha1", configCtx.getMyKey(), certs);
			
			// send request
			SAMLResponse response = binding.send(this.aaHandler.trim(),request);
			log.debug("Attribute Response:" +response);
			if(response == null)
				return null;
		
			Iterator<?> iter = response.getAssertions();
			if(!iter.hasNext())
				return null;
			SAMLAssertion responseAssertion = (SAMLAssertion)iter.next();
			iter = responseAssertion.getStatements();
			if(!iter.hasNext())
				return null;
			SAMLAttributeStatement attributeStatement = (SAMLAttributeStatement)iter.next();
			log.debug("Attribute Statement: "+attributeStatement.toString());
			List<Attribute> attributeList = new ArrayList<Attribute>();
			
			// read attributes
			iter = attributeStatement.getAttributes();
			while(iter.hasNext()) {
				SAMLAttribute samlAttributes = (SAMLAttribute)iter.next();
				String attributeName = samlAttributes.getName();
				Iterator<String> attributesValue = samlAttributes.getValues();
				String name = attributeName.substring(attributeName.lastIndexOf(":")+1);
				Attribute attribute = new BasicAttribute(name);
				while(attributesValue.hasNext()) {
					String value = attributesValue.next();
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					// consider namespace (saml:)
					dbf.setNamespaceAware(true);
					DocumentBuilder db = dbf.newDocumentBuilder();
					// parse SAML attribute and its values
					Document xmlDom = db.parse(new InputSource(new StringReader(samlAttributes.toString())));
					XPath xpath = XPathFactory.newInstance().newXPath();
					// consider Namespace (saml:) in XPath 
					NamespaceContext nsc = new SamlNamespaceContext();
					xpath.setNamespaceContext(nsc);
					
					// in case read scope of the current attribute values
					String scope = xpath.evaluate("string(//saml:AttributeValue[text()='"+value+"']/@Scope)", xmlDom);
					
					// read NameID Element (for eduPersonTargetedId)
					if(name.equals("eduPersonTargetedID")) {
						String nameQualifier = 
							xpath.evaluate("string(/saml:Attribute/saml:AttributeValue[1]/*[namespace-uri()='urn:oasis:names:tc:SAML:2.0:assertion' and local-name()='NameID'][1]/@NameQualifier)", xmlDom);
						String sPNameQualifier = 
							xpath.evaluate("string(/saml:Attribute/saml:AttributeValue[1]/*[namespace-uri()='urn:oasis:names:tc:SAML:2.0:assertion' and local-name()='NameID'][1]/@SPNameQualifier)", xmlDom);
						String nameid = 
							xpath.evaluate("string(/saml:Attribute/saml:AttributeValue[1]/*[namespace-uri()='urn:oasis:names:tc:SAML:2.0:assertion' and local-name()='NameID'][1])", xmlDom);
						value = nameQualifier + "!" + sPNameQualifier + "!" + nameid;
					}
					
					// if scope was there -> append -> to allow "prescoped" attribute to be proccessed properly in IdP
					if(scope != "")
						attribute.add(value+"@"+scope);
					else
						attribute.add(value);
					
					log.debug("AttributeID: "+attribute.getID()+" Value: "+value);
				}
				attributeList.add(attribute);
			}
			return attributeList;
		} catch(Exception eE) {
			throw new ConnectorException(eE);
		}
	}
}
