package de.mpg.aai.idpproxy.connector;

import de.mpg.aai.idpproxy.config.ConfigContext;


public abstract class BaseConnector {

	private String			name;
	private ConfigContext	configCtx;
	
	
	public String getName() {
		return this.name;
	}
	public void setName(String theName) {
		this.name = theName;
	}
	
	
	/**
	 * @param cfg the current configuration VO
	 */
	public void setConfigContext(ConfigContext cfg) {
		this.configCtx = cfg;
	}
	/**
	 * @return the current configuration VO
	 */
	public ConfigContext getConfigContext() {
		return this.configCtx;
	}
}
