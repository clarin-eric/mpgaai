package de.mpg.aai.idpproxy;

import javax.naming.directory.Attribute;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.ConnectorLoader;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;
import de.mpg.aai.idpproxy.connector.auth.BaseAuth;
import de.mpg.aai.idpproxy.connector.auth.imp.LdapAuth;
import de.mpg.aai.idpproxy.connector.data.BaseData;
import de.mpg.aai.idpproxy.connector.data.DataContext;
import de.mpg.aai.idpproxy.connector.data.imp.LdapData;

public class LdapTest extends TestBase {

	public void testSuccess() throws ConnectorException, ConfigException {
		String remoteUser = "srieger2";
		String password  = "e2";

		ConfigContext context = this.getConfigContext();
		ConnectorLoader loader = new ConnectorLoader(context.getConn());
		
		
		String suffix;
		if (remoteUser.indexOf("@") == -1)
		{
//			remoteUser = remoteUser;
			suffix = context.getConn().getDefaultConnector();
		}
		else
		{
			//Zerteilen des Remote User Strings
			String []nameTeile1 = remoteUser.split("@");
			//Der eigendlich RemoteName
			remoteUser = nameTeile1[0];
			//Praefix zur namensaufl�sung
			suffix = nameTeile1[1];
		}
			
		assertEquals("Falsche decodieren", suffix, "gwdg.de");
			
		BaseAuth auth = loader.loadAuthConnector(suffix);
			
		assertTrue("Falsche Classe geladen", auth instanceof LdapAuth);
			
			
		AuthContext authContext = new AuthContext();
		authContext.setUserName(remoteUser);
		authContext.setPassword(password);
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
			
		assertTrue("Falscher Connector", data instanceof LdapData);
			
		DataContext dataContext = new DataContext(remoteUser,suffix);

			
		
		java.util.List<Attribute> attr = data.loadAttributes(dataContext);
			
		assertFalse("Keine Attribute", attr.size() == 0);
		
	}
}
