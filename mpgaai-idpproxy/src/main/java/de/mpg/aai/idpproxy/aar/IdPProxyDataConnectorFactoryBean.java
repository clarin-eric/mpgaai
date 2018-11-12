package de.mpg.aai.idpproxy.aar;

import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorFactoryBean;

public class IdPProxyDataConnectorFactoryBean extends BaseDataConnectorFactoryBean{

	private static org.slf4j.Logger log = LoggerFactory.getLogger(IdPProxyDataConnectorFactoryBean.class);
		
	
	
	@Override
	protected Object createInstance() {
		log.debug("Create Instance of IdPProxyDataConnector");
		IdPProxyDataConnector connector = new IdPProxyDataConnector();
		populateDataConnector(connector);
		return connector;
	}

	@Override
	public Class<IdPProxyDataConnector> getObjectType() {
		return IdPProxyDataConnector.class;
	}

}
