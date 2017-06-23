package org.apache.lucene.analysis;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;  
import org.apache.lucene.analysis.PostingsField;
import org.apache.lucene.util.BytesRef;

public class PostingsArrayInstance {
	HashMap<String,PostingsField> hm;
	ArrayList<String> fieldlist = new ArrayList<String>();
	ArrayList<String> typelist = new ArrayList<String>();
	
	public PostingsArrayInstance(){
		hm = new HashMap<String,PostingsField>(); 
		typelist.add("str"); 
		typelist.add("str");
	}

	public ArrayList<Object> toOriginal(String allpostings){
		
		ArrayList<Object> outputlist = new ArrayList<Object>();
		String[] ind_postings = allpostings.split("-");
		
		int count = 0; 
		for(String type:typelist){
			System.out.println(type);
			if(type == "str"){
				outputlist.add((String)ind_postings[count]);
			}
			
			else if(type == "int"){
				System.out.println(ind_postings[count]);
				outputlist.add(Integer.parseInt(ind_postings[count]));
				
			}
		count++;
			
		}
		
		return outputlist;
		
	}
	
	public String toString(){
		String output = ""; 
		for(String field: fieldlist){	
			output += hm.get(field).getVal().toString();
			output += "-"; 
			
		    //System.out.println(output);
		}
		
		return output;
		
	}
	
	public BytesRef toBytesRef(){
		String outString = toString();
		return new BytesRef(outString.getBytes(), 0, outString.length());
	}
	
/*	
	public String fromBytesRef(){
		return null;
		
	}
	
	public BytesRef toBytesRef(){
		Iterator it = hm.entrySet().iterator();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		while(it.hasNext()){
			Map.Entry curField = (Map.Entry)it.next();
			
			byte fieldsize = ((byte)((PostingsField)curField.getValue()).getSize());
			byte fieldval =  ((byte)((PostingsField)curField.getValue()).getVal());	
			baos.write(fieldsize);
			baos.write(fieldval); 
			
		}
		
		return null;
		
		
	}
	
*/
	
public static byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(out);
    os.writeObject(obj);
    return out.toByteArray();
}
public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    ObjectInputStream is = new ObjectInputStream(in);
    return is.readObject();
}
	
	public PostingsField getField(String FieldName){
		return hm.get(FieldName); 
		
	}
	
	public void addField(String FieldName, Object fval,String t){
		hm.put(FieldName, new PostingsField(fval,t));
	    fieldlist.add(FieldName); 
		
	}
}
