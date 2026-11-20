package de.mpg.aai.idpproxy.config.connector.data.imp;

import de.mpg.aai.idpproxy.config.connector.data.DataFactory;
import de.mpg.aai.idpproxy.connector.BaseConnector;
import de.mpg.aai.idpproxy.connector.data.imp.Shib1Data;


public class Shib1DataFactory extends DataFactory {

	private String aaHandler;
	
	
	public void setAaHandler(String handler) {
		this.aaHandler = handler;
	}
	
	
	/** {@inheritDoc} */
	@Override
	protected BaseConnector internCreateNewInstance() {
		Shib1Data data = new Shib1Data();
		data.setAaHandler(this.aaHandler);
		return data;
	}
}
