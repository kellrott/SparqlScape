package org.sparql;

public class SparqlData implements Comparable<SparqlData> {

	String value;
	boolean isURL;
	public String type;
	
	public SparqlData(String value, boolean isURL) {
		this.value = value;
		this.isURL = isURL;
	}

	public SparqlData(String value, String type ) {
		this.value = value;
		this.isURL = true;
		this.type = type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return this.type;
	}
	@Override
	public String toString() {
		return value;
	}

	public String getURI() {
		return value;
	}
	
	public boolean isURI() {
		return isURL;
	}

	@Override
	public int compareTo(SparqlData other) {
		return value.compareTo(other.value);
	}
}
