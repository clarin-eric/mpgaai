package de.mpg.aai.idpproxy.config.connector.data.imp;

import org.w3c.dom.Element;

import de.mpg.aai.idpproxy.config.connector.data.DataFactory;
import de.mpg.aai.idpproxy.config.connector.data.DataParser;


public class Shib1DataParser extends DataParser {


	/** {@inheritDoc} */
	@Override
	public DataFactory doParse(Element element) {
		Shib1DataFactory factory = new Shib1DataFactory();
		String aaHandler = element.getElementsByTagName("AAHandler").item(0).getTextContent();
		factory.setAaHandler(aaHandler.trim());
		return factory;
	}
}