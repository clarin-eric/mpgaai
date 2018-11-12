package de.mpg.aai.idpproxy;

import java.util.List;

import javax.naming.directory.Attribute;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.ConnectorLoader;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;
import de.mpg.aai.idpproxy.connector.auth.BaseAuth;
import de.mpg.aai.idpproxy.connector.auth.imp.Shib1TomcatAuth;
import de.mpg.aai.idpproxy.connector.data.BaseData;
import de.mpg.aai.idpproxy.connector.data.DataContext;
import de.mpg.aai.idpproxy.connector.data.imp.Shib1Data;

public class Shib1TomcatTest extends TestBase {
	
	
	public void testSuccess() throws ConnectorException, ConfigException
	{
		String remoteUser = "tgrusch@gwdg.tomcat.de";
		String password  = "shibboleth_2007";

		ConfigContext context = this.getConfigContext();
		ConnectorLoader loader = new ConnectorLoader(context.getConn());
		
		
		//Zerteilen des Remote User Strings
		String []nameTeile1 = remoteUser.split("@");
		//Der eigendlich RemoteName
		remoteUser = nameTeile1[0];
		//Praefix zur namensaufl�sung
		String suffix = nameTeile1[1];
			
		assertEquals("Falsche decodieren", suffix, "gwdg.tomcat.de");
			
		BaseAuth auth = loader.loadAuthConnector(suffix);
			
		assertTrue("Falsche Classe geladen", auth instanceof Shib1TomcatAuth);
			
			
		AuthContext authContext = new AuthContext();
		authContext.setUserName(remoteUser);
		authContext.setPassword(password);
		authContext.setRealm(suffix);
		AuthResponse response = auth.login(authContext);
			
		assertNotNull(response.getRemoteUser());

			
		response.setRemoteUser(response.getRemoteUser()+"@"+suffix);
		
		//Zerteilen des Remote User Strings
		String []nameTeile2 = response.getRemoteUser().split("@");
		//Der eigendlich RemoteName
		remoteUser = nameTeile2[0];
		//Praefix zur namensaufl�sung
		suffix = nameTeile2[1];
			
			
		BaseData data = loader.loadDataConnector(suffix);
			
		assertTrue("Falscher Connector", data instanceof Shib1Data);
			
		DataContext dataContext = new DataContext(remoteUser,suffix);
			
		List<Attribute> attr = data.loadAttributes(dataContext);
			
		assertEquals("Falsche anzahl von attributen", attr.size(), 3);
			
	}
}
