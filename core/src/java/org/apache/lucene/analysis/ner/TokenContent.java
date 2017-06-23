package org.apache.lucene.analysis.ner;

import java.util.Comparator;

public class TokenContent implements Comparator<TokenContent>, Comparable<TokenContent>
{
	private String text;
	private String entityType;
	private String reference;
	private int beginIndex;
	private int endIndex;
	
	public TokenContent()
	{
		text = null;
		entityType = null;
		reference = null;
		beginIndex = -1;
		endIndex = -1;
	}
	
	public TokenContent(String text_in, String entityType_in, String reference_in, int beginIndex_in, int endIndex_in)
	{
		text = text_in;
		entityType = entityType_in;
		reference = reference_in;
		beginIndex = beginIndex_in;
		endIndex = endIndex_in;
	}
	
	public void setText(String text_in)
	{
		text = text_in;
	}
	
	public String getText()
	{
		return text;
	}
	
	public void setEntityType(String entityType_in)
	{
		entityType = entityType_in;
	}
	
	public String getEntityType()
	{
		return entityType;
	}
	
	public void setReference(String reference_in)
	{
		reference = reference_in;
	}
	
	public String getReference()
	{
		return reference;
	}
	
	public void setBeginIndex(int beginIndex_in)
	{
		beginIndex = beginIndex_in;
	}
	
	public int getBeginIndex()
	{
		return beginIndex;
	}
	
	public void setEndIndex(int endIndex_in)
	{
		endIndex = endIndex_in;
	}
	
	public int getEndIndex()
	{
		return endIndex;
	}

	@Override
	public int compareTo(TokenContent o)
	{
		return beginIndex - o.getBeginIndex();
	}

	@Override
	public int compare(TokenContent o1, TokenContent o2)
	{
		return o1.getBeginIndex() - o2.getBeginIndex();
	}
}
