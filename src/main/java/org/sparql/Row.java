package org.sparql;

import java.util.HashMap;

public class Row extends HashMap<String,SparqlData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9090621706726356157L;

	public boolean isResource(String colName) {
		return get(colName).isURI();
	}
	
	public SparqlData getResource(String colName) {
		return get(colName);
	}

	public String getLiteral(String colName) {
		return get(colName).toString();
	}
	
}
