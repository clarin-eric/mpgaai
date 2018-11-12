package de.mpg.aai.idpproxy.connector;

import java.util.List;

import de.mpg.aai.idpproxy.config.connector.BaseFactory;
import de.mpg.aai.idpproxy.config.connector.ConnectorConfig;
import de.mpg.aai.idpproxy.config.connector.NameTable;
import de.mpg.aai.idpproxy.connector.auth.BaseAuth;
import de.mpg.aai.idpproxy.connector.data.BaseData;


/**
 * loads and instanciates the connectors from the config
 * @author thajek
 */
public class ConnectorLoader {
	private ConnectorConfig config;
	
	
	/**
	 * constructor, initializes the current configuration context
	 * @param cfg the current config context (config VO) 
	 */
	public ConnectorLoader(ConnectorConfig cfg) {
		this.config = cfg;
	}
	
	
	public BaseAuth loadAuthConnector(String suffix) throws ConnectorException {
		return  (BaseAuth)load(suffix, this.config.getAuthFactoryList());
	}
	
	
	public BaseData loadDataConnector(String suffix) throws ConnectorException {
		return (BaseData)load(suffix, this.config.getDataFactoryList());
	}
	
	
	private BaseConnector load(String suffix, List<BaseFactory> factoryList) throws ConnectorException {
		for(int x = 0 ; x < factoryList.size() ; x++) {
			BaseFactory base = factoryList.get(x);
			NameTable nameTable = base.getNameTable();
			if(nameTable.ContainsSuffix(suffix))
				return base.createInstance();
		}
		throw new ConnectorException("no mathcing factory found for realm " + suffix);
	}
}
