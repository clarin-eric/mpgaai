package de.mpg.aai.idpproxy.connector.data.imp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.data.BaseData;
import de.mpg.aai.idpproxy.connector.data.DataContext;


public class LdapData extends BaseData{

	String ldapUrl;
	String ldapAdmin;
	String ldapPassword;
	
	
	public String getLdapUrl() {
		return this.ldapUrl;
	}
	public void setLdapUrl(String url) {
		this.ldapUrl = url;
	}
	
	
	public String getLdapAdmin() {
		return this.ldapAdmin;
	}
	public void setLdapAdmin(String admin) {
		this.ldapAdmin = admin;
	}
	
	
	public String getLdapPassword() {
		return this.ldapPassword;
	}
	public void setLdapPassword(String password) {
		this.ldapPassword = password;
	}
	
	
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Attribute> loadAttributes(DataContext context) throws ConnectorException {
		try{
			String target = this.ldapUrl.replaceAll("%username%", context.getRemoteUser());
			Hashtable<String,String> env;
			env = new Hashtable<String,String>(11);
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, this.ldapAdmin);
			env.put(Context.SECURITY_CREDENTIALS, this.ldapPassword);
			env.put(Context.PROVIDER_URL, target);
			DirContext ctxSearch = new InitialDirContext(env);
			NamingEnumeration<?> results = ctxSearch.search(target,"", null);
			List<Attribute> attributeList = new ArrayList<Attribute>();
			if (results.hasMore()) {
				SearchResult result = (SearchResult)results.next();
				Attributes attributesResult = result.getAttributes();
				NamingEnumeration<Attribute> attribibuteNamingEnumeration = 
					(NamingEnumeration<Attribute>)attributesResult.getAll();
				
				while(attribibuteNamingEnumeration.hasMore()) {
					Attribute attribute = attribibuteNamingEnumeration.next();
					attributeList.add(attribute);
				}
				return attributeList;
			}
			throw new ConnectorException("Attribute Faild to Load");
		} catch(NamingException nE) {
			throw new ConnectorException(nE.getExplanation(), nE);
		}
	}
}
