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
import de.mpg.aai.idpproxy.connector.auth.imp.Shib1ApacheAuth;
import de.mpg.aai.idpproxy.connector.data.BaseData;
import de.mpg.aai.idpproxy.connector.data.DataContext;
import de.mpg.aai.idpproxy.connector.data.imp.Shib1Data;

public class Shib1ApacheTest extends TestBase {
	
	
	public void testSuccess() throws ConnectorException, ConfigException
	{
		String remoteUser = "tgrusch@gwdg.apache.de";
		String password  = "shibboleth_2007";

		ConfigContext context = this.getConfigContext();
		ConnectorLoader loader = new ConnectorLoader(context.getConn());
		
		
		//Zerteilen des Remote User Strings
		String []nameTeile1 = remoteUser.split("@");
		//Der eigendlich RemoteName
		remoteUser = nameTeile1[0];
		//Praefix zur namensaufl�sung
		String realm = nameTeile1[1];
			
		assertEquals("Falsche decodieren", realm, "gwdg.apache.de");
			
		BaseAuth auth = loader.loadAuthConnector(realm);
			
		assertTrue("Falsche Classe geladen", auth instanceof Shib1ApacheAuth);
			
			
		AuthContext authContext = new AuthContext();
		authContext.setUserName(remoteUser);
		authContext.setPassword(password);
		authContext.setRealm(realm);
		AuthResponse response = auth.login(authContext);
			
		assertNotNull(response.getRemoteUser());
		
		response.setRemoteUser(response.getRemoteUser()+"@"+realm);
			
		//Zerteilen des Remote User Strings
		String []nameTeile2 = response.getRemoteUser().split("@");
		//Der eigendlich RemoteName
		remoteUser = nameTeile2[0];
		//Praefix zur namensaufl�sung
		realm = nameTeile2[1];
			
			
		BaseData data = loader.loadDataConnector(realm);
			
		assertTrue("Falscher Connector", data instanceof Shib1Data);
			
		DataContext dataContext = new DataContext(remoteUser,realm);
			
		List<Attribute> attr = data.loadAttributes(dataContext);
			
		assertEquals("Falsche anzahl von attributen", attr.size(), 2);
			
	
	}
}
