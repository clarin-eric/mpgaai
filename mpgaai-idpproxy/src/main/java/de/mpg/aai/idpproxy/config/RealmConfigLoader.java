package de.mpg.aai.idpproxy.config;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RealmConfigLoader {
	
	
	public static void LoadConfig(ConfigContext context) throws ConfigException
	{
		try{
			File file = new File(context.path+"/realm.xml");
			DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFact.newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			
			Element element = doc.getDocumentElement();
			NodeList roleNodeList = element.getElementsByTagName("Role");
			RealmConfig config = context.getRealm();
			for(int x = 0 ; x < roleNodeList.getLength() ; x++) {
				Element roleElement = (Element)roleNodeList.item(x);
				config.getRoles().add(roleElement.getAttribute("name"));
			}
		}catch(Exception e) {
			throw new ConfigException(e.getMessage(),e);
		}
	}
}
