package de.mpg.aai.idpproxy;

import java.util.List;

import javax.naming.directory.Attribute;

import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.config.connector.ConnectorConfig;
import de.mpg.aai.idpproxy.connector.AuthConnector;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.DataConnector;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;

public class DataPoolTest extends TestBase {

	public void testDataManager() throws ConnectorException, ConnectorException, ConfigException
	{
		ConnectorConfig cfg = this.getConfigContext().getConn();
		AuthConnector connectorAuth0 = new AuthConnector(cfg);
		AuthResponse response0 = connectorAuth0.authenticate("tgrusch@gwdg.tomcat.de", "shibboleth_2007");
		assertNotNull(response0.getRemoteUser());
		
		AuthConnector connectorAuth1 = new AuthConnector(cfg);
		AuthResponse response1 = connectorAuth1.authenticate("tgrusch@gwdg.apache.de", "shibboleth_2007");
		assertNotNull(response1.getRemoteUser());
		
		AuthConnector connectorAuth2 = new AuthConnector(cfg);
		AuthResponse response2 = connectorAuth2.authenticate("tgrusch@gwdg.shib2.tomcat.de", "shibboleth_2007");
		assertNotNull(response2.getRemoteUser());
		
		DataConnector connectorData = new DataConnector(cfg);
		List<Attribute> attrs2 = connectorData.loadAttribute(response2.getRemoteUser());
		
		assertTrue(attrs2.size() == 1);
		
		List<Attribute> attrs0 = connectorData.loadAttribute(response0.getRemoteUser());
		
		assertTrue(attrs0.size() == 3);
		
		List<Attribute> attrs1 = connectorData.loadAttribute(response1.getRemoteUser());
	
		assertTrue(attrs1.size() == 2);
		
		List<Attribute> attrs3 = connectorData.loadAttribute(response2.getRemoteUser());
		
		assertTrue(attrs3.size() == 1);
	
	}	
}
