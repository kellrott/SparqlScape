package org.sparqlscape;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sparql.LinkTriple;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;

public class ConnectTest {

	
/*
PREFIX core:<http://purl.uniprot.org/core/>
SELECT "{canonicalName}" as ?src, ?protein as ?dst WHERE {
?protein core:encodedBy ?gene .
?gene core:locusName "{canonicalName}" 
}

PREFIX core:<http://purl.uniprot.org/core/>
PREFIX taxon:<http://purl.uniprot.org/taxonomy/>
SELECT ?src, core:locusName as ?edge, ?dst WHERE {
?src a core:Protein .
?src core:encodedBy ?gene .
?gene core:locusName ?dst .
?src core:organism taxon:818
}  

PREFIX core:<http://purl.uniprot.org/core/>
SELECT ?g1 as ?src, ?pred as ?edge, ?s2 as ?dst WHERE {
graph ?g1 {
?s1 a core:Protein .
}
graph ?g2 {
?s2 a ?type .
}
?s1 ?pred ?s2 
}
*/
	
	public static void main(String args[]) throws IOException {	
		
		CSVParser p = new CSVParser( new InputStreamReader( new FileInputStream(new File(args[0])) ), new CSVStrategy('\t', '"',	'#'));
		
		String[] head = p.getLine();

		List< Map<String,Object> > editList = new LinkedList<Map<String,Object>>();

		String []curRead = null;
		do {
			curRead = p.getLine();
			if ( curRead != null ) {
				Map<String, Object> features = new HashMap<String,Object>();
				for ( int i = 0; i < curRead.length; i++ ) {
					features.put(head[i], curRead[i]);
				}
				editList.add(features);
			}			
		} while ( curRead != null );
		
		Map<String,String> nsMap = new HashMap<String,String>();
		nsMap.put("BLAST_UniProt_AC", "http://purl.uniprot.org/uniprot/");
		
		SparqlInterface iSparql = new SparqlInterface( "http://proteins:8890/sparql" );
		iSparql.SetNamespaces(nsMap);
		
		for ( LinkTriple link :	iSparql.ConnectionFinder( editList, "BLAST_UniProt_AC", "ID" ) ) {
			System.out.println( link );
		}
		//System.exit(0);		
	}
	
}
