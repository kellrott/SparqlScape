package org.topsan;

import org.sparql.LinkTriple;


public class SearchTest {

	static String testSet [] = {
		"BT_4723",
		"BT_4706",
		"BT_4660",
		"BT_4582",
		"BT_4581",
		"BT_4412",
		"BT_4367",
		"BT_4364",
		"BT_4247",
		"BT_4238",
		"BT_4193",
		"BT_4088",
		"BT_4057",
		"BT_4056",
		"BT_4055",
		"BT_3983",
		"BT_3952",
		"BT_3806",
		"BT_3673",
		"BT_3664",
		"BT_3661",
		"BT_3656",
		"BT_3628",
		"BT_3341",
		"BT_3325",
		"BT_3297",
		"BT_3282",
		"BT_3279",
		"BT_3278",
		"BT_3276",
		"BT_3275",
		"BT_3271",
		"BT_3254",
		"BT_3239",
		"BT_3154",
		"BT_3019",
		"BT_3017",
		"BT_3016",
		"BT_3015",
		"BT_3014",
		"BT_3013",
		"BT_3012",
		"BT_2952",
		"BT_2824",
		"BT_2820",
		"BT_2780",
		"BT_2667",
		"BT_2560",
		"BT_2550",
		"BT_2390",
		"BT_2264",
		"BT_2059",
		"BT_1854",
		"BT_1573",
		"BT_1572",
		"BT_1420",
		"BT_1349",
		"BT_1261",
		"BT_1248",
		"BT_1186",
		"BT_1185",
		"BT_1119",
		"BT_1057",
		"BT_0964",
		"BT_0954",
		"BT_0921",
		"BT_0910",
		"BT_0909",
		"BT_0908",
		"BT_0907",
		"BT_0906",
		"BT_0905",
		"BT_0904",
		"BT_0903",
		"BT_0902",
		"BT_0773",
		"BT_0695",
		"BT_0683",
		"BT_0682",
		"BT_0681",
		"BT_0452",
		"BT_0448",
		"BT_0132",
		"BT_0131",
		"BT_0012",
		"BT_0004",
		"BT_0003",
		"BT_0002",
		"BT_0001"
	};
	
	/*
PREFIX core:<http://purl.uniprot.org/core/>
SELECT ?protein WHERE {
?protein core:encodedBy ?gene .
?gene core:locusName "{id}" 
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
	
	public static void main(String args[]) {		
		SparqlInterface iSparql = new SparqlInterface( "http://proteins:8890/sparql" );		
		for ( LinkTriple link :	iSparql.NodeSearch(null, testSet) ) {
			System.out.println( link );
		}
		//System.exit(0);		
	}
	
}
