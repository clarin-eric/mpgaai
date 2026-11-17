package de.mpg.aai.security.auth.model;

import java.security.Principal;


/***
 * 
 * @author megger
 */
public class BasePrincipal extends AbstractReadOnly implements Principal, Cloneable {
	/** the username, uid */
	private String	name; 
	
	
	/**
	 * default constructor 
	 * @param username this Principals user-name
	 */
	public BasePrincipal(String username) {
		if(username == null)
			throw new IllegalArgumentException("username must not be null");
		this.name = username;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return this.name;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unused")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		BasePrincipal result = new BasePrincipal(this.getName());
		return result;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof BasePrincipal))	// strict
			return false; 
		String otherName = ((BasePrincipal) obj).getName();
		String thisName = this.getName();
		// CONTRACT (see hashcode contract): name never null
		if(thisName == null || otherName == null)
			throw new NullPointerException("principal.name must not be null"); 
		return thisName.equals(otherName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		String hashBase = this.getName();
		if(hashBase == null)
			throw new NullPointerException("principal.name must not be null"); 
		return hashBase.hashCode();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(this.getClass().getSimpleName());
		result.append(" '").append(this.getName()).append("'");
		return result.toString();
	}
}
