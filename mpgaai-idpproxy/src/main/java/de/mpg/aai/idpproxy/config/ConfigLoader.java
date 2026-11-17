package de.mpg.aai.idpproxy.config;

import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.connector.ConnectorConfigLoader;


/**
 * loads the configuration(context) from the config files location
 */
public class ConfigLoader {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(ConfigLoader.class);
	
	
	/**
	 * reads in and load the configuration from the given config-context (providing the config-file location)
	 * @param configCtx config-context to load on (providing the config-file location)
	 * @return loaded/initialized actual configuration
	 */
	public void load(ConfigContext configCtx) throws ConfigException {
		log.debug("loading config from {}", configCtx.getLocation());
		
		LogConfigLoader logLoader = new LogConfigLoader();
		logLoader.load(configCtx);
		
		ConnectorConfigLoader connLoader = new ConnectorConfigLoader();
		connLoader.load(configCtx);
	}
}
