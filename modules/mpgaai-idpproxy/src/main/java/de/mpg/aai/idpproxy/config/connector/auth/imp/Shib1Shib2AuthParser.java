package de.mpg.aai.idpproxy.config.connector.auth.imp;

import org.w3c.dom.Element;

import de.mpg.aai.idpproxy.config.connector.auth.AuthFactory;


public class Shib1Shib2AuthParser extends Shib1AuthParser{
	
	
	/** {@inheritDoc} */
	@Override
	public AuthFactory doParse(Element element) {
		Shib1Shib2AuthFactory factory = new Shib1Shib2AuthFactory();
		String AuthHandler = element.getElementsByTagName("AuthHandler").item(0).getTextContent();
		factory.setAuthHandler(AuthHandler.trim());
		this.doParse(factory, element);
		return factory;
	}
}
