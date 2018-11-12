package de.mpg.aai.idpproxy.config;

import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;


public class CertsConfig {

	Map<String,Certificate> certMap = new HashMap<String,Certificate>();

	public Map<String, Certificate> getCertMap() {
		return this.certMap;
	}
	
}
