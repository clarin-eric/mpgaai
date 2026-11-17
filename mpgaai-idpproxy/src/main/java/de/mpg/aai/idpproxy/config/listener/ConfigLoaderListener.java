package de.mpg.aai.idpproxy.config.listener;


import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.config.ConfigException;

public class ConfigLoaderListener implements ServletContextListener {
	/**the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(ConfigLoaderListener.class);
	/** startuplistener config parameter name for: 
	 * location (URL) of the configuration DIRECTORY */
	public static final String		CONFIG_LOCATON 	= "IdPProxyConfigLocation";
	/** startuplistener config parameter name for: 
	 * identifier of this configuration set (only required if running multiple instances in same JVM
	 * @see ConfigContext#singletons */
	public static final String		CONFIG_ID 		= "IdPProxyConfigID";
	
	/**configuration context */
	private ConfigContext	configCtx;
	
	
	/**
	 * default constructor
	 */
	public ConfigLoaderListener() {
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		this.closeConfigContext(event.getServletContext());
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletCtx = event.getServletContext();
		this.initConfigContext(servletCtx);
	}
	
	
	/**
	 * initializes the configuration context and loads the config
	 * @param servletCtx the (web)applications servlet context 
	 */
	private void initConfigContext(ServletContext servletCtx) {
		String location = null;
		try {
			log.debug("init configuration context");
			// first check if already done  
			if(ConfigContext.getActive(servletCtx) != null) 
				throw new IllegalStateException("cannot initialize context because there is already a root application context present - " +
                     "check whether you have multiple ConfigContext* definitions in your web.xml");
			
			// lookup config-file location and init ConfigContext
			String paramName = CONFIG_LOCATON;
			location = servletCtx.getInitParameter(paramName);
			if(location == null) {
				location = ConfigContext.DEFAULT_CONF_LOCATION;
				log.debug("no config-location found as init-parameter {}, fallback to {}", paramName, location);
			} else
				log.debug("found config-location from init-parameter {}: {}", paramName, location);
			
			URL locURL = location.startsWith("/")
				? servletCtx.getResource(location)
				: new URL(location);
			
			// optional: configuration ID: used to distinguish and segregate different configs in multiple parallel installations    
			paramName = CONFIG_ID;
			String configID = servletCtx.getInitParameter(paramName);
			ConfigContext ctx = new ConfigContext();
			ctx.init(locURL, configID, servletCtx);
			this.configCtx = ctx;	// set member only after successful init (no Exception)
		} catch(ConfigException cE) {
			log.error("failed to initialize configuration context: {}", cE.getMessage());
			throw cE;
		} catch(MalformedURLException muE) {
			throw new ConfigException("invalid configuration file location " + location, muE);
		}
	}
	
	
	/**
	 * closes the  configuration context 
	 * and removes it (as attribute) from the given servlet context 
	 * @param servletCtx
	 */
	private void closeConfigContext(ServletContext servletCtx) {
		if(this.configCtx != null) {
			this.configCtx.close(servletCtx);
			this.configCtx = null;
		}
	}
}
