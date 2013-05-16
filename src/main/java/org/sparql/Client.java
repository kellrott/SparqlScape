package org.sparql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Client {

	private URL endpoint;

	public Client(URL endpoint) {
		this.endpoint = endpoint;
	}

	public Results select(String query) {
		Results outList = null;

		try {
			String queryURL = endpoint.toString() + "?output=json&query=" + URLEncoder.encode( query, "UTF8" );
			System.out.println(queryURL);
			URLConnection conn = (new URL(queryURL)).openConnection();

			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();

			String line = null;
			do { 
				line = br.readLine();
				if ( line != null )
					sb.append(line);			
			} while (line != null);					
			JSONObject result = new JSONObject( sb.toString() );			

			List<String> varList = new ArrayList<String>();
			JSONArray vars = result.getJSONObject("head").getJSONArray("vars");
			for ( int i = 0; i < vars.length(); i++) {
				varList.add( vars.get(i).toString()  );
			}
			outList = new Results(varList);
			//System.err.println(varList);
			JSONArray bind = result.getJSONObject("results").getJSONArray("bindings");
			for ( int i = 0; i < bind.length(); i++) {
				Row row = new Row();
				JSONObject sparqlRow = bind.getJSONObject(i);
				for ( String key : varList ) {
					if ( sparqlRow.has(key)) {
						JSONObject r = sparqlRow.getJSONObject(key);
						if (r.has("type")) {
							String type = r.getString("type"); 
							if ( type.compareTo("uri") == 0 ) {
								row.put(key, new SparqlData( r.getString("value"), true ) );
							} else if (type.compareTo("literal") == 0 ) {
								row.put(key, new SparqlData( r.getString("value"), false ) );						
							}
						}
					}
				}
				outList.add(row);
			}
			//System.err.print(result);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
		return outList;
	}


	public static void main(String []args) throws MalformedURLException {

		String testQuery = "PREFIX core:<http://purl.uniprot.org/core/> PREFIX taxon:<http://purl.uniprot.org/taxonomy/> " +
		"SELECT * WHERE { ?src a core:Protein . ?src core:encodedBy ?gene . ?gene core:locusName ?dst . " + 
		"?src core:organism taxon:818 }"; 

		Client s = new Client(new URL("http://proteins:8890/sparql"));
		//Client s = new Client( "proteins", 8890, "/sparql" );
		System.out.println( s.select( testQuery ) );
	}
}
