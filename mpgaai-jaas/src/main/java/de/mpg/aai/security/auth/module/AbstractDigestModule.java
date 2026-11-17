package de.mpg.aai.security.auth.module;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;


/**
 * abstract base class for LoginModule implementations which use digesting 
 * @author megger
 *
 */
public abstract class AbstractDigestModule {
	/** reserved flag(value) to indicate no-digesting / use plain strings */
	public static final String	DIGEST_PLAIN =	"plain";
	/** encoding in hexadecimal */
	public static final String	ENCODING_HEX =	"hex";
	/** encoding in base-64 */
	public static final String	ENCODING_B64 =	"base64";
	
	/** the logger */
//	private static Logger log = LoggerFactory.getLogger(AbstractDigestModule.class);
	
	/** algorithm used for passwd digest */
	private String		digest;
	/** encoding used for digested pw, default is "hex" */
	private String		encoding;
	
	/**
	 * default constructor
	 */
	public AbstractDigestModule() {
	}
	
	/**
	 * @param digest the digest algorithm
	 */
	protected void setDigest(String val) {
		this.digest = this.ensureValue(val, "SHA");
		if(this.digest != null
		&& !DIGEST_PLAIN.equalsIgnoreCase(this.digest)) {
			try {
				MessageDigest.getInstance(this.digest);
			} catch(NoSuchAlgorithmException nsE) {
				throw new IllegalArgumentException("initialization failed: digest algorithm not available: " + this.digest, nsE);
			}
		}
	}
	/**
	 * @param encoding
	 */
	protected void setEncoding(String val) {
		this.encoding = this.ensureValue(val, ENCODING_B64);
		if(this.encoding != null 
		&& !ENCODING_HEX.equalsIgnoreCase(this.encoding)
		&& !ENCODING_B64.equalsIgnoreCase(this.encoding)) {
			throw new IllegalArgumentException("initialization failed, endocing not supported: " + this.encoding);
		}
	}
	
	private String ensureValue(String val, String defaultVal) {
		if(val == null)
			return defaultVal;
		String result = val.trim();
		return result.isEmpty() ? defaultVal : result;
	}
	
	protected void init(Map<String,?>  options) {
		String val = (String) options.get("digest");
		this.setDigest(val);
		val = (String) options.get("encoding");
		this.setEncoding(val);
	}

	/**
	 * This method checks if the provided password is correct.  The original password may have been digested.
	 * @param expected	 Original password in digested form if applicable
	 * @param provided User provided password in clear text
	 * @return true	 If the password is correct
	 */
	protected boolean check(String expected, String provided) {
		if(expected == null && provided == null)	// both null <=> ok
			return true;
		if(expected == null || provided == null)	// one of both is null => invalid
			return false;
		String encPw;
		try {
			encPw = this.encode(provided);
			return expected.equals(encPw);
		} catch(NoSuchAlgorithmException nsaE) {
			// should not occur, algorithm availability should've been checked at initialization
//			log.error("failed to encode properly during pw-check: " + nsaE.getMessage());
			return false;
		} catch(RuntimeException rtE) {
			// should not occur, digest/encoding availability should've been checked at initialization
//			log.error("failed to encode properly during pw-check: " + rtE.getMessage());
			return false;
		}
	}
	
	
	/**
	 * digests and encodes the given providedPw with the given digest-algorithm in the given encoding 
	 * @param target provided password to digest and encode
	 * @param dig destined digest algorithm
	 * @param enc destined encoding
	 * @return providedPw digested and encoded 
	 * @throws NoSuchAlgorithmException if given digest algorithm is not recognized 
	 * @throws IllegalArgumentException if given encoding is not recognized 
	 */
	protected String encode(String target) throws NoSuchAlgorithmException {
		// no digest algorithm used: direct comparison
		if(!this.shallDigest())
			return target;
		// digest...
		MessageDigest md = MessageDigest.getInstance(this.digest);
		byte[] digested = md.digest(target.getBytes());
		// ... & encode
		//	hex encoding as default
		if(this.encoding == null || ENCODING_HEX.equalsIgnoreCase(this.encoding)) {
//			byte[] hexData = new byte[digestedPw.length * 2];
//			HexTranslator ht = new HexTranslator();
//			ht.encode(digestedPw, 0, digestedPw.length, hexData, 0);
//			return new String(hexData);
			return new String(Hex.encodeHex(digested));
		}
		if(ENCODING_B64.equalsIgnoreCase(this.encoding)) {
			return new String(Base64.encodeBase64(digested));
		}
		// should not occur, encoding availability should've been checked at initialization
		throw new IllegalArgumentException("no handler for encoding: " + this.encoding);
	}
	
	/**
	 * checks whether an actual digester is configured
	 * @return false if {@link #digest} is null/empty or {@link #DIGEST_PLAIN} (ignore case)
	 */
	private boolean shallDigest() {
		return this.digest != null 
			&& !this.digest.isEmpty()
			&& !DIGEST_PLAIN.equalsIgnoreCase(this.digest);
	}
}
