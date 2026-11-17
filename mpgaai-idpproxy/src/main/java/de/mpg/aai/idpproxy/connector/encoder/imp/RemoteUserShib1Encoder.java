package de.mpg.aai.idpproxy.connector.encoder.imp;

import de.mpg.aai.idpproxy.connector.auth.NameIdentifier;
import de.mpg.aai.idpproxy.connector.encoder.RemoteUserEncoder;


public class RemoteUserShib1Encoder extends RemoteUserEncoder{
	private NameIdentifier nameIdentifier;
	
	
	public RemoteUserShib1Encoder(String remoteUser,NameIdentifier nameID) {
		super(remoteUser);
		this.nameIdentifier = nameID;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String getEncodeRemoteUser() {
		StringBuffer result = new StringBuffer(this.getRemoteUser());
		result.append("|").append(this.nameIdentifier.getFormat());
		result.append("|").append(this.nameIdentifier.getName());
		result.append("|").append(this.nameIdentifier.getQualifier());
		return result.toString();
	}
}
