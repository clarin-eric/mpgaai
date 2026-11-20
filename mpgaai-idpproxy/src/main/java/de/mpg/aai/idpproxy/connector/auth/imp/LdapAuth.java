package de.mpg.aai.idpproxy.connector.auth.imp;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.auth.AuthContext;
import de.mpg.aai.idpproxy.connector.auth.AuthResponse;
import de.mpg.aai.idpproxy.connector.auth.BaseAuth;


public class LdapAuth extends BaseAuth{
	
	private String ldapUrl;
	private String ldapAdmin;
	private String ldapPassword;
	
	
	/** {@inheritDoc} */
	@Override
	public AuthResponse login(AuthContext context) throws ConnectorException {
		try{
			String target = this.ldapUrl.replaceAll("%username%", context.getUserName());
			Hashtable<String,String> env;
			env = new Hashtable<String,String>(11);
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, this.ldapAdmin);
			env.put(Context.SECURITY_CREDENTIALS, this.ldapPassword);
			env.put(Context.PROVIDER_URL, target);
			
			DirContext ctxSearch = new InitialDirContext(env);
			NamingEnumeration<?> results = ctxSearch.search(target,"", null);
			if(results.hasMore()) {
				SearchResult sr = (SearchResult) results.next();
				String targetDN = sr.getNameInNamespace();
				env.put(Context.SECURITY_PRINCIPAL, targetDN);
				env.put(Context.SECURITY_CREDENTIALS, context.getPassword());
				
				ctxSearch = new InitialDirContext(env);
				AuthResponse response = new AuthResponse();
				response.setRemoteUser(context.getUserName());
				return response;
			} 
			throw new ConnectorException("ldap authentication failed - no match found");
		} catch(NamingException nE) {
			throw new ConnectorException("ldap authn failed", nE);
		}
	}
	
	
	public String getLDAPUrl() {
		return this.ldapUrl;
	}
	public void setLDAPUrl(String url) {
		this.ldapUrl = url;
	}


	public String getLDAPAdmin() {
		return this.ldapAdmin;
	}
	public void setLDAPAdmin(String admin) {
		this.ldapAdmin = admin;
	}


	public String getLdapPassword() {
		return this.ldapPassword;
	}
	public void setLdapPassword(String password) {
		this.ldapPassword = password;
	}
}
