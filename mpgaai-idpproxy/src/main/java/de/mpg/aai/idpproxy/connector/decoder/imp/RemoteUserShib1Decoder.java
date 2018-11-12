package de.mpg.aai.idpproxy.connector.decoder.imp;

import de.mpg.aai.idpproxy.connector.decoder.RemoteUser;
import de.mpg.aai.idpproxy.connector.decoder.RemoteUserDecoder;

public class RemoteUserShib1Decoder extends RemoteUserDecoder{
	
	
	public RemoteUserShib1Decoder(String remoteUser) {
		super(remoteUser);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public RemoteUser getDecodeRemoteUser() {
		String user = this.getRemoteUser();
		if(user == null)
			return null;
		String []nameTeile = user.split("\\|");
		String remoteUser = nameTeile[0];
		//NameIdentifier is important for the IdP behind to find/resovle the user 
		String nameIdentifier = nameTeile[2];
		String nameQualifier = nameTeile[3];
		String nameFormat = nameTeile[1];
		Shib1RemoteUser decodeShib1RemoteUser = new Shib1RemoteUser(remoteUser,null,nameIdentifier,nameFormat,nameQualifier);
		return decodeShib1RemoteUser;
	}
}
