package org.apache.lucene.analysis.ner;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class XMLNERAnalyzer extends Analyzer {

	
	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
	
	public void setMaxTokenLength(int length)
	{
		maxTokenLength = length;
	}
	
	public int getMaxTokenLength()
	{
		return maxTokenLength;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName)
	{
		final XMLNERTokenizer src = new XMLNERTokenizer();
		//src.setMaxTokenLength(maxTokenLength);
		TokenStream tok = new StandardFilter(src);
		tok = new LowerCaseFilter(tok);
		return new TokenStreamComponents(src, tok)
		{
			@Override
			protected void setReader(final Reader reader)
			{
				// So that if maxTokenLength was changed, the change takes
				// effect next time tokenStream is called:
				//src.setMaxTokenLength(XMLNERAnalyzer.this.maxTokenLength);
				super.setReader(reader);
			}
		};
	}
}
