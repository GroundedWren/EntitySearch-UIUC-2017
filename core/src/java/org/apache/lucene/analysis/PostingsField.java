package org.apache.lucene.analysis;
import java.lang.instrument.Instrumentation;

public class PostingsField {
	
	public Object fieldval; 
	public String type; 
	public long size; 
	
	private static Instrumentation instrumentation; 
	
	PostingsField(Object fval , String t){
		fieldval = fval; 
		type = t; 
	//	size = instrumentation.getObjectSize(fieldval); 
	}
	
	public Object getVal(){
		return fieldval; 
	}
	
	public String getType(){
		return type; 
	}
	
	public long getSize(){
		return size; 
	}
	
	
}
