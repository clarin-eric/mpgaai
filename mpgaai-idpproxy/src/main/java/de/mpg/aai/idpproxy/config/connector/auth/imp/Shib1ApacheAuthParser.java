package de.mpg.aai.idpproxy.config.connector.auth.imp;

import org.w3c.dom.Element;

import de.mpg.aai.idpproxy.config.connector.auth.AuthFactory;


public class Shib1ApacheAuthParser extends Shib1AuthParser {
	
	
	/** {@inheritDoc} */
	@Override
	public AuthFactory doParse(Element element) {
		Shib1ApacheAuthFactory factory = new Shib1ApacheAuthFactory();
		this.doParse(factory, element);
		return factory;
	}
}
