package de.mpg.aai.idpproxy.config.connector;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.connector.BaseConnector;


public abstract class BaseFactory {

	private String			name;
	private NameTable		nameTable;
	/** the current configuration VO */
	private ConfigContext	configCtx;
	
	
	/**
	 * todo
	 * @return loaded connector instance
	 */
	protected abstract BaseConnector internCreateNewInstance();
	
	
	public String getName() {
		return this.name;
	}
	public void setName(String val) {
		this.name = val;
	}

	
	public NameTable getNameTable() {
		return this.nameTable;
	}
	public void setNameTable(NameTable val) {
		this.nameTable = val;
	}
	

	public BaseConnector createInstance() {
		BaseConnector connector = internCreateNewInstance();
		connector.setName(this.name);
		connector.setConfigContext(this.configCtx);
		return connector;
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
