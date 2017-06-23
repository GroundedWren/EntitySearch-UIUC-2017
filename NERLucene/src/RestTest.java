import java.io.*;
import java.net.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RestTest
{
	//CITATION: https://stackoverflow.com/questions/3913502/restful-call-in-java
    public static void main(String[] args) throws IOException
    {
    	
    	CloseableHttpClient httpclient = HttpClients.createDefault();
    	try
    	{
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("model.dbpedia-spotlight.org")
					.setPath("/en/annotate")
					.setParameter("text", "President Obama called Wednesday on Congress to extend a tax break for students included in last year's economic stimulus package, arguing that the policy provides more generous assistance.")
					.setParameter("confidence", "0.2")
					.setParameter("support", "20")
					.build();
			HttpGet httpget = new HttpGet(uri);
			httpget.addHeader("Accept", "text/xml");
			System.out.println(httpget.getURI());
			HttpResponse response = httpclient.execute(httpget);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";while ((line = rd.readLine()) != null) 
		    {
		    	System.out.println(line);
		    }
		}
    	catch (URISyntaxException e)
    	{
			e.printStackTrace();
		}
    	/*
        URL url = new URL("http://model.dbpedia-spotlight.org/en/annotate?text=President%20Obama%20called%20Wednesday%20on%20Congress%20to%20extend%20a%20tax%20break%20for%20students%20included%20in%20last%20year%27s%20economic%20stimulus%20package,%20arguing%20that%20the%20policy%20provides%20more%20generous%20assistance.&confidence=0.2&support=20 -H");

        //make connection
        URLConnection urlc = url.openConnection();

        //use post mode
        urlc.setDoOutput(true);
        urlc.setAllowUserInteraction(false);

        //get result
        BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        String l = null;
        while ((l=br.readLine())!=null)
        {
            System.out.println(l);
        }
        br.close();
        */
    }
}