package de.mpg.aai.idpproxy.config.connector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.ssl.PKCS8Key;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.config.ConfigException;


public class ConnectorConfigLoader {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(ConnectorConfigLoader.class);
	
	
	/** default constructor */
	public ConnectorConfigLoader() {
	}
	
	
	/**
	 * loads Auth- and Data-Factory from {@link #configFile}
	 * @param configCtx the current configuration context
	 * @throws ConfigException
	 */
	public void load(ConfigContext configCtx) throws ConfigException {
		try {
			URL location = configCtx.getConnectorConfLocation();
			log.debug("loading connectors config from {}", location.toString());
			DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFact.newDocumentBuilder();
			Document doc = docBuilder.parse(location.toString());
			log.debug("connectors config file parsed sucessfully");
			
			// start load data
			// basic SP data: "myData"
			Element element = doc.getDocumentElement();
			Element myElement = (Element)element.getElementsByTagName("myData").item(0);
			String SPTarget = myElement.getElementsByTagName("SPTarget").item(0).getTextContent();
			configCtx.setSPTarget(SPTarget.trim());
			String SPShire = myElement.getElementsByTagName("SPShire").item(0).getTextContent();
			configCtx.setSPShire(SPShire.trim());
			String SPProviderId = myElement.getElementsByTagName("SPProviderId").item(0).getTextContent();
			configCtx.setSPProviderId(SPProviderId.trim());
			
			// idpproxy certificate: private- & public-key
			this.loadMyCert(configCtx, myElement);
			
			// name tables
			ConnectorConfig connConfig = new ConnectorConfig();
			configCtx.setConn(connConfig);
			this.loadNameTables(element, connConfig);
			
			// XXX 
			org.apache.xml.security.Init.init();
			
			// auth connectors
			NodeList authConnNodeList = element.getElementsByTagName("Auth-Connector");
			log.debug("found {} auth-connectors", String.valueOf(authConnNodeList.getLength()));
			List<BaseFactory> authFactoryList = connConfig.getAuthFactoryList();
			for(int ii = 0 ; ii < authConnNodeList.getLength() ; ii++) {
				Element authConn = (Element)authConnNodeList.item(ii);
				BaseFactory factory = createBaseFactory(authConn, configCtx);
				authFactoryList.add(factory);
			}
			
			// data connectors
			NodeList dataConnNodeList = element.getElementsByTagName("Data-Connector");
			List<BaseFactory> dataFactoryList = connConfig.getDataFactoryList();
			log.debug("found {} data-connectors", String.valueOf(dataConnNodeList.getLength()));
			for(int ii = 0 ; ii < dataConnNodeList.getLength() ; ii++) {
				Element dataConn = (Element)dataConnNodeList.item(ii);
				BaseFactory factory = createBaseFactory(dataConn, configCtx);
				dataFactoryList.add(factory);
			}
		} catch(ConfigException cE) {	// don't wrap in itself
			throw cE;
		} catch(Exception eE) {
			throw new ConfigException("failed to load connector config", eE);
		}
	}
	
	
	/**
	 * provides the (fist) element (of all childs) with the given tag-name from the given parent element
	 * @param parent the element to lookup the data in
	 * @param tag name of the tag(s) to lookup
	 * @return Element representing the (first) found data
	 * @throws ConfigException if no result could be found
	 */
	private Element getElement(Element parent, String tag) {
		Element result = null;
		NodeList nodes = parent.getElementsByTagName(tag);
		if(nodes != null && nodes.getLength() >= 0)
			result = (Element) nodes.item(0);
		if(result == null)
				throw new ConfigException("could not find configuration tag \"" + tag 
						+ "\" as child of " + parent.getTagName());
		return  result;
	}
	
	private void loadMyCert(ConfigContext configCtx, Element parent) {
		log.debug("loading idpproxy credential");
		Element credentials = this.getElement(parent, "Credential");
		String path = null;
		// load private key
		try {
    		Element keyTag = this.getElement(credentials, "PrivateKey");
    		path = keyTag.getTextContent();
            FileInputStream ins = new FileInputStream(path);
            byte[] encoded = new byte[ins.available()];
            ins.read(encoded);
            ins.close();
	        PKCS8Key deocodedKey = new PKCS8Key(encoded, null);
	        PrivateKey key = deocodedKey.getPrivateKey();
	        configCtx.setMyKey(key);
        } catch(IOException ioE) {
        	throw new ConfigException("could not read private key " + path, ioE);
        } catch (GeneralSecurityException gsE) {
        	throw new ConfigException("failed to import private key " + path, gsE);
		}
		// load "certificate" / public key
		try {
			Element certTag = this.getElement(credentials, "Certificate");
			path = certTag.getTextContent();
			InputStream in = new FileInputStream(path);
			CertificateFactory certFac = CertificateFactory.getInstance("X.509");
			Certificate cert = certFac.generateCertificate(in);
			configCtx.setMyCert(cert);
		} catch(FileNotFoundException fnfE) {
        	throw new ConfigException("could not read certificate " + path, fnfE);
		} catch (CertificateException cE) {
			throw new ConfigException("failed to import certificate " + path, cE);
		}
	}
	
	
	/**
	 * List die namen aus die als Suffix eingegeben werden aus der konfig
	 * so das diese aufgel�st werden k�nnen
	 * load the mapping of allowed username suffixes and their associated authn- and data-connector IDs 
	 * @param element
	 * @param connConfig
	 */
	private void loadNameTables(Element element,ConnectorConfig connConfig) {
		List<NameTable> nameTableList = connConfig.getNameTableList();
		NodeList NameTableNodeList = element.getElementsByTagName("NameTable");
		
		for(int ii = 0 ; ii < NameTableNodeList.getLength() ; ii++) {
			Element NameTable = (Element)NameTableNodeList.item(ii);
			NodeList NameNodeList = NameTable.getElementsByTagName("Name");
			NameTable nameTable = new NameTable(NameTable.getAttribute("name"));
			
			for(int nn = 0 ; nn < NameNodeList.getLength() ; nn++) {
				Element Name = (Element)NameNodeList.item(nn);
				nameTable.getNames().add(Name.getTextContent());
				if(NameTable.getAttribute("default").toLowerCase().equals("true"))
					connConfig.setDefaultConnector(Name.getTextContent());
			}
			nameTableList.add(nameTable);
		}
	}
	
	
	/**
	 * creates the BaseFactory
	 * @param element
	 * @param connConfig
	 * @return resulting BaseFactory
	 * @throws ConfigException
	 */
	private BaseFactory createBaseFactory(Element element, ConfigContext configCtx) throws ConfigException {
		try {
			String ParserClass = element.getAttribute("class");
			String FactoryName = element.getAttribute("name");
			String FactoryNameTable = element.getAttribute("nameTable");
			List<NameTable> nameTableList = configCtx.getConn().getNameTableList();
			NameTable nameTable = null;
			for(int ii = 0 ; ii < nameTableList.size() ; ii++) {
				if(nameTableList.get(ii).getName().equals(FactoryNameTable)) {
					nameTable = nameTableList.get(ii);
					break;
				}
			}
			if(nameTable == null)
				throw new ConfigException("no matching NameTable found");
			Class<?> parserClass = Class.forName(ParserClass);
			BaseParser parser = (BaseParser) parserClass.getConstructor().newInstance();
			BaseFactory factory = parser.doParse(element);
			factory.setConfigContext(configCtx);
			factory.setName(FactoryName);
			factory.setNameTable(nameTable);
			return factory;
		} catch(Exception eE) {
			throw new ConfigException("could not create BaseFactory", eE);
		}
	}
}
