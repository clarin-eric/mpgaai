package de.mpg.aai.idpproxy.connector.auth;


public class NameIdentifier {
	
	String Format;
	String Name;
	String Qualifier;
	
	
	public String getFormat() {
		return this.Format;
	}
	public void setFormat(String format) {
		this.Format = format;
	}
	
	
	public String getName() {
		return this.Name;
	}
	public void setName(String name) {
		this.Name = name;
	}
	
	
	public String getQualifier() {
		return this.Qualifier;
	}
	public void setQualifier(String qualifier) {
		this.Qualifier = qualifier;
	}
}
