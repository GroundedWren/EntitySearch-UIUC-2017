//Java
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

//Lucene
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.ner.XMLNERAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

//REST DBPedia
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

//XML Parsing
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class IndexingManagerDBPedia 
{
	
	private static String runClassifier(String fieldContent)
	{
		//CITATION: https://stackoverflow.com/questions/3913502/restful-call-in-java
		CloseableHttpClient httpclient = HttpClients.createDefault();
    	try
    	{
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("model.dbpedia-spotlight.org")
					.setPath("/en/annotate")
					.setParameter("text", fieldContent)
					.setParameter("confidence", "0.35")
					.setParameter("support", "20")
					.build();
			HttpGet httpget = new HttpGet(uri);
			httpget.addHeader("Accept", "text/xml");
			HttpResponse response = httpclient.execute(httpget);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String completeResponse = "";
			String line = "";
			while ((line = rd.readLine()) != null) 
		    {
		    	completeResponse += line;
		    }
			return formatXML(completeResponse);
		}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		return null;
		}
	}
	
	private static String formatXML(String dbpediaXML)
	{
		try
		{	
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringBuilder xmlStringBuilder = new StringBuilder();
			System.out.println(dbpediaXML);
			xmlStringBuilder.append(dbpediaXML);
			ByteArrayInputStream xmlbytes =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			org.w3c.dom.Document xmldoc = builder.parse(xmlbytes);
			NodeList xmlNodes = xmldoc.getElementsByTagName("Resource");
			String formattedXML = "";
			//TODO: FORMAT XML
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
		return dbpediaXML;
	}
	
	public static void main(String[] args)
	{
		try
		{	
			//	Code to create the index
			//Directory index = new RAMDirectory();
			Directory indexDirectory = new SimpleFSDirectory(Paths.get("/Users/alexaulabaugh/Desktop/Research Summer 17/NER/NERLucene/src/indexdir_dbpedia"));
			StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
			XMLNERAnalyzer nerAnalyzer = new XMLNERAnalyzer();
			IndexWriterConfig standardConfig = new IndexWriterConfig(nerAnalyzer);
			standardConfig.setCodec(new SimpleTextCodec());
			standardConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			IndexWriter standardWriter = new IndexWriter(indexDirectory, standardConfig);
			
			addDocsFromDirectory("/Users/alexaulabaugh/Desktop/Research Summer 17/NER/NERLucene/src/docs", new TextFileFilter(), standardWriter);
			standardWriter.close();
			
			//	Text to search
			String querystr = "entity_location";
			
			//	The "content" arg specifies the default field to use when no field is explicitly specified in the query
			Query q = new QueryParser("content", standardAnalyzer).parse(querystr);
			
			// Searching code
			int hitsPerPage = 10;
		    IndexReader reader = DirectoryReader.open(indexDirectory);
		    IndexSearcher searcher = new IndexSearcher(reader);
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		    searcher.search(q, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    
		    //	Code to display the results of search
		    System.out.println("Found " + hits.length + " hits.");
		    for(int i=0;i<hits.length;++i) 
		    {
		      int docId = hits[i].doc;
		      Document d = searcher.doc(docId);
		      System.out.println((i + 1) + ". " + d.get("name"));
		    }
		    
		    // reader can only be closed when there is no need to access the documents any more
		    reader.close();
		}
		catch(Exception e)
		{
			System.out.println("Lucene Error");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	private static void addDocsFromDirectory(String path, FileFilter filter, IndexWriter indexW) throws IOException
	{
		// https://www.tutorialspoint.com/lucene/lucene_indexing_process.htm
		File[] files = new File(path).listFiles();
		for (File file : files)
		{
	         if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file))
	         {
	            Document docToAdd = getDocFromFile(file);
	            if(docToAdd != null)
	            {
	            	indexW.addDocument(docToAdd);
	            }
	         }
	      }
	}
	private static Document getDocFromFile(File f)
	{
		Document doc = new Document();
		try
		{
			FieldType contentType = new FieldType();
			contentType.setStored(false);
			contentType.setTokenized(true);
			contentType.setOmitNorms(false);
			contentType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
			contentType.setStoreTermVectors(true);
			contentType.setStoreTermVectorPositions(true);
			contentType.setStoreTermVectorPayloads(true);
			String contentString = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
			String xmlString = runClassifier(contentString);
			Field contentField = new Field("content", xmlString, contentType);
			
			Field nameField = new StringField("name", f.getName(), Field.Store.YES);
			
			BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);			
			Field sizeField = new LongPoint("size", attr.size());
			
			doc.add(contentField);
			doc.add(nameField);
			doc.add(sizeField);
			
			return doc;
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
