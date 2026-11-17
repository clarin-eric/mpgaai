package de.mpg.aai.idpproxy.aar;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorBeanDefinitionParser;

public class IdPProxyDataConnectorBeanDefinitionParser extends BaseDataConnectorBeanDefinitionParser{
//	private static org.slf4j.Logger log = LoggerFactory.getLogger(IdPProxyDataConnectorBeanDefinitionParser.class);
	public static final QName QNAME = new QName(IdPProxyDataConnectorNamespaceHandler.NAMESPACE, "IdPProxy");
	
	
	@Override
	protected Class<IdPProxyDataConnectorFactoryBean> getBeanClass(@SuppressWarnings("unused") Element element) {
		return IdPProxyDataConnectorFactoryBean.class;
	}
	
	/**
	 * Get custom attributes from the element here and pass them as properties
	 * or constructor args to the bean definition, as appropriate.
		 */
	@Override
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
		super.doParse(element, builder);
	}
}
