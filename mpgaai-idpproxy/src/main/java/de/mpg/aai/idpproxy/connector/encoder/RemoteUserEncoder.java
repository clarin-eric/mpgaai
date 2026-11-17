package de.mpg.aai.idpproxy.connector.encoder;


public abstract class RemoteUserEncoder {

	private String remoteUser;
	
	
	public RemoteUserEncoder(String user) {
		this.remoteUser = user;
	}
	
	
	/**
	 * todo
	 * @return
	 */
	public abstract String getEncodeRemoteUser();
	
	
	public String getRemoteUser() {
		return this.remoteUser;
	}
}
