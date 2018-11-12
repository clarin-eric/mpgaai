package de.mpg.aai.idpproxy.connector.decoder;


public abstract class RemoteUserDecoder {

	private String remoteUser;
	
	
	public RemoteUserDecoder(String user) {
		this.remoteUser = user;
	}
	
	
	/**
	 * todo
	 * @return
	 */
	public abstract RemoteUser getDecodeRemoteUser();
	
	
	public String getRemoteUser() {
		return this.remoteUser;
	}
}
