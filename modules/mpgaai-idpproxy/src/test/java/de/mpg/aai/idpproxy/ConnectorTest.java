package de.mpg.aai.idpproxy;

import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.config.connector.ConnectorConfig;
import de.mpg.aai.idpproxy.connector.AuthConnector;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.DataConnector;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;

public class ConnectorTest extends TestBase {
	
	
	public void testTomcatSuccess() throws ConnectorException, ConfigException
	{
		ConnectorConfig cfg = this.getConfigContext().getConn();
		AuthConnector connectorAuth = new AuthConnector(cfg);
		AuthResponse response = connectorAuth.authenticate("tgrusch@gwdg.tomcat.de", "shibboleth_2007");
		assertNotNull(response.getRemoteUser());
		
		DataConnector connectorData = new DataConnector(cfg);
		connectorData.loadAttribute(response.getRemoteUser());
	}
	

	
	public void testApacheSuccess() throws ConnectorException, ConfigException
	{
		ConnectorConfig cfg = this.getConfigContext().getConn();
		AuthConnector connectorAuth = new AuthConnector(cfg);
		AuthResponse response = connectorAuth.authenticate("tgrusch@gwdg.apache.de", "shibboleth_2007");
		assertNotNull(response.getRemoteUser());
		
		DataConnector connectorData = new DataConnector(cfg);
		connectorData.loadAttribute(response.getRemoteUser());
	}
	

	
	public void testLdapSuccess() throws ConnectorException, ConfigException
	{
		ConnectorConfig cfg = this.getConfigContext().getConn();
		AuthConnector connectorAuth = new AuthConnector(cfg);
		AuthResponse response = connectorAuth.authenticate("thajek@ldap.de", "moritzhajek");
		assertNotNull(response.getRemoteUser());
		
		DataConnector connectorData = new DataConnector(cfg);
		connectorData.loadAttribute(response.getRemoteUser());
	}
}
