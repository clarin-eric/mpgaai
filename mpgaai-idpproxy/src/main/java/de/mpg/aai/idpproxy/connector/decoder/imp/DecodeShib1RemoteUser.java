package de.mpg.aai.idpproxy.connector.decoder.imp;

import de.mpg.aai.idpproxy.connector.decoder.DecodeRemoteUser;


public class DecodeShib1RemoteUser extends DecodeRemoteUser{

	private String nameIdentifier;
	private String nameQualifier;
	private String nameFormat;
	
	
	public String getNameIdentifier() {
		return this.nameIdentifier;
	}
	public void setNameIdentifier(String nameid) {
		this.nameIdentifier = nameid;
	}
	
	
	public String getNameQualifier() {
		return this.nameQualifier;
	}
	public void setNameQualifier(String val) {
		this.nameQualifier = val;
	}
	
	
	public String getNameFormat() {
		return this.nameFormat;
	}
	public void setNameFormat(String val) {
		this.nameFormat = val;
	}
	
}
