package de.mpg.aai.idpproxy.connector.auth;


/**
 * provides login name password and others to Login Context 
 * @author thajek
 */
public class AuthContext {

	private String userName;
	private String password;
	private String realm;
	
	
	public String getRealm() {
		return this.realm;
	}
	public void setRealm(String val) {
		this.realm = val;
	}
	
	
	public String getUserName() {
		return this.userName;
	}
	public void setUserName(String uid) {
		this.userName = uid;
	}
	
	
	public String getPassword() {
		return this.password;
	}
	public void setPassword(String pw) {
		this.password = pw;
	}
}
