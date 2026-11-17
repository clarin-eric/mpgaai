package de.mpg.aai.idpproxy.connector.decoder.imp;

import de.mpg.aai.idpproxy.connector.decoder.RemoteUser;


public class Shib1RemoteUser extends RemoteUser{
	private String nameIdentifier;
	private String nameQualifier;
	private String nameFormat;
	
	
	public Shib1RemoteUser(String remoteUser, String realm,
			String nameId, String nameFmt, String nameQual) {
		super(remoteUser, realm);
		this.nameIdentifier = nameId;
		this.nameQualifier = nameQual;
		this.nameFormat = nameFmt;
	}
	
	
	public String getNameIdentifier() {
		return this.nameIdentifier;
	}
	public String getNameQualifier() {
		return this.nameQualifier;
	}
	public String getNameFormat() {
		return this.nameFormat;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("username: ").append(this.getRemoteUser());
		result.append(" nameIdentifier: ").append(this.getNameIdentifier());
		return result.toString();
	}
}
