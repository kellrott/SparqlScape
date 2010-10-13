package org.sparql;

import java.util.ArrayList;
import java.util.List;

public class Results extends ArrayList<Row> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 561219650194086876L;
	List<String> vars;
	
	public Results(List<String> vars) {
		this.vars = vars;
	}
	
	public List<String> getResultVars() {
		return vars;
	}
	
}
