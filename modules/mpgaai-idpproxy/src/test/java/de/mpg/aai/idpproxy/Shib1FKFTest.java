package de.mpg.aai.idpproxy;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.ConnectorLoader;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;
import de.mpg.aai.idpproxy.connector.auth.BaseAuth;
import de.mpg.aai.idpproxy.connector.data.BaseData;
import de.mpg.aai.idpproxy.connector.data.DataContext;
import de.mpg.aai.idpproxy.connector.data.imp.Shib1Data;

public class Shib1FKFTest extends TestBase {
	
	
	public void testSuccess() throws ConnectorException, ConfigException, NamingException
	{
		//https://shib.fkf.mpg.de/idp/profile/Shibboleth/SSO?target=cookie&shire=https://shib-idp.mpg.de/shibboleth/dummy/entry&providerId=https://shib-idp.gwdg.de/shibboleth/proxy
		String remoteUser = "ivstest@fkf.mpg.de";
		String password  = "easyd2or";

		ConfigContext context = this.getConfigContext();
		ConnectorLoader loader = new ConnectorLoader(context.getConn());
		
		
		//Zerteilen des Remote User Strings
		String []nameTeile1 = remoteUser.split("@");
		//Der eigendlich RemoteName
		remoteUser = nameTeile1[0];
		//Praefix zur namensaufl�sung
		String suffix = nameTeile1[1];
			
				
		BaseAuth auth = loader.loadAuthConnector(suffix);
			

			
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
			
		if(attr != null)
		{
			for (Attribute attribute : attr) {
				System.out.println(attribute.getID()+": "+attribute.get().toString());
			}
		}
	}
}
