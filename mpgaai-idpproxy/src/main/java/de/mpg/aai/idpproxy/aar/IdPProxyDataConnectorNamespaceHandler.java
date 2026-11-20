package de.mpg.aai.idpproxy.aar;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;


public class IdPProxyDataConnectorNamespaceHandler extends BaseSpringNamespaceHandler{
//	private static org.slf4j.Logger log = LoggerFactory.getLogger(IdPProxyDataConnectorNamespaceHandler.class);
	public static final String NAMESPACE = "urn:idpproxy:shibboleth:2.0:resolver";
	
	
	public void init() {
		registerBeanDefinitionParser( 
				IdPProxyDataConnectorBeanDefinitionParser.QNAME, 
				new IdPProxyDataConnectorBeanDefinitionParser());
	}
}
