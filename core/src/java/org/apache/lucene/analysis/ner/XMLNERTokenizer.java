package org.apache.lucene.analysis.ner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizerImpl;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.EntityInstanceAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.BytesRef;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class XMLNERTokenizer extends Tokenizer {

	private NodeList xmlNodes;
	private int tokenNum = -1;
	private int startOffset = 0;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final EntityInstanceAttribute entityAtt = addAttribute(EntityInstanceAttribute.class);
	private final PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);
	
	private String readToString()
	{
		String text = "";
		try
		{
			// CITATION: http://www.baeldung.com/java-convert-reader-to-string
			int intValueOfChar;
		    while ((intValueOfChar = input.read()) != -1)
		    {
		        text += (char) intValueOfChar;
		    }
		}
		catch(IOException e)
		{
			System.out.println("drf");
		}
		return text;
	}
	
	private void init()
	{
		/*
		try
		{
			//this.scanner = new StandardTokenizerImpl(input);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringBuilder xmlStringBuilder = new StringBuilder();
			System.out.println("EY" + readToString());
			xmlStringBuilder.append(input);
			ByteArrayInputStream xmlbytes =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document xmldoc = builder.parse(xmlbytes);
			xmlNodes = xmldoc.getElementsByTagName("wi");
		}
		catch(Exception e)
		{
			System.out.println("XMLNERTokenizer Init Fail");
			e.printStackTrace();
		}
		*/
	}

	public XMLNERTokenizer()
	{
		init();
	}
	
	private void parseXML()
	{
		if(tokenNum != -1)
			return;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringBuilder xmlStringBuilder = new StringBuilder();
			String xmlString = "<?xml version='1.0' encoding='utf-8'?><content>" + readToString() + "</content>";
			System.out.println(xmlString);
			xmlStringBuilder.append(xmlString);
			ByteArrayInputStream xmlbytes =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document xmldoc = builder.parse(xmlbytes);
			xmlNodes = xmldoc.getElementsByTagName("wi");
			tokenNum = 0;
		}
		catch(Exception e)
		{
			System.out.println("XMLParsing Error");
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean incrementToken() throws IOException
	{
		parseXML();
		clearAttributes();
		try
		{
			posIncrAtt.setPositionIncrement(1);
			Element xmlElement = (Element)xmlNodes.item(tokenNum);
			String textContent = xmlElement.getTextContent();
			String entityClass = xmlElement.getAttribute("entity");
			if(!entityClass.equals("O"))
			{
				char[] originalChars = textContent.toCharArray();
				entityAtt.copyBuffer(originalChars, 0, originalChars.length);
				payAtt.setPayload(new BytesRef(textContent.getBytes(), 0, originalChars.length));
				textContent = "entity_" + entityClass;
				System.out.println(textContent);
			}
			char[] tokChars = textContent.toCharArray();
			termAtt.copyBuffer(tokChars, 0, tokChars.length);
			typeAtt.setType(StandardTokenizer.TOKEN_TYPES[0]);
			offsetAtt.setOffset(startOffset, startOffset+termAtt.length());
			startOffset += termAtt.length();
			//termAtt.append(xmlElement.getTextContent());
			tokenNum++;
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	@Override
	public void reset() throws IOException
	{
		super.reset();
		xmlNodes = null;
		tokenNum = -1;
		startOffset = 0;
	}

}
