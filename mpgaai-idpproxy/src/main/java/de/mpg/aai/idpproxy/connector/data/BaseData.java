package de.mpg.aai.idpproxy.connector.data;

import java.util.List;

import javax.naming.directory.Attribute;

import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.ConnectorException;


public abstract class BaseData extends BaseConnector {
	
	
	/**
	 * todo
	 * @param context
	 * @return
	 * @throws ConnectorException
	 */
	public abstract List<Attribute> loadAttributes(DataContext context) throws ConnectorException ;
}
