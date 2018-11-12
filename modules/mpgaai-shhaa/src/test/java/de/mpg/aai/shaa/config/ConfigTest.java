package de.mpg.aai.shaa.config;


import de.mpg.aai.shhaa.authz.Location;
import de.mpg.aai.shhaa.authz.Matcher;
import de.mpg.aai.shhaa.config.ConfigContext;
import de.mpg.aai.shhaa.config.Configuration;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.hasItem;
import org.junit.Assert;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ConfigTest {
	/** the logger */
	private static Logger log = LoggerFactory.getLogger(ConfigTest.class);

        private static Configuration conf;
        
        @BeforeClass
        public static void setUpClass() {
            ConfigContext confCtx = new ConfigContext();
            confCtx.init(ConfigTest.class.getResource("/shhaa.xml"));
            conf = confCtx.getConfiguration();
        }
	
	/**
	 * tests loading the configuration from config-xml
	 * @throws MalformedURLException 
	 */
	@Test
	public void loadStandard() throws MalformedURLException {
		Assert.assertNotNull(conf);
	}
        
        @Test 
        public void testLocations() {
            List<Location> locationRules = conf.getLocationRules();
            assertEquals(2, locationRules.size());
            Location location1 = locationRules.get(0);
            assertEquals("/app/login", location1.getTarget());
            assertTrue(location1.matchesPath("/app/LOGIN")); // case insensitive (mode='nocase')
            assertArrayEquals(new String[]{}, location1.getMethods());
            
            Location location2 = locationRules.get(1);
            assertEquals("/service/virtualcollections", location2.getTarget());
            assertFalse(location1.matchesPath("/SERVICE/virtualcollections")); // case sensitive (default)

            // test methods
            List<String> methods = Arrays.asList(location2.getMethods());
            assertThat(methods, hasItem("post"));
            assertThat(methods, hasItem("put"));
            assertThat(methods, hasItem("delete"));
        }
        
	@Test
	@Ignore
	public void checkQuery() {
		String result = null;
		String action = "shhaaDo";		
		// alone
		result =  this.cleanoutShhaaAction(action, "shhaaDo=lI");
		Assert.assertNull(result);
		// leading
		result =  this.cleanoutShhaaAction(action, "shhaaDo=lI&x=y&bla=1");
		Assert.assertEquals("?x=y&bla=1", result);
		// middle
		result =  this.cleanoutShhaaAction(action, "bla=1&shhaaDo=lI&x=y");
		Assert.assertEquals("?bla=1&x=y", result);
		// trailing
		result =  this.cleanoutShhaaAction(action, "x=y&bla=1&shhaaDo=lI");
		Assert.assertEquals("?x=y&bla=1", result);
		// missing
		result =  this.cleanoutShhaaAction(action, "x=y&bla=1");
		Assert.assertEquals("?x=y&bla=1", result);
		// none
		result =  this.cleanoutShhaaAction(action, "");
		Assert.assertNull(result);
	}
	private String cleanoutShhaaAction(String action, String query) {
		if(action == null || action.isEmpty())
			return null;
		if(query == null || query.isEmpty())
			return null;
		
		String[] params = query.split("&");
		StringBuffer result = new StringBuffer();
		for(String param : params) {
			if(param.startsWith(action))
				continue;
			result.append(result.length() <= 0 ? "?" : "&");
			result.append(param);
		}
		return result.length() > 0 ? result.toString() : null;
	}
}
