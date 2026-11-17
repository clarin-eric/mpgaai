package de.mpg.aai.security.auth.model;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author megger
 *
 */
public class BaseGroup extends BasePrincipal implements Principal {
	private Set<Principal> members = ConcurrentHashMap.newKeySet();

	/**
	 * {@inheritDoc}
	 */
	public BaseGroup(String username) {
		super(username);
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	public boolean addMember(Principal user) {
		if(user == null)
			return false;
		return this.members.add(user);
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	public boolean isMember(Principal user) {
		return this.members.contains(user);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	//@Override
	public Enumeration<? extends Principal> members() {
		return ((Vector) this.members).elements();
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	public boolean removeMember(Principal user) {
		return this.members.remove(user);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unused")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		BaseGroup result = new BaseGroup(this.getName());
		for(Principal member : this.members) {
			result.addMember(member);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj))
			return false;
		if(!(obj instanceof BaseGroup))	// strict
			return false;
		// check members: 
		Enumeration<? extends Principal> otherMembers = ((BaseGroup) obj).members();
		int count=0;
		for(Principal member ; otherMembers.hasMoreElements() ; count++) {
			member = otherMembers.nextElement();
			if(!this.members.contains(member))
				return false;
		}
		// ok, all other.members in this.members => same vice versa?
		return this.members.size() == count;	// sufficient <=> this.members must contain no duplicates 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + super.hashCode();	// using: name
		result += 31 * result + this.members.hashCode();
		return result;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("BaseGroup '");
		result.append(this.getName()).append("'");
		return result.toString();
	}
}
