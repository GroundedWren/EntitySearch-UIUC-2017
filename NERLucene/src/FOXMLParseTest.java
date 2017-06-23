

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class FOXMLParseTest
{
	public static void main(String[] args) throws Exception
	{
		String xmlString = new String(Files.readAllBytes(Paths.get("/Users/alexaulabaugh/Desktop/testXML.txt")));
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringBuilder xmlStringBuilder = new StringBuilder();
			//System.out.println(xmlString);
			xmlStringBuilder.append(xmlString);
			ByteArrayInputStream xmlbytes =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document xmldoc = builder.parse(xmlbytes);
			NodeList xmlNodes = xmldoc.getElementsByTagName("rdf:RDF").item(0).getChildNodes();
			for(int i = 0; i < xmlNodes.getLength(); i++)
			{
				Node node = xmlNodes.item(i);
				NodeList sublist = node.getChildNodes();
				String nodeName = node.getNodeName();
				System.out.println(nodeName);
				for(int j = 0; j< sublist.getLength(); j++)
				{
					Node subnode = sublist.item(j);
					if(subnode.getNodeName() != "#text")
					{
						String subnodeName = subnode.getNodeName();
						String subnodeAttr = subnode.getAttributes().item(0).getNodeValue();
						String subnodeText = subnode.getTextContent();
						System.out.println("\t" + subnodeName);
						System.out.println("\t\t" + subnodeAttr);
						System.out.println("\t\t" + subnodeText);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("XMLParsing Error");
			e.printStackTrace();
		}
	}
}
