package de.mpg.aai.idpproxy.config.connector;

import java.util.List;
import java.util.Vector;


public class ConnectorConfig {
	private List<BaseFactory> AuthFactoryList = new Vector<BaseFactory>();
	private List<BaseFactory> DataFactoryList = new Vector<BaseFactory>();
	private List<NameTable> NameTableList = new Vector<NameTable>();
	private String defaultConnector;
	
	
	public String getDefaultConnector() {
		return this.defaultConnector;
	}
	public void setDefaultConnector(String defConn) {
		this.defaultConnector = defConn;
	}


	public List<NameTable> getNameTableList() {
		return this.NameTableList;
	}


	public List<BaseFactory> getAuthFactoryList() {
		return this.AuthFactoryList;
	}
	public void setAuthFactoryList(List<BaseFactory> authFactoryList) {
		this.AuthFactoryList = authFactoryList;
	}


	public List<BaseFactory> getDataFactoryList() {
		return this.DataFactoryList;
	}
	public void setDataFactoryList(List<BaseFactory> dataFactoryList) {
		this.DataFactoryList = dataFactoryList;
	}
}