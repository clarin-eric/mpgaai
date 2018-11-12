package de.mpg.aai.idpproxy.connector;

import java.util.List;

import javax.naming.directory.Attribute;

import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.config.connector.ConnectorConfig;
import de.mpg.aai.idpproxy.connector.data.BaseData;
import de.mpg.aai.idpproxy.connector.data.DataContext;


public class DataConnector {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(DataConnector.class);
	/** the connector configuration */
	private ConnectorConfig connConfig;
	
	
	/**
	 * constructor, initializes the current configuration context
	 * @param cfg the current config context (config VO) 
	 */
	public DataConnector(ConnectorConfig cfg) {
		this.connConfig = cfg;
	}
	
	
	public List<Attribute> loadAttribute(String remoteUserUn) throws ConnectorException, ConfigException {
		ConnectorLoader loader = new ConnectorLoader(this.connConfig);
		String remoteUser;
		String suffix;
		if(remoteUserUn.indexOf("@") == -1) {
			remoteUser = remoteUserUn;
			suffix = this.connConfig.getDefaultConnector();
		} else {
			String []nameTeile = remoteUserUn.split("@");
			remoteUser = nameTeile[0];
			suffix = nameTeile[1];
		}
		log.debug("load DataConnector for realm {}", suffix);
		BaseData data = loader.loadDataConnector(suffix);
		log.debug("loaded DataConntector {}", data.getClass().toString());
		DataContext dataContext = new DataContext(remoteUser,suffix);
		List<Attribute> attr = data.loadAttributes(dataContext);
		return attr;
	}
}
