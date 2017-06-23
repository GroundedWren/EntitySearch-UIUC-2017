//Java
/*
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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

//NER
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.util.Triple;

public class IndexingManagerStanfordNER 
{
	
	private static AbstractSequenceClassifier<CoreLabel> classifier;
	
	private static void createClassifier()
	{
		try
		{
			String serializedClassifier = "/Users/alexaulabaugh/Desktop/Research Summer 17/NER/stanford-ner-2016-10-31/classifiers/english.all.3class.distsim.crf.ser.gz";
		    classifier = CRFClassifier.getClassifier(serializedClassifier);
		}
		catch(Exception e)
		{
			System.out.println("Classifier Creation Error");
			System.out.println(e.getMessage());
		}
	}
	
	private static String runClassifier(String fieldContent)
	{
		String classifiedXML = classifier.classifyToString(fieldContent, "xml", true);
		System.out.println(classifiedXML);
		return classifiedXML;
		//Alternate format below
		/*
		List<List<CoreLabel>> out = classifier.classify(fieldContent);
		for (List<CoreLabel> sentence : out)
		{
			for (CoreLabel word : sentence)
			{
				System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');

			}
			System.out.println();
		}
		return "";
		
	}
	
	public static void main(String[] args)
	{
		//Initialize the classifier
		createClassifier();
		try
		{	
			//	Code to create the index
			//Directory index = new RAMDirectory();
			Directory indexDirectory = new SimpleFSDirectory(Paths.get("/Users/alexaulabaugh/Desktop/Research Summer 17/NER/NERLucene/src/indexdir"));
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
*/
