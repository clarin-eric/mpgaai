package de.mpg.aai.idpproxy.connector.auth.imp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;


public class Shib1ApacheAuth extends Shib1Auth {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(Shib1ApacheAuth.class);
	
	
	/** {@inheritDoc} */
	@Override
	public InputStream getSAMLResponseStream
	(HttpClient httpClient,AuthContext context,NameValuePair[] SPParams) 
	throws ConnectorException, ConfigException {
		try {
			int status = 0;
			// GetMethod to SSO handler
	        HttpMethod getMethod = new GetMethod(this.ssoHandler);
	        //Query String set shire provider etc
	        getMethod.setQueryString(SPParams);
	        log.trace("executing http-GET...");
	        status = httpClient.executeMethod(getMethod);
	        log.trace("http-GET status: {}", String.valueOf(status));
	        // if authentication required
	        if(status != 401) {
	        	log.error("authentication failed, returned http status was {}, expecting 401", String.valueOf(status));
	        	throw new ConnectorException("ldap authn failed");
	        }
	        
	        String username = this.getSendUserName(context);
	        
        	// set credentials
	        log.trace("sending credentials...");
	        httpClient.getState().setCredentials
	        	(AuthScope.ANY, new UsernamePasswordCredentials(username, context.getPassword()));
	        status = httpClient.executeMethod(getMethod);
	        log.trace("got http-status {}", String.valueOf(status));    
	        if(status == 401) {
	        	log.error("authentication failed, returned http status was {}, expecting 401", String.valueOf(status));
	        	throw new ConnectorException("ldap authn failed");
	        } 
	        log.debug("authn success, now parsing response");
	        return getMethod.getResponseBodyAsStream();
		}catch(IOException ioE) {
			throw new ConnectorException(ioE);
		}
	}
}
