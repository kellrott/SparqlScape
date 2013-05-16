package org.sparqlscape;


import java.util.HashMap;
import java.util.Map;
import java.util.prefs.*;

public class PreferenceInterface {

	private Preferences prefs;


	public PreferenceInterface() {
		prefs = Preferences.userNodeForPackage(this.getClass());
	}
	
	public void getEndpoint(String endpoint) {
		prefs.put("ENDPOINT_URL", endpoint);
	}
	
	public String getEndpoint() {
		return prefs.get("ENDPOINT_URL", "http://localhost/sparql");
	}

	public void setEndpoint(String endpoint) {
		prefs.put("ENDPOINT_URL", endpoint);
	}

	
	public void addNameSpace(String namespace, String url) {
		Preferences node = prefs.node("namespaces");
		node.put(namespace, url);
	}

	public void removeNameSpace(String namespace) {
		Preferences node = prefs.node("namespaces");
		node.remove(namespace);
	}
	
	public Map<String,String> getNameSpaces() {
		Map<String,String> out = new HashMap<String,String>();
		try {
			Preferences node = prefs.node("namespaces");
			for ( String namepace : node.keys() ) {
				out.put(namepace, node.get(namepace, ""));
			}			
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
	
	public static void main(String [] args) {		
		PreferenceInterface prefsInterface = new PreferenceInterface();
		//prefsInterface.addNameSpace("core", "http://purl.uniprot.org/core/");		
		//System.out.println( prefsInterface.getEndpoint() );
		System.out.println( prefsInterface.getNameSpaces() );		
	}
	
	
}
