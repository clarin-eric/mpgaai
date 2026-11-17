/**
 * 
 */
package de.mpg.aai.security.auth;

/**
 * a generic security exception base class for the mpgaai package
 * that provides type safety for all the security-related exception classes that extend from it. 
 * it's a common base exception for all mpgaai security issues, like errors on authentication, authorization, etc...
 * <p>
 * please note:<br /> 
 * this is an <b>unchecked exception</b>.<br />
 * that follows that all extending exceptions are unchecked!<br />
 * reasoning:<br/>
 * the lines of thoughts were:
 * <ul>
 * <li>in the words of the enterprise spring framework authors<br />
 * "Checked exceptions are overused in Java. A platform shouldn't force you to catch exceptions you're unlikely to be able to recover from."
 * <li>
 * <li>in Gunjan Doshi's article about best practice exceptions handling:
 * http://onjava.com/pub/a/onjava/2003/11/19/exceptions.html
 * </li>
 * <li>in my view:
 * there is no value in forcing clients of API to handle or throw/propagate such checked exceptions they cannot directly deal with.
 * it should be more a question of documentation and making it known to client implementors what kind of exception they have-to expect,
 * and can POSSIBLY handle them, rather to be impelled to treat them immediately  
 * </li>
 * </ul>
 * @see http://www.springsource.org/aboutspring
 * @see "http://www.onjava.com/pub/a/onjava/2003/11/19/exceptions.html"
 * @author megger
 */
public class AuthException extends RuntimeException {
	/** @see java.io.Serializable */
	private static final long serialVersionUID = -1233547086589670569L;
	
	
	/** {@inheritDoc} */
	public AuthException() {
	}
	/** {@inheritDoc} */
	public AuthException(String message) {
		super(message);
	}
	/** {@inheritDoc} */
	public AuthException(Throwable cause) {
		super(cause);
	}
	/** {@inheritDoc} */
	public AuthException(String message, Throwable cause) {
		super(message, cause);
	}
}
