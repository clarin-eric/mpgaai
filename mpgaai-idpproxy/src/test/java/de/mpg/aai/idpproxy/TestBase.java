package de.mpg.aai.idpproxy;

import java.net.URL;

import junit.framework.TestCase;
import de.mpg.aai.idpproxy.config.ConfigContext;

public class TestBase extends TestCase {
	private ConfigContext configCtx;
	
	@Override
	public void setUp() {
		try {
			URL config = new URL("./config");
			this.configCtx = new ConfigContext();
			this.configCtx.init(config, null);
		} catch(Exception e) {
		}
	}
	
	public ConfigContext getConfigContext() {
		return this.configCtx;
	}
	
	@Override
	public void tearDown() {
	}
	
}
