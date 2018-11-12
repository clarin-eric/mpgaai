package de.mpg.aai.idpproxy.realm;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import java.util.Vector;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.slf4j.LoggerFactory;

import de.mpg.aai.idpproxy.config.ConfigContext;
import de.mpg.aai.idpproxy.connector.AuthConnector;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;


public class IdpProxyRealm extends RealmBase {
	/** the logger */
	private static org.slf4j.Logger log = LoggerFactory.getLogger(IdpProxyRealm.class);
	/** actual user to authenticate */
	private GenericPrincipal	user;
	/** list of configured roles to be granted to the authenticated principal */
	private List<String>		roles;
	/** identifier, used to identify the proper config target:
	 * distinguish and segregate different configs in multiple parallel installations */
	private String				id;
	
	
	/**
	 * default constructor
	 */
	public IdpProxyRealm() {
	}
	
	
	/** {@inheritDoc} */
	@Override
	protected String getName() {
		return "IdpProxyRealm";
	}
	
	
	/** {@inheritDoc} */
	@Override
	protected String getPassword(String arg0) {
		GenericPrincipal pcpl = (GenericPrincipal) this.getPrincipal(arg0);
		return pcpl != null 
			? pcpl.getPassword()
			: null;
	}
	
	
	/** {@inheritDoc} */
	@Override
	protected Principal getPrincipal(String arg0) {
		return this.user != null && this.user.getName().equals(arg0)
			? this.user
			: null;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public Principal authenticate(String username,byte[] credentials) {
		try {
			String pw = new String(credentials, "UTF-8");
			this.authenticate(username, pw);
			return this.user;
		} catch(UnsupportedEncodingException ueE) {
			log.error("failed to decode credential bytes: {}", ueE.getMessage(), ueE);
			return null;
		}
	}
	
	
	/** {@inheritDoc} */
	@Override
	public Principal authenticate(String username, String credentials) {
		this.user = null;
		try {
			log.debug("Starting realm with username {}", username);
			ConfigContext configCtx = ConfigContext.getActive(this.getId());
			AuthConnector connector = new AuthConnector(configCtx.getConn());
			AuthResponse response = connector.authenticate(username, credentials);
			log.debug("successful authn for user {} with roles {}", response.getRemoteUser(), this.roles);
			this.user = new GenericPrincipal(this,response.getRemoteUser(),credentials, this.roles);
		} catch(Exception eE) {
			log.error("error during realm authn: IdPProxyRealm {}", eE.getMessage(), eE);
		}
		return this.user;
	}
	
	
	/**
	 * initializes the roles by the given (list of) role names
	 * @param roleNames name(s) of assigned roles, can be a comma-separated string 
	 * @return Set of roles 
	 */
	private List<String> initRoles(String roleNames) {
		log.debug("initializing roles with values " + roleNames);
		String rolez = null; 
		if(roleNames == null || roleNames.trim().length() <= 0) {
			rolez= "role";
			log.warn("no roles found, fallback to default: {}", rolez);
		} else {
			rolez = roleNames;
		}
		this.roles = new Vector<String>();
		String[] names = rolez.split("[,;|]");
		for(int ii=0 ; ii < names.length ; ii++) {
			String name = names[ii].trim();
			if(name == null || name.length() <= 0) {
				log.debug("ommitting \"empty\" role(name)");
				continue; 
			}
			this.roles.add(name);
		}
		return this.roles;
	}
	
	
	/**
	 * @param rolez the roles to be granted to authenticated principals
	 */
	public void setRoles(String rolez) {
		this.initRoles(rolez);
	}
	/**
	 * @return list (as string) of the roles to be granted to authenticated principals
	 */
	public String getRoles() {
		return this.roles != null 
			? this.roles.toString()
			: null;
	}
	
	
	/**
	 * @param configID the identifier, used to load proper config target
	 */
	public void setId(String configID) {
		this.id = configID;
	}
	/**
	 * @return this instance's (used config-) ID
	 */
	public String getId() {
		return this.id;
	}
}
