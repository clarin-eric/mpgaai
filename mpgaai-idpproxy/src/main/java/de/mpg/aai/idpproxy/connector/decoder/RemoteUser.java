package de.mpg.aai.idpproxy.connector.decoder;


public class RemoteUser {
	
	private String remoteUser;
	private String realm;
	
	
	public RemoteUser(String user, String scope) {
		this.remoteUser = user;
		this.realm = scope;
	}
	
	
	public String getRealm() {
		return this.realm;
	}
	public String getRemoteUser() {
		return this.remoteUser;
	}
}
