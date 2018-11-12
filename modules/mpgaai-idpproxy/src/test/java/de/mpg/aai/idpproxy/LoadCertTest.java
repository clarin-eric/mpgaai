package de.mpg.aai.idpproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.ssl.PKCS8Key;
import org.junit.Ignore;
import org.junit.Test;

import de.mpg.aai.idpproxy.config.ConfigException;
import de.mpg.aai.idpproxy.config.ConfigLoader;


public class LoadCertTest {

	
	public static junit.framework.Test suite() {
		  return new JUnit4TestAdapter(LoadCertTest.class);
	}
	
	
//	@Test
	@Ignore
	public void loadKeys() {
		
	}


	@Test
	@Ignore
	public void loadPrivateKey() throws ConfigException {
		try{
			String path = "/home/megger/tmp/idpproxy-test/";	// configCtx.getLocation().toString();
			
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			InputStream inputStream = ConfigLoader.class.getResourceAsStream("/PrivateKey.pkcs");
			if(inputStream == null) {
//				log.debug(path + "/PrivateKey.pkcs");
				inputStream = new FileInputStream(path + "/PrivateKey.pkcs");
			}
			String password = "changeit";
			keyStore.load(inputStream, password.toCharArray());
			Enumeration<String> aliases = keyStore.aliases();
			PrivateKey key = (PrivateKey)keyStore.getKey(aliases.nextElement(), "changeit".toCharArray());
			
			keyStore = KeyStore.getInstance("JKS");
			inputStream = ConfigLoader.class.getResourceAsStream("/PublicKey.jks");
			if(inputStream == null) {
//				log.debug(path + "/PublicKey.jks");
				inputStream = new FileInputStream(path + "/PublicKey.jks");
			}
			keyStore.load(inputStream, password.toCharArray());
			String alias = "idpproxy";
			Certificate cert = keyStore.getCertificate(alias);
			if(cert == null) {
				String oldAlias = "Shib2Proxy";
				cert = keyStore.getCertificate(oldAlias);
//				if(cert != null)
//					log.warn("found certificate with deprecated alias {}, expecting alias {}", new Object[] {oldAlias, alias});
			}
			
			System.out.println(cert.toString());
			System.out.println(key.toString());
			
//			if(cert == null)
//				log.error("could not find certificate in keystore");
//			configCtx.setMyCert(cert);
//			if(key == null)
//				log.error("could not find (certificate) key in keystore");
//			configCtx.setMyKey(key);
		} catch(Exception e) {
			throw new ConfigException(e.getLocalizedMessage(), e);
		}
	}

	@Test
	public void loadX509() {
		
		String privKeyPath = "/home/megger/tmp/idpproxy-test/services.aai.mpg.de.key";
		String certPath = "/home/megger/tmp/idpproxy-test/services.aai.mpg.de.pem2";
        char[] password = null;
        
/*		
		// google:
		InputStream dis = new DataInputStream(new FileInputStream());
		byte[] privKeyBytes = new byte[(int)privKeyFile.length()];
		dis.read(privKeyBytes);
		dis.close();
		// load private key
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		BASE64Decoder b64 = new BASE64Decoder();
		PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(b64.decodeBuffer(privKeyBytes.toString()));
		RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        // decode private key
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
        RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
*/        
		
		// internet2:
        try {
        	// load private key
            FileInputStream fins = new FileInputStream(privKeyPath);
            byte[] encoded = new byte[fins.available()];
            fins.read(encoded);
            fins.close();
	        PKCS8Key key0 = new PKCS8Key(encoded, password);
	        PrivateKey pkey0 = key0.getPrivateKey();

            // load pk-2
        	File file = new File(privKeyPath); 
            long numOfBytes = file.length();
            if (numOfBytes > Integer.MAX_VALUE) {
                throw new IOException("File is to large to be read in to a byte array");
            }
            byte[] bytes = new byte[(int) numOfBytes];
            FileInputStream ins = new FileInputStream(file);
            int offset = 0;
            int numRead = 0;
            do{
                numRead = ins.read(bytes, offset, bytes.length - offset);
                offset += numRead;
            }while(offset < bytes.length && numRead >= 0);
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            ins.close();
            
	        PKCS8Key key1 = new PKCS8Key(bytes, password);
	        PrivateKey pkey1 = key1.getPrivateKey();
            
            boolean eqKey = bytes.equals(encoded);
            boolean eqPKey = pkey1.equals(pkey0);
			System.out.println(key0.toString());
			System.out.println(key1.toString());
            
            
	        // load "certificate" / public key
			InputStream in = new FileInputStream(certPath);
			CertificateFactory certFac = CertificateFactory.getInstance("X.509");
			Certificate cert = certFac.generateCertificate(in);
			
			System.out.println(cert.toString());
			System.out.println(key0.toString());
			
		} catch(Exception e) {
        	e.printStackTrace();
		}
		
		
	}

	

//	private void loadAsBase64(ConfigContext configCtx, Element parent) {
//		NodeList nodes = parent.getElementsByTagName(tagname);
//		Element target = null;
//		String value = null;
//		if(nodes != null && nodes.getLength() < 0)
//			target = (Element) nodes.item(0);
//		if(target != null)
//			value = target.getTextContent();
//		if(value == null || value.trim().isEmpty())
//			throw new ConfigException("could not find (value for) idpproxy configuration tag " 
//					+ tagname + " in " + parent.getTagName());
//		value = value.trim();
//	
//		try {
//			if(value.contains("BEGIN CERTIFICATE"))
//				value = value.substring(value.indexOf("\n"));
//			if(value.contains("END CERTIFICATE"))
//				value = value.substring(0, value.lastIndexOf("\n"));
//	//		Certificate cert = new X509CertImpl(Base64.decode(value));
//	//		Certificate cert = new X509Certificate(value);
//			
//			configCtx.setMyCert(cert);
//		} catch(CertificateException cE) {
//			throw new ConfigException("could not load idpproxy certificate ", cE);
//		} catch(Base64DecodingException bdE) {
//			throw new ConfigException("could not (base64)decode given idpproxy certificate", bdE);
//		}
//	}
}
