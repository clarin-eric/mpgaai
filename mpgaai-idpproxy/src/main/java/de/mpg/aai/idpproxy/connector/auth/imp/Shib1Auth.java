package de.mpg.aai.idpproxy.connector.auth.imp;

import java.io.InputStream;
import java.security.cert.Certificate;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;
import de.mpg.aai.idpproxy.connector.auth.BaseAuth;
import de.mpg.aai.idpproxy.connector.auth.NameIdentifier;
import de.mpg.aai.idpproxy.connector.decoder.imp.Shib1RemoteUser;
import de.mpg.aai.idpproxy.connector.pool.PoolDataManager;
import de.mpg.aai.idpproxy.saml.SamlResponseExtractor;


public abstract class Shib1Auth extends BaseAuth {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(Shib1Auth.class);
	
	protected Certificate	cert;
	protected String		ssoHandler;
	protected Boolean		sendWithRealm = new Boolean(false);
	
	
	/**
	 * todo 
	 * @param httpClient
	 * @param context
	 * @param SPParams
	 * @return
	 * @throws ConnectorException
	 * @throws ConfigException
	 */
	protected abstract InputStream getSAMLResponseStream
		(HttpClient httpClient,AuthContext context,NameValuePair[] SPParams) 
		throws ConnectorException, ConfigException;
	
	
	public Certificate getCert() {
		return this.cert;
	}
	public void setCert(Certificate crt) {
		this.cert = crt;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public AuthResponse login(AuthContext context) throws ConnectorException {
		log.debug("ssoHandler "+ this.ssoHandler);
		HttpClient httpClient = new HttpClient();
		ConfigContext configCtx = this.getConfigContext();
		InputStream stream;
		try{
			NameValuePair[] SPParams = {
				new NameValuePair("target", configCtx.getSPTarget()),
	        	new NameValuePair("shire", configCtx.getSPShire()),
	        	new NameValuePair("providerId", configCtx.getSPProviderId())
			};
			stream = getSAMLResponseStream(httpClient,context, SPParams);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	    SamlResponseExtractor responseExtractor = SamlResponseExtractor.getInstance();
	    log.trace("using SAMLResponseExtractor {}", responseExtractor);
	    log.trace("got username {}", context.getUserName());
	    
	    // extract NameIdentifiers
	    NameIdentifier nameIdentifier = responseExtractor.extract(stream, this.cert);
	    // load DataPool
	    PoolDataManager poolDataManager = PoolDataManager.getInstance();
	    // init RemoteUser with proper data
	    Shib1RemoteUser shib1RemoteUser = new Shib1RemoteUser(context.getUserName(),context.getRealm(),nameIdentifier.getName(),nameIdentifier.getFormat(),nameIdentifier.getQualifier());
	    // movre user to DataPool
	    poolDataManager.addRemoteUser(shib1RemoteUser);
	    log.debug("added remoteUser {} to DataPool", shib1RemoteUser.toString());
	    
	    // return Name@Realm
	    AuthResponse authResponse = new AuthResponse();
	    authResponse.setRemoteUser(context.getUserName());
	    log.debug("RemoteUser "+authResponse.getRemoteUser());    
	    return authResponse;
	}
	
	
	public String getSsoHandler() {
		return this.ssoHandler;
	}
	public void setSsoHandler(String handler) {
		this.ssoHandler = handler;
	}
	
	
	public Boolean getSendWithRealm() {
		return this.sendWithRealm;
	}
	public void setSendWithRealm(Boolean val) {
		this.sendWithRealm = val;
	}
	
	
	protected String getSendUserName(AuthContext context) {
		String username = context.getUserName();
		if(this.sendWithRealm != null && this.sendWithRealm.booleanValue())
        	username = username + "@" + context.getRealm();
        log.debug("sent username: "+username);
        return username;
	}
}
