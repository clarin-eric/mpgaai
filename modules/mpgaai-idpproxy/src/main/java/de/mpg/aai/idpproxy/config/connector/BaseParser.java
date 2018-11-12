package de.mpg.aai.idpproxy.config.connector;

import org.w3c.dom.Element;

public interface BaseParser {
	/**
	 * 
	 * @param element
	 * @return
	 */
	public BaseFactory doParse(Element element);
}
