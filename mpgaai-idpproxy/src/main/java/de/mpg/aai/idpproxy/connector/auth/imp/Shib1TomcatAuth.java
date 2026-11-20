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


public class Shib1TomcatAuth extends Shib1Auth {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(Shib1TomcatAuth.class);
	
	
	/** {@inheritDoc} */
	@Override
	public InputStream getSAMLResponseStream(HttpClient httpClient,AuthContext context,NameValuePair[] SPParams) throws ConnectorException, ConfigException {
		try {
			int status;
	        GetMethod getMethod = new GetMethod(this.ssoHandler);
	        getMethod.setQueryString(SPParams);
	        status = httpClient.executeMethod(getMethod);
	        getMethod.getResponseBodyAsString();
	        getMethod.releaseConnection();
	        String realmPath = this.ssoHandler.substring(0,this.ssoHandler.lastIndexOf('/'));
	        realmPath = realmPath +"/j_security_check";
	        String username = this.getSendUserName(context);
	        PostMethod postMethod = new PostMethod(realmPath);
	        NameValuePair[] postData = {
	        	new NameValuePair("j_username", username),
	        	new NameValuePair("j_password", context.getPassword())
	        };
	        postMethod.setRequestBody(postData);
	        status = httpClient.executeMethod(postMethod);
	        postMethod.getResponseBodyAsString();
	        if(status != 302) {
	        	log.error("authentication failed, returned http status was {}, expecting 302", String.valueOf(status));
	        	throw new ConnectorException("tomcat authn failed");
	        }
	        status = httpClient.executeMethod(getMethod);
	        return getMethod.getResponseBodyAsStream();
		} catch(IOException ioE) {
			throw new ConnectorException(ioE);
		}
	}
}
