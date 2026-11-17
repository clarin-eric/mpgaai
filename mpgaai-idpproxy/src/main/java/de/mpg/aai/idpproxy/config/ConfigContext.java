package de.mpg.aai.idpproxy.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.connector.ConnectorConfig;


public class ConfigContext {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(ConfigContext.class);
	
	/** default location/name of the configuration file */
	public static final String	DEFAULT_CONF_LOCATION 	= "/etc/idp-proxy";
	/** the logger config file name */
	private static final String	LOG_CONFNAME			= "log.xml";
	/** the connector config file name */
	private static final String	CONNECTOR_CONFNAME		= "connectors.xml";
	/** 
	 * name/identifier to put/get the loaded configurationContext 
	 * into/from the servletContext and the (config) singletons-map
	 * @see #id 
	 */
	private static final String	DEFAULT_ID	= "idpproxy";
	/** singleton map to segregate & keep different configs for multiple parallel installations;
	 * for details please refer to special comment inside {@link #reload(ServletContext)} */
	private static Map<String, ConfigContext>	singletons;
	/** 
	 * name/identifier from this instance 
	 * to put/get this configurationContext into/from the servletContext and the (config) singletons-map
	 * @see #DEFAULT_ID 
	 */
	private String	id;
	/** location/url of the configuration file */
	private URL		location;
	
	/** the idpproxy's certificate private key */
	private PrivateKey		myKey;
	/** the idpproxy's certificate/public key */
	private Certificate		myCert;
	/** the connectors config VO */
	private ConnectorConfig	conn;
	
	/** service provider target */
	private String	SPTarget;
	/** the proxy's shib-1.3 service provider shire url 
	 * (the precurser of the later shib2-SP discovery-response (then in metadata) url)  */
	private String	SPShire;
	/** the proxy's "mimic" service provider ID */
	private String	SPProviderId;
	

	/**
	 * constructor, initializes ID
	 */
	public ConfigContext() {
	}
	
	
	/**
	 * @return {@link #id}
	 */
	public String getID() {
		return this.id;
	}
	
	/**
	 * formats the given value to provide the "real", means actually internally used, config-id 
	 * @param configID the id (part) to format and generate the internally used value 
	 * @return config-context-class-name with given config-id, all lowercase
	 */
	private static String toID(String configID) {
		String prefix = ConfigContext.class.getName().toLowerCase();
		StringBuffer result = new StringBuffer(prefix);
		result.append("-");
		if(configID == null) {
			result.append(DEFAULT_ID);	// lowercase!
			return result.toString();
		}
		String id = configID.toLowerCase();
		if(id.startsWith(result.toString()))	// is already properly prefixed
			return id;
		result.append(id);
		return result.toString();
	}
	
	private void setID(String configID) {
		this.id = toID(configID);
	}
	
	
	/**
	 * provides the current/active configuration context (expected/looked-up in the given Servletcontext)
	 * @param servletCtx the (web)applications servlet context
	 * @return the ConfigContext, null if not found
	 */
	public static ConfigContext getActive(ServletContext servletCtx) {
		// using default ID for webapp scope <=> compare to #reload(ServletContext) and #close(..)
		return (ConfigContext) servletCtx.getAttribute(toID(DEFAULT_ID));
	}
	
	public static ConfigContext getActive(String configID) {
		ConfigContext result = null;
		String key = toID(configID);
		if(singletons == null) {
			log.warn("configContext singletons-map is (still) null. config seems to be not initialized yet, try #init(..)");
			return null;
		}
		result = singletons.get(key);
		if(result != null)
			return result;
		// catch standard setup without specific config-ID
		if(singletons.size() == 1) {
			result = singletons.get(singletons.keySet().iterator().next());
			log.debug("only one configContext in singletons-map: providing configContext with id {} while asked for {}",
					new Object[] {result.getID(), key});
			return result;
		}
		log.error("no configContext found with ID {}, providing null!", configID);
		return null; 
	}
	
	/**
	 * provides the configuration file location (url) 
	 * @return configuration file location (url)
	 */
	public URL getLocation() {
		return this.location;
	}
	public URL getLogConfLocation() throws MalformedURLException {
		return this.getLocation(LOG_CONFNAME);
	}
	public URL getConnectorConfLocation() throws MalformedURLException {
		return this.getLocation(CONNECTOR_CONFNAME);
	}
	private URL getLocation(String filename) throws MalformedURLException {
		if(this.location == null)
			throw new NullPointerException("no base location url found for file " + filename);
		
		return new URL(this.location + File.separator + filename);
	}
	
	
	/**
	 * sets the configuration file location (url) 
	 * and invokes (re)loading of the configuration
	 * @param url location to/of the configuration file
	 * @param configID identifier of this config (required only when multiple instances run in same jvm) 
	 * @param servletCtx the applications ServletContext 
	 * (used to hold/provide current config(context) once ("statically") application wide)
	 * @see #reload() 
	 * @throws ConfigException
	 */
	public void init(URL url, String configID, ServletContext servletCtx) {
		this.setID(configID);
		this.location = url;
		this.reload(servletCtx);
	}
	/**
	 * sets the configuration file location (url) 
	 * and invokes (re)loading of the configuration
	 * @param url location to/of the configuration file
	 * @param servletCtx the applications ServletContext 
	 * (used to hold/provide current config(context) once ("statically") application wide)
	 * @see #reload() 
	 * @throws ConfigException
	 */
	public void init(URL url, ServletContext servletCtx) {
		this.init(url, DEFAULT_ID, servletCtx);
	}
	
	
	/**
	 * (re)loads the configuration from the config file at {@link #location}
	 * AND initializes hereafter the services (authn/z handler)
	 * @throws ConfigException
	 */
	public void reload(ServletContext servletCtx) {
		log.debug("(re)loading configuration, from location {}", this.location);
		if(this.location == null)
			throw new ConfigException("no config file location specified - call init(URL/String) first");
		
		// special: singletons-map
		// config(context) has to be available from webapp external logic: 
		// 	like for jaas or (tomcat-) realm login
		//	<=> there is NO servletContext available (and no way to add it without mod. 3rd-party code...) 
		// => solution: 
		// create a singleton MAP with config-location as key 
		// (basic singleton also not possible since we need load a DIFFERENT config for each running webapp)
		if(singletons == null)
			singletons = new HashMap<String, ConfigContext>();
		else
			singletons.remove(this.getID());	// cleanup old

		ConfigLoader confLoader = new ConfigLoader();
		confLoader.load(this);
		
		// using default ID for webapp scope <=> compare to #getActive(ServletContext) and #close(..)
		if(servletCtx != null)
			servletCtx.setAttribute(toID(DEFAULT_ID), this);
		else
			log.warn("no servletContext given, could NOT put current config into servletContext");
		
		// put to config singletons store 
		singletons.put(this.getID(), this);
	}
	
	
	/** "closes" this configuration(context): 
	 * removes config this container instance from given servletContext and internal singleton-map
	 * @param the servlet context of this webapp to remove this config container from  
	 */
	public void close(ServletContext servletCtx) {
		log.debug("closing configuration context {}", this.getID());
		// using default ID for webapp scope <=> compare to #getActive(ServletContext) and #reload(..)
		if(servletCtx != null)
			servletCtx.removeAttribute(toID(DEFAULT_ID));
		if(singletons == null)
			return;
		singletons.remove(this.getID());
	}
	
	
	public ConnectorConfig getConn() {
		return this.conn;
	}
	public void setConn(ConnectorConfig cfg) {
		this.conn = cfg;
	}
	
	
	public PrivateKey getMyKey() {
		return this.myKey;
	}
	public void setMyKey(PrivateKey key) {
		this.myKey = key;
	}
	
	
	public Certificate getMyCert() {
		return this.myCert;
	}
	public void setMyCert(Certificate crt) {
		this.myCert = crt;
	}
	
	
	public String getSPTarget() {
		return this.SPTarget;
	}
	public void setSPTarget(String target) {
		this.SPTarget = target;
	}


	public String getSPShire() {
		return this.SPShire;
	}
	public void setSPShire(String shire) {
		this.SPShire = shire;
	}


	public String getSPProviderId() {
		return this.SPProviderId;
	}
	public void setSPProviderId(String providerId) {
		this.SPProviderId = providerId;
	}
}
