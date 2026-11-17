package de.mpg.aai.security.auth.model;


/**
 * simple interface to indicate implementing classes can be set read-only
 * @author megger
 *
 */
public interface ReadOnly {
	
	/**
	 * @return read-only status of this instance
	 */
	public boolean isReadOnly();
	
	/**
	 * sets this instance to read-only status
	 */
	public void setReadOnly();

}
