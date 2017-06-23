package org.apache.lucene.analysis.ner;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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

import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.aksw.fox.Fox;
import org.aksw.fox.data.Entity;

import org.apache.lucene.analysis.PostingsArrayInstance;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class FOXNERTokenizer extends Tokenizer {

	private int tokenNum = -1;
	private int startOffset = 0;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final EntityInstanceAttribute entityAtt = addAttribute(EntityInstanceAttribute.class);
	private final PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);
	
	private Fox fox;
	private Map<String, String> FOXParams;
	
	private ArrayList<TokenContent> fieldTokens;
	
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
			System.out.println("Failed to read field to string");
		}
		return text;
	}
	
	private void init()
	{
		//System.setProperty("user.dir", "/Users/alexaulabaugh/Desktop/Research Summer 17/NER/FOX");
		System.out.println(System.getProperty("user.dir"));
		//Spin-up the FOX classifier
		fox = new Fox(Fox.Langs.EN.toString());
		FOXParams = fox.getDefaultParameter();
		FOXParams.put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString());
		FOXParams.put(Fox.Parameter.TASK.toString(), Fox.Task.RE.toString());//Fox.Task.NER.toString());
		FOXParams.put(Fox.Parameter.OUTPUT.toString(), "RDF/XML");
		FOXParams.put(Fox.Parameter.FOXLIGHT.toString(), "OFF");
	}

	public FOXNERTokenizer()
	{
		fieldTokens = new ArrayList<TokenContent>();
		init();
	}
	
	private String classify(String rawInput)
	{
		System.out.println(rawInput);
		FOXParams.put(Fox.Parameter.INPUT.toString(), rawInput);
		fox.setParameter(FOXParams);
		fox.run();
		return fox.getResults();
	}
	
	private void parseXML(String xmlString)
	{
		try
		{
			System.out.println(xmlString);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append(xmlString);
			ByteArrayInputStream xmlbytes =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document xmldoc = builder.parse(xmlbytes);
			NodeList xmlNodes = xmldoc.getElementsByTagName("rdf:RDF").item(0).getChildNodes();
			for(int i = 0; i < xmlNodes.getLength(); i++)
			{
				Node node = xmlNodes.item(i);
				NodeList sublist = node.getChildNodes();
				String nodeName = node.getNodeName();
				if(nodeName != "#text")
				{
					TokenContent entityToken = new TokenContent();
					System.out.println(nodeName);
					entityToken.setEntityType(nodeName.replace("scmsann:", ""));
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
							
							switch(subnodeName)
							{
								case("ann:body"):
									entityToken.setText(subnodeText);
									break;
								case("scms:means"):
									if(!subnodeAttr.contains("scms.eu"))
										entityToken.setReference(subnodeAttr);
									break;
								case("scms:endIndex"):
									entityToken.setEndIndex(Integer.parseInt(subnodeText));
									break;
								case("scms:beginIndex"):
									entityToken.setBeginIndex(Integer.parseInt(subnodeText));
							}
						}
					}
					fieldTokens.add(entityToken);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("XMLParsing Error");
			e.printStackTrace();
		}
	}
	
	private void generateTokens(String rawInput)
	{
		ArrayList<TokenContent> nonEntityTokens = new ArrayList<TokenContent>();
		int entityTokenIndex = 0;
		int inputPointer = 0;
		int beginPointer = 0;
		Collections.sort(fieldTokens);
		TokenContent earliestEntity = null;
		if(fieldTokens.size() > entityTokenIndex)
		{
			earliestEntity = fieldTokens.get(entityTokenIndex);
		}
		String tokenString = "";
		while(inputPointer < rawInput.length())
		{
			if(earliestEntity != null && earliestEntity.getBeginIndex() == inputPointer)
			{
				if(tokenString.length() > 0)
				{
					TokenContent newToken = new TokenContent(tokenString, null, null, beginPointer, inputPointer);
					nonEntityTokens.add(newToken);
				}
				tokenString = "";
				inputPointer = earliestEntity.getEndIndex()+1;
				beginPointer = inputPointer;
				entityTokenIndex++;
				if(fieldTokens.size() > entityTokenIndex)
				{
					earliestEntity = fieldTokens.get(entityTokenIndex);
				}
				else
				{
					earliestEntity = null;
				}
			}
			else if(!Character.isLetterOrDigit(rawInput.charAt(inputPointer)))
			{
				if(tokenString.length() > 0)
				{
					TokenContent newToken = new TokenContent(tokenString, null, null, beginPointer, inputPointer);
					nonEntityTokens.add(newToken);
				}
				tokenString = "";
				inputPointer++;
				beginPointer = inputPointer;
			}
			else
			{
				tokenString = tokenString + rawInput.charAt(inputPointer);
				inputPointer++;
			}
		}
		fieldTokens.addAll(nonEntityTokens);
		Collections.sort(fieldTokens);
	}
	
	@Override
	public boolean incrementToken() throws IOException
	{
		if(tokenNum == -1)
		{
			String rawInput = readToString();
			String xmlString = classify(rawInput);
			parseXML(xmlString);
			generateTokens(rawInput);
			System.out.println("TOKENS: " + fieldTokens.size());
			tokenNum++;
		}
		clearAttributes();
		try
		{
			posIncrAtt.setPositionIncrement(1);
			TokenContent currentTok = fieldTokens.get(tokenNum);
			String textContent = currentTok.getText();
			String entityType = currentTok.getEntityType();
			if(entityType != null)
			{
				PostingsArrayInstance postingsFormatter = new PostingsArrayInstance();
				postingsFormatter.addField("OriginalText", textContent, "str");
				if(currentTok.getReference() != null)
					postingsFormatter.addField("DBPediaLink", currentTok.getReference(), "str");
				payAtt.setPayload(postingsFormatter.toBytesRef());
				textContent = "entity_" + entityType;
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
		fieldTokens = null;
		fieldTokens = new ArrayList<TokenContent>();
		tokenNum = -1;
		startOffset = 0;
	}
	

}

