/**
 *
 */
package de.mpg.aai.security.auth.module;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import com.sun.security.auth.LdapPrincipal;
import com.sun.security.auth.UserPrincipal;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.MissingResourceException;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * patched version of Sun's com.sun.security.auth.module.LdapLoginModule:
 * additional flag 'noLdapPrincipal' - if "true" {@link #commit()} will NOT add
 * the ldapPrincipal(name) to the resulting {@link Subject}
 *
 * @author megger
 *
 */
public class LdapLoginModule implements LoginModule {

    // Use the default classloader for this class to load the prompt strings.
    private static final ResourceBundle rb
            = (ResourceBundle) AccessController.doPrivileged(
                    new PrivilegedAction() {
                public Object run() {
                    return ResourceBundle.getBundle(
                            "sun.security.util.AuthResources");
                }
            }
            );

    // Keys to retrieve the stored username and password
    private static final String USERNAME_KEY = "javax.security.auth.login.name";
    private static final String PASSWORD_KEY
            = "javax.security.auth.login.password";

    // Option names
    private static final String USER_PROVIDER = "userProvider";
    private static final String USER_FILTER = "userFilter";
    private static final String AUTHC_IDENTITY = "authIdentity";
    private static final String AUTHZ_IDENTITY = "authzIdentity";

    // Used for the username token replacement
    private static final String USERNAME_TOKEN = "{USERNAME}";
    private static final Pattern USERNAME_PATTERN
            = Pattern.compile("\\{USERNAME\\}");

    // Configurable options
    private String userProvider;
    private String userFilter;
    private String authcIdentity;
    private String authzIdentity;
    private String authzIdentityAttr = null;
    private boolean useSSL = true;
    private boolean verifyCertificate = true;
    private boolean authFirst = false;
    private boolean authOnly = false;
    private boolean useFirstPass = false;
    private boolean tryFirstPass = false;
    private boolean storePass = false;
    private boolean clearPass = false;
    private boolean noLdapPrincipal = false;
    private boolean debug = false;

    // Authentication status
    private boolean succeeded = false;
    private boolean commitSucceeded = false;

    // Supplied username and password
    private String username;
    private char[] password;

    // User's identities
    private LdapPrincipal ldapPrincipal;
    private UserPrincipal userPrincipal;
    private UserPrincipal authzPrincipal;

    // Initial state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;
    private LdapContext ctx;
    private Matcher identityMatcher = null;
    private Matcher filterMatcher = null;
    private Hashtable ldapEnvironment;
    private SearchControls constraints = null;

    /**
     * Initialize this <code>LoginModule</code>.
     *
     * @param subject the <code>Subject</code> to be authenticated.
     * @param callbackHandler a <code>CallbackHandler</code> to acquire the
     * username and password.
     * @param sharedState shared <code>LoginModule</code> state.
     * @param options options specified in the login <code>Configuration</code>
     * for this particular <code>LoginModule</code>.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        ldapEnvironment = new Hashtable(9);
        ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");

        // Add any JNDI properties to the environment
        Set keys = options.keySet();
        String key;
        for (Iterator i = keys.iterator(); i.hasNext();) {
            key = (String) i.next();
            if (key.indexOf(".") > -1) {
                ldapEnvironment.put(key, options.get(key));
            }
        }

        // initialize any configured options
        userProvider = (String) options.get(USER_PROVIDER);
        if (userProvider != null) {
            ldapEnvironment.put(Context.PROVIDER_URL, userProvider);
        }

        authcIdentity = (String) options.get(AUTHC_IDENTITY);
        if (authcIdentity != null
                && (authcIdentity.indexOf(USERNAME_TOKEN) != -1)) {
            identityMatcher = USERNAME_PATTERN.matcher(authcIdentity);
        }

        userFilter = (String) options.get(USER_FILTER);
        if (userFilter != null) {
            if (userFilter.indexOf(USERNAME_TOKEN) != -1) {
                filterMatcher = USERNAME_PATTERN.matcher(userFilter);
            }
            constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(new String[0]); //return no attrs
            constraints.setReturningObjFlag(true); // to get the full DN
        }

        authzIdentity = (String) options.get(AUTHZ_IDENTITY);
        if (authzIdentity != null
                && authzIdentity.startsWith("{") && authzIdentity.endsWith("}")) {
            if (constraints != null) {
                authzIdentityAttr
                        = authzIdentity.substring(1, authzIdentity.length() - 1);
                constraints.setReturningAttributes(
                        new String[]{authzIdentityAttr});
            }
            authzIdentity = null; // set later, from the specified attribute
        }

        // determine mode
        if (authcIdentity != null) {
            if (userFilter != null) {
                authFirst = true; // authentication-first mode
            } else {
                authOnly = true; // authentication-only mode
            }
        }

        if ("false".equalsIgnoreCase((String) options.get("useSSL"))) {
            useSSL = false;
            ldapEnvironment.remove(Context.SECURITY_PROTOCOL);
        } else {
            ldapEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
            //Disable certificate verification
            if ("false".equalsIgnoreCase((String) options.get("verifyCertificate"))) {
                verifyCertificate = false;
                ldapEnvironment.put("java.naming.ldap.factory.socket", BlindSSLSocketFactory.class.getName());
            }
        }
        
        tryFirstPass
                = "true".equalsIgnoreCase((String) options.get("tryFirstPass"));

        useFirstPass
                = "true".equalsIgnoreCase((String) options.get("useFirstPass"));

        storePass = "true".equalsIgnoreCase((String) options.get("storePass"));

        clearPass = "true".equalsIgnoreCase((String) options.get("clearPass"));

        this.noLdapPrincipal = "true".equalsIgnoreCase((String) options.get("noLdapPrincipal"));

        debug = "true".equalsIgnoreCase((String) options.get("debug"));

        if (debug) {
            if (authFirst) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "authentication-first mode; ");
            } else if (authOnly) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "authentication-only mode; ");
            } else {
                System.out.println("\t\t[LdapLoginModule] "
                        + "search-first mode; ");
            }
        }
        if(useSSL) {
            System.out.println("SSL enabled; " + (verifyCertificate ? "Verifying certicates" : "_CERTIFACTE VERIFICATION DISABLED_"));
        } else {
            System.out.println("SSL disabled");
        }
    }

    /**
     * Begin user authentication.
     *
     * <p>
     * Acquire the user's credentials and verify them against the specified LDAP
     * directory.
     *
     * @return true always, since this <code>LoginModule</code> should not be
     * ignored.
     * @exception FailedLoginException if the authentication fails.
     * @exception LoginException if this <code>LoginModule</code> is unable to
     * perform the authentication.
     */
    public boolean login() throws LoginException {

        if (userProvider == null) {
            throw new LoginException("Unable to locate the LDAP directory service");
        }

        if (debug) {
            System.out.println("\t\t[LdapLoginModule] user provider: "
                    + userProvider);
        }

        // attempt the authentication
        if (tryFirstPass) {

            try {
                // attempt the authentication by getting the
                // username and password from shared state
                attemptAuthentication(true);

                // authentication succeeded
                succeeded = true;
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "tryFirstPass succeeded");
                }
                return true;

            } catch (LoginException le) {
                // authentication failed -- try again below by prompting
                cleanState();
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "tryFirstPass failed: " + le.toString());
                }
            }

        } else if (useFirstPass) {

            try {
                // attempt the authentication by getting the
                // username and password from shared state
                attemptAuthentication(true);

                // authentication succeeded
                succeeded = true;
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "useFirstPass succeeded");
                }
                return true;

            } catch (LoginException le) {
                // authentication failed
                cleanState();
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "useFirstPass failed");
                }
                throw le;
            }
        }

        // attempt the authentication by prompting for the username and pwd
        try {
            attemptAuthentication(false);

            // authentication succeeded
            succeeded = true;
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "authentication succeeded");
            }
            return true;

        } catch (LoginException le) {
            cleanState();
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "authentication failed");
            }
            throw le;
        }
    }

    /**
     * Complete user authentication.
     *
     * <p>
     * This method is called if the LoginContext's overall authentication
     * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules succeeded).
     *
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by
     * retrieving the private state saved by the <code>login</code> method),
     * then this method associates an <code>LdapPrincipal</code> and one or more
     * <code>UserPrincipal</code>s with the <code>Subject</code> located in the
     * <code>LoginModule</code>. If this LoginModule's own authentication
     * attempted failed, then this method removes any state that was originally
     * saved.
     *
     * @exception LoginException if the commit fails
     * @return true if this LoginModule's own login and commit attempts
     * succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException {

        if (succeeded == false) {
            return false;
        } else {
            if (subject.isReadOnly()) {
                cleanState();
                throw new LoginException("Subject is read-only");
            }
            // add Principals to the Subject
            Set principals = subject.getPrincipals();

            if (!this.noLdapPrincipal) {
                if (!principals.contains(ldapPrincipal)) {
                    principals.add(ldapPrincipal);
                }
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "added LdapPrincipal \""
                            + ldapPrincipal
                            + "\" to Subject");
                }
            } else if (debug) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "omit adding ldap principal \""
                        + ldapPrincipal
                        + "\" to Subject");
            }

            if (!principals.contains(userPrincipal)) {
                principals.add(userPrincipal);
            }
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "added UserPrincipal \""
                        + userPrincipal
                        + "\" to Subject");
            }

            if (authzPrincipal != null
                    && (!principals.contains(authzPrincipal))) {
                principals.add(authzPrincipal);

                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "added User(authz)Principal \""
                            + authzPrincipal
                            + "\" to Subject");
                }
            }
        }
        // in any case, clean out state
        cleanState();
        commitSucceeded = true;
        return true;
    }

    /**
     * Abort user authentication.
     *
     * <p>
     * This method is called if the overall authentication failed. (the relevant
     * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did not
     * succeed).
     *
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by
     * retrieving the private state saved by the <code>login</code> and
     * <code>commit</code> methods), then this method cleans up any state that
     * was originally saved.
     *
     * @exception LoginException if the abort fails.
     * @return false if this LoginModule's own login and/or commit attempts
     * failed, and true otherwise.
     */
    public boolean abort() throws LoginException {
        if (debug) {
            System.out.println("\t\t[LdapLoginModule] "
                    + "aborted authentication");
        }

        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {

            // Clean out state
            succeeded = false;
            cleanState();

            ldapPrincipal = null;
            userPrincipal = null;
            authzPrincipal = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    /**
     * Logout a user.
     *
     * <p>
     * This method removes the Principals that were added by the
     * <code>commit</code> method.
     *
     * @exception LoginException if the logout fails.
     * @return true in all cases since this <code>LoginModule</code> should not
     * be ignored.
     */
    public boolean logout() throws LoginException {
        if (subject.isReadOnly()) {
            cleanState();
            throw new LoginException("Subject is read-only");
        }
        Set principals = subject.getPrincipals();
        principals.remove(ldapPrincipal);
        principals.remove(userPrincipal);
        if (authzIdentity != null) {
            principals.remove(authzPrincipal);
        }

        // clean out state
        cleanState();
        succeeded = false;
        commitSucceeded = false;

        ldapPrincipal = null;
        userPrincipal = null;
        authzPrincipal = null;

        if (debug) {
            System.out.println("\t\t[LdapLoginModule] logged out Subject");
        }
        return true;
    }

    /**
     * Attempt authentication
     *
     * @param getPasswdFromSharedState boolean that tells this method whether to
     * retrieve the password from the sharedState.
     * @exception LoginException if the authentication attempt fails.
     */
    private void attemptAuthentication(boolean getPasswdFromSharedState)
            throws LoginException {

        // first get the username and password
        getUsernamePassword(getPasswdFromSharedState);

        if (password == null || password.length == 0) {
            throw (LoginException) new FailedLoginException("No password was supplied");
        }

        String dn = "";

        if (authFirst || authOnly) {

            String id = replaceUsernameToken(identityMatcher, authcIdentity);

           

            try {
                // Connect to the LDAP server (using simple bind)
                //ldapEnvironment.put("java.naming.ldap.factory.socket", BlindSSLSocketFactory.class.getName());
                ctx = new InitialLdapContext(ldapEnvironment, null);

                // Start TLS
                StartTlsResponse tls =
                    (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
                
                tls.negotiate();
                
                // Prepare to bind using user's username and password
                ldapEnvironment.put(Context.SECURITY_CREDENTIALS, password);
                ldapEnvironment.put(Context.SECURITY_PRINCIPAL, id);

                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "attempting to authenticate to LDAP: " + id);

                    System.out.println("\t\t[LdapLoginModule] "
                            + "attempting to authenticate user: " + username);
                
                
                }
                
                //force bind over tls connection
                ctx.reconnect(null);

                tls.close();
                
            } catch (NamingException e) {
                
                throw (LoginException) new FailedLoginException("Cannot bind to LDAP server")
                        .initCause(e);
            } catch(IOException e) {
                throw (LoginException) new FailedLoginException("Cannot communicate with LDAP server")
                        .initCause(e);
            }

            // Authentication has succeeded
            // Locate the user's distinguished name
            if (userFilter != null) {
                dn = findUserDN(ctx);
            } else {
                dn = id;
            }

        } else {

            try {
                // Connect to the LDAP server (using anonymous bind)
                ctx = new InitialLdapContext(ldapEnvironment, null);

            } catch (NamingException e) {
                throw (LoginException) new FailedLoginException("Cannot connect to LDAP server")
                        .initCause(e);
            }

            // Locate the user's distinguished name
            dn = findUserDN(ctx);

            try {

                // Prepare to bind using user's distinguished name and password
                ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);

                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] "
                            + "attempting to authenticate user: " + username);
                }
                // Connect to the LDAP server (using simple bind)
                ctx.reconnect(null);

                // Authentication has succeeded
            } catch (NamingException e) {
                throw (LoginException) new FailedLoginException("Cannot bind to LDAP server")
                        .initCause(e);
            }
        }

        // Save input as shared state only if authentication succeeded
        if (storePass
                && !sharedState.containsKey(USERNAME_KEY)
                && !sharedState.containsKey(PASSWORD_KEY)) {
            sharedState.put(USERNAME_KEY, username);
            sharedState.put(PASSWORD_KEY, password);
        }

        // Create the user principals
        userPrincipal = new UserPrincipal(username);
        if (authzIdentity != null) {
            authzPrincipal = new UserPrincipal(authzIdentity);
        }

        try {

            ldapPrincipal = new LdapPrincipal(dn);

        } catch (InvalidNameException e) {
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "cannot create LdapPrincipal: bad DN");
            }
            throw (LoginException) new FailedLoginException("Cannot create LdapPrincipal")
                    .initCause(e);
        }
    }

    /**
     * Search for the user's entry. Determine the distinguished name of the
     * user's entry and optionally an authorization identity for the user.
     *
     * @param ctx an LDAP context to use for the search
     * @return the user's distinguished name or an empty string if none was
     * found.
     * @exception LoginException if the user's entry cannot be found.
     */
    private String findUserDN(LdapContext ctx) throws LoginException {

        String userDN = "";

        // Locate the user's LDAP entry
        if (userFilter != null) {
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "searching for entry belonging to user: " + username);
            }
        } else {
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] "
                        + "cannot search for entry belonging to user: " + username);
            }
            throw (LoginException) new FailedLoginException("Cannot find user's LDAP entry");
        }

        try {
            NamingEnumeration results = ctx.search("",
                    replaceUsernameToken(filterMatcher, userFilter), constraints);

            // Extract the distinguished name of the user's entry
            // (Use the first entry if more than one is returned)
            if (results.hasMore()) {
                SearchResult entry = (SearchResult) results.next();

                // %%% - use the SearchResult.getNameInNamespace method
                //        available in JDK 1.5 and later.
                //        (can remove call to constraints.setReturningObjFlag)
                userDN = ((Context) entry.getObject()).getNameInNamespace();

                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] found entry: "
                            + userDN);
                }

                // Extract a value from user's authorization identity attribute
                if (authzIdentityAttr != null) {
                    Attribute attr
                            = entry.getAttributes().get(authzIdentityAttr);
                    if (attr != null) {
                        Object val = attr.get();
                        if (val instanceof String) {
                            authzIdentity = (String) val;
                        }
                    }
                }

                results.close();

            } else // Bad username
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] user's entry "
                        + "not found");
            }

        } catch (NamingException e) {
            // ignore
        }

        if (userDN.equals("")) {
            throw (LoginException) new FailedLoginException("Cannot find user's LDAP entry");
        } else {
            return userDN;
        }
    }

    /**
     * Replace the username token
     *
     * @param string the target string
     * @return the modified string
     */
    private String replaceUsernameToken(Matcher matcher, String string) {
        return matcher != null ? matcher.replaceAll(username) : string;
    }

    /**
     * Get the username and password. This method does not return any value.
     * Instead, it sets global name and password variables.
     *
     * <p>
     * Also note that this method will set the username and password values in
     * the shared state in case subsequent LoginModules want to use them via
     * use/tryFirstPass.
     *
     * @param getPasswdFromSharedState boolean that tells this method whether to
     * retrieve the password from the sharedState.
     * @exception LoginException if the username/password cannot be acquired.
     */
    private void getUsernamePassword(boolean getPasswdFromSharedState)
            throws LoginException {

        if (getPasswdFromSharedState) {
            // use the password saved by the first module in the stack
            username = (String) sharedState.get(USERNAME_KEY);
            password = (char[]) sharedState.get(PASSWORD_KEY);
            return;
        }

        // prompt for a username and password
        if (callbackHandler == null) {
            throw new LoginException("No CallbackHandler available "
                    + "to acquire authentication information from the user");
        }

        Callback[] callbacks = new Callback[2];
        try {
            callbacks[0] = new NameCallback(rb.getString("username: "));
        } catch (MissingResourceException ex) {
            System.out.println("Failed to find resource, falling back to [username.]");
            callbacks[0] = new NameCallback(rb.getString("username."));
        }
        try {
            callbacks[1] = new PasswordCallback(rb.getString("password: "), false);
        } catch (MissingResourceException ex) {
            System.out.println("Failed to find resource, falling back to [password.]");
            callbacks[1] = new PasswordCallback(rb.getString("password."), false);
        }

        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
            password = new char[tmpPassword.length];
            System.arraycopy(tmpPassword, 0,
                    password, 0, tmpPassword.length);
            ((PasswordCallback) callbacks[1]).clearPassword();

        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());

        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString()
                    + " not available to acquire authentication information"
                    + " from the user");
        }
    }

    /**
     * Clean out state because of a failed authentication attempt
     */
    private void cleanState() {
        username = null;
        if (password != null) {
            Arrays.fill(password, ' ');
            password = null;
        }
        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch (NamingException e) {
            // ignore
        }
        ctx = null;

        if (clearPass) {
            sharedState.remove(USERNAME_KEY);
            sharedState.remove(PASSWORD_KEY);
        }
    }


    /**
     * http://huikaucom.blogspot.it/2006/05/disable-cert-validation-for-ldap-and.html
     */
    public static class BlindSSLSocketFactory extends SocketFactory {
        private static SocketFactory blindFactory = null;

        /**
         * Builds an all trusting "blind" ssl socket factory.
         */
        static {
                // create a trust manager that will purposefully fall down on the
                // job
                TrustManager[] blindTrustMan = new TrustManager[] { new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] c, String a) { }
                        public void checkServerTrusted(X509Certificate[] c, String a) { }
                } };

                // create our "blind" ssl socket factory with our lazy trust manager
                try {
                        SSLContext sc = SSLContext.getInstance("SSL");
                        sc.init(null, blindTrustMan, new java.security.SecureRandom());
                        blindFactory = sc.getSocketFactory();
                } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                }
        }

        /**
         * @see javax.net.SocketFactory#getDefault()
         */
        public static SocketFactory getDefault() {
                return new BlindSSLSocketFactory();
        }


        /**
         * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
         */
        public Socket createSocket(String arg0, int arg1) throws IOException,
                        UnknownHostException {
                return blindFactory.createSocket(arg0, arg1);
        }

        /**
         * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
         */
        public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
                return blindFactory.createSocket(arg0, arg1);
        }

        /**
         * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
         *      java.net.InetAddress, int)
         */
        public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
                        throws IOException, UnknownHostException {
                return blindFactory.createSocket(arg0, arg1, arg2, arg3);
        }

        /**
         * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
         *      java.net.InetAddress, int)
         */
        public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
                        int arg3) throws IOException {
                return blindFactory.createSocket(arg0, arg1, arg2, arg3);
        }
    }
}
