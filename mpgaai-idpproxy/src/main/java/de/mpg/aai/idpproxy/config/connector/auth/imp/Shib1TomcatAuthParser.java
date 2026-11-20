package de.mpg.aai.idpproxy.config.connector.auth.imp;

import org.w3c.dom.Element;

import de.mpg.aai.idpproxy.config.connector.auth.AuthFactory;


public class Shib1TomcatAuthParser extends Shib1AuthParser{
	
	
	/** {@inheritDoc} */
	@Override
	public AuthFactory doParse(Element element) {
		Shib1TomcatAuthFactory factory = new Shib1TomcatAuthFactory();
		this.doParse(factory, element);
		return factory;
	}
}
