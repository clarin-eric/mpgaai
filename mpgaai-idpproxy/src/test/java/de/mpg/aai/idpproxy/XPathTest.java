package de.mpg.aai.idpproxy;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import junit.framework.JUnit4TestAdapter;

import org.junit.Ignore;
import org.w3c.dom.Document;

import de.mpg.aai.idpproxy.saml.SamlNamespaceContext;


public class XPathTest {
	
	
	public static junit.framework.Test suite() {
		  return new JUnit4TestAdapter(XPathTest.class);
	}
	
	
//	@Test
	@Ignore
	public void getNameID() throws Exception {
		DocumentBuilderFactory domFactory = 
		DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse("/tmp/me/samlAttbStmt-idpproxy.xml");
		XPath xpath = XPathFactory.newInstance().newXPath();
		NamespaceContext nsc = new SamlNamespaceContext();
		xpath.setNamespaceContext(nsc);
		
		// XPath Query for showing all nodes value
		String result = null;
		String value = "megger";
		result = xpath.evaluate("string(//saml:AttributeValue[text()='"+value+"']/@Scope)", doc);
		System.out.println(result);
	
		value ="mytest";
//		result = xpath.evaluate("string(//saml:AttributeValue/saml:NameID[text()='"+value+"']/@NameQualifier)", doc);
//		System.out.println(result);

		result = xpath.evaluate("string(//saml:NameID[text()='"+value+"']/@NameQualifier)", doc);
		System.out.println(result);
		
		
		xpath.compile("//Employee[City='Haldwani']/name/text()");
//		XPathExpression expr = xpath.compile("//saml:AttributeValue[saml:NameID='mytest']/name/text()");
		
//		Object result = expr.evaluate(doc, XPathConstants.NODESET);
//		NodeList nodes = (NodeList) result;
//		for (int i = 0; i < nodes.getLength(); i++) {
//		 System.out.println(nodes.item(i).getNodeValue()); 
//		}
	}
}
