package de.mpg.aai.idpproxy.connector.data;


public class DataContext {

	private String remoteUser;
	private String realm;
	
	
	public DataContext(String usr, String theRealm) {
		super();
		this.remoteUser = usr;
		this.realm = theRealm;
	}
	
	
	public String getRemoteUser() {
		return this.remoteUser;
	}
	
	
	public String getRealm() {
		return this.realm;
	}
}
