package de.mpg.aai.idpproxy.aar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.Attribute;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.connector.DataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;


public class IdPProxyDataConnector extends BaseDataConnector implements ApplicationListener{
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(IdPProxyDataConnector.class);
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext shibContext) {
		try{
			log.debug("resolving attributes for remote user");
			String remoteUserUn = shibContext.getAttributeRequestContext().getPrincipalName();
			ConfigContext configCtx = ConfigContext.getActive(this.getId());
			DataConnector connector = new DataConnector(configCtx.getConn());
			List<Attribute> attributeList = connector.loadAttribute(remoteUserUn);
			Map<String, BaseAttribute> attributeMap = new HashMap<String, BaseAttribute>();
			
			log.debug("Attributes in DataConnector from: "+remoteUserUn);
			if(attributeList == null)
				return attributeMap;
			
			for(int x = 0 ; x < attributeList.size() ; x++) {
				Attribute attr = attributeList.get(x);
				log.debug("Attribute name:"+attr.getID());
				BaseAttribute<Object> baseAttr = new BasicAttribute<Object>(attr.getID());
				for(int y = 0 ; y < attr.size() ; y++) {
					log.debug("-- Attribute value:"+attr.get(y));
					baseAttr.getValues().add(attr.get(y));
				}
				attributeMap.put(baseAttr.getId(), baseAttr);
			}
			return attributeMap;
		} catch(Exception e) {
			// FIXME: why not failfast? obiously no errorhandling here
			log.error("could not resolve attributes due to: {}", e.getMessage());
			return null;
		}
	}

	public void validate() {
	}
	
	
	public void onApplicationEvent(@SuppressWarnings("unused") ApplicationEvent event) {
	}
}
