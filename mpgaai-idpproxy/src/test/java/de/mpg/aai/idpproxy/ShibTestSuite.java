package de.mpg.aai.idpproxy;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ShibTestSuite extends TestSuite{

	
	public static Test suite()
	  {
	    TestSuite mySuite = new TestSuite( "Shib Proxy Test Suit" );
	    mySuite.addTestSuite( LdapTest.class);
	    //mySuite.addTestSuite( ConnectorTest.class);
	    //mySuite.addTestSuite( Shib1ApacheTest.class );
	    //mySuite.addTestSuite( Shib1TomcatTest.class);
	    //mySuite.addTestSuite(Shib2TomcatVmext148Test.class);
	    //mySuite.addTestSuite(Shib1FKFTest.class);
	    //mySuite.addTestSuite(Shib1RZGTest.class);
	    //mySuite.addTestSuite(DataPoolTest.class);
	    
	    return mySuite;
	  }
	
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

}
