package de.mpg.aai.idpproxy.config.connector;

import java.util.ArrayList;
import java.util.List;


public class NameTable {

	private List<String> names = new ArrayList<String>();
	private String name;
	private boolean isDefault;
	
	
	public NameTable(String val) {
		this.name = val;
	}
	
	
	public boolean isDefault() {
		return this.isDefault;
	}
	public void setDefault(boolean val) {
		this.isDefault = val;
	}
	
	
	public boolean ContainsSuffix(String suffix) {
		return this.names.contains(suffix);
	}
	
	
	public List<String> getNames() {
		return this.names;
	}
	public String getName() {
		return this.name;
	}
}
