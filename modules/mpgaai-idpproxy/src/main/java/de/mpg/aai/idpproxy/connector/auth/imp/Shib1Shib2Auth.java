package de.mpg.aai.idpproxy.connector.auth.imp;


import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;

public class Shib1Shib2Auth extends Shib1Auth {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(Shib1Shib2Auth.class);
	
	protected String authHandler;
	
	
	/** {@inheritDoc} */
	@Override
	public InputStream getSAMLResponseStream
	(HttpClient httpClient,AuthContext context,NameValuePair[] SPParams) 
	throws ConnectorException, ConfigException {
		try{
			int status;
	        GetMethod getMethod = new GetMethod(this.ssoHandler);
 	        getMethod.setQueryString(SPParams);
	        status = httpClient.executeMethod(getMethod);
	        String username = this.getSendUserName(context);
	        getMethod.releaseConnection();
	        PostMethod postMethod = new PostMethod(this.authHandler);
	        NameValuePair[] postData = {
	        	new NameValuePair("j_username", username),
	        	new NameValuePair("j_password", context.getPassword())
	        };
	        postMethod.setRequestBody(postData);
	        status = httpClient.executeMethod(postMethod);
	        if(status != 200) {
	        	log.error("authentication failed, returned http status was {}, expecting 200", String.valueOf(status));
	        	throw new ConnectorException("shib authn failed");
	        }
	        return postMethod.getResponseBodyAsStream();
		} catch(IOException ioE) {
			throw new ConnectorException(ioE);
		}
	}
	
	
	public String getAuthHandler() {
		return this.authHandler;
	}
	public void setAuthHandler(String handler) {
		this.authHandler = handler;
	}
}
