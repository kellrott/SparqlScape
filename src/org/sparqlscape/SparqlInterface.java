package org.sparqlscape;

import java.awt.Button;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.sparql.Client;
import org.sparql.LinkTriple;
import org.sparql.Results;
import org.sparql.Row;
import org.sparql.SparqlData;

public class SparqlInterface {
	String endpointURL;	
	Map<String,Boolean> predicates;
	Map<String,Boolean> nodeTypes;
	Map<String,String> typeCache;
	Map<String,Boolean> predExclude;
	Map<String,String> nsMap;

	public SparqlInterface(String url) {
		endpointURL = url;
		predicates = new HashMap<String,Boolean>();
		nodeTypes = new HashMap<String,Boolean>();
		typeCache = new HashMap<String, String>();
		predExclude = new HashMap<String, Boolean>();
		predExclude.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", true);
	}

	public void SetEndpoint(String url) {
		endpointURL = url;
	}

	public void SetNamespaces(Map<String,String> nsMap ) {
		this.nsMap = nsMap;
	}

	public String getType( String uri ) {
		if ( typeCache.containsKey(uri) )
			return typeCache.get(uri);

		String typeURI = null;		
		try {
			Client sClient = new Client(new URL(endpointURL));
			String typeQuery = "SELECT * WHERE { <" + uri + "> a ?type} ";
			Results typeResult = sClient.select( typeQuery );
			if ( typeResult.size() > 0 ) {
				typeURI = typeResult.get(0).getResource("type").toString();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		typeCache.put(uri,typeURI);
		return typeURI;
	}

	public LinkTriple [] ExploreURI( String uri ) {		
		List<LinkTriple> outList = new LinkedList<LinkTriple>();
		try {
			String typeURI = getType(uri);
			String sparqlQueryString = 
				"SELECT * WHERE {" + 
				"{ <" + uri + "> ?pred ?dst }" +
				"UNION" +
				"{ ?src ?pred <" + uri + "> }" + 
				"FILTER (isURI( ?dst ) || isURI(?src)) " +
				"}";
			Client sClient = new Client(new URL(endpointURL));
			Results results = sClient.select( sparqlQueryString );
			for ( Row row : results ) {
				String pred = row.get("pred").getURI() ;
				if ( !predExclude.containsKey(pred) ) {
					if ( !predicates.containsKey(pred) ) {
						predicates.put(pred, true);
					}
					if ( row.get("dst") != null ) {
						String dst = row.get("dst").getURI() ;
						String dstType = getType( dst );
						outList.add( new LinkTriple( new SparqlData(uri, typeURI), new SparqlData(pred, true), new SparqlData(dst, dstType) ) );
					}
					if ( row.get("src") != null ) {
						String src = row.get("src").getURI() ;
						String srcType = getType(src);
						outList.add( new LinkTriple( new SparqlData(src, srcType), new SparqlData(pred, true), new SparqlData(uri, typeURI) ) );
					}
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outList.toArray( new LinkTriple[0] );
	}


	public LinkTriple [] NodeFilter( Component parent, LinkTriple [] inList ) {
		GridBagConstraints c = new GridBagConstraints();

		final JDialog frame = new JDialog();
		JPanel panel = new JPanel();
		panel.setLayout( new GridBagLayout() );

		panel.add(new Label("Edge Types"), c);
		c.gridx = 1;
		panel.add(new Label("Node Types"), c);

		c.gridy = 1;
		c.gridx = 0;

		//Edge List
		DefaultListModel edgeTypeListModel = new DefaultListModel();
		for ( LinkTriple t : inList ) {
			if ( !edgeTypeListModel.contains( t.pred.toString() ) )
				edgeTypeListModel.addElement( t.pred.toString() );
		}
		JList edgeTypeList = new JList( edgeTypeListModel );
		edgeTypeList.setVisibleRowCount(-1);
		//edgeTypeList.setVisibleRowCount(20);
		edgeTypeList.setSelectionInterval(0,edgeTypeListModel.getSize()-1);
		JScrollPane edgeTypeScrollPane = new JScrollPane(edgeTypeList);
		edgeTypeScrollPane.setPreferredSize(new Dimension(300, 200) );
		c.gridx=0;
		panel.add( edgeTypeScrollPane, c );

		//Node type List
		DefaultListModel nodeTypeListModel = new DefaultListModel();
		for ( LinkTriple t : inList ) {
			if ( !nodeTypeListModel.contains( t.obj.getType() ) )
				nodeTypeListModel.addElement( t.obj.getType() );
			if ( !nodeTypeListModel.contains( t.subj.getType() ) )
				nodeTypeListModel.addElement( t.subj.getType() );
		}
		JList nodeTypeList = new JList( nodeTypeListModel );
		nodeTypeList.setVisibleRowCount(-1);
		//nodeTypeList.setVisibleRowCount(20);
		nodeTypeList.setSelectionInterval(0,nodeTypeListModel.getSize()-1);
		JScrollPane nodeTypeScrollPane = new JScrollPane(nodeTypeList);
		nodeTypeScrollPane.setPreferredSize(new Dimension(300, 200) );
		c.gridx=1;
		panel.add( nodeTypeScrollPane, c );

		Button okButton = new Button("OK");
		okButton.addActionListener( new ActionListener() {					
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		}); 
		c.gridx=0;
		c.gridy = 3;
		panel.add( okButton, c );

		frame.add( panel );
		frame.setLocationRelativeTo(null);
		frame.setSize(600,300);
		frame.setModal(true);		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		frame.setVisible(true);

		Map<String, Boolean> nodeTypeHash = new HashMap<String, Boolean>();
		for (Object o : nodeTypeList.getSelectedValues() ) {
			String s = (String)o;
			nodeTypeHash.put(s, true);
		}
		List<LinkTriple> outList = new LinkedList<LinkTriple>();

		for ( LinkTriple link : inList ) {
			if ( nodeTypeHash.containsKey( link.pred.getType() ) 
					&& nodeTypeHash.containsKey( link.subj.getType() ) 
			) {
				outList.add(link);
			}
		}

		return outList.toArray( new LinkTriple[0] );
	}

	public Boolean dialogDone;
	public LinkTriple [] NodeSearch( Component parent, final List<Map<String,Object>> nodeAttributes, final String idCol ) {

		JPanel panel = new JPanel();

		panel.setLayout( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 1;
		panel.add( new Label("SPARQL Search"), c );
		c.gridx = 1;
		final TextArea sparqlQuery = new TextArea(6, 50);
		panel.add( sparqlQuery, c );

		final Map<LinkTriple, Integer> nameList = new HashMap<LinkTriple, Integer>();
		final JDialog frame = new JDialog();

		c.gridx = 0;
		c.gridy = 3;
		panel.add( new Label("LinkCount:"), c);
		c.gridx = 1;
		final Label linkCountLabel = new Label("0");
		panel.add( linkCountLabel, c);		

		c.gridx = 0;
		c.gridy = 6;
		panel.add( new JLabel("<html><p>SPARQL</p><p>INPUT node id: {id}</p><p>OUTPUT  ?src  ?edge ?dst</p></html>"), c  );

		Button searchButton = new Button("Search Selected"); 
		searchButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String searchBase = sparqlQuery.getText() ;
				nameList.clear();
				try {
					Client sClient = new Client(new URL(endpointURL));
					for ( Map<String,Object> attrs : nodeAttributes ) {
						for ( String key : attrs.keySet() ) {
							Pattern idRegex = Pattern.compile( "\\{" + key + "\\}" );
							String searchStr = idRegex.matcher(searchBase).replaceAll( attrs.get(key).toString() );		
							Results results = sClient.select(searchStr);
							String colName = results.getResultVars().get(0);
							for ( Row row : results ) {
								System.out.println( row.getResource(colName).getURI() );
								nameList.put( new LinkTriple( new SparqlData(attrs.get(key).toString(), true), new SparqlData("sparqlLink", true), row.getResource(colName) ), 1 );
							}					
						}
					}
					linkCountLabel.setText( Integer.toString( nameList.size() ) );
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} );
		c.gridx = 0;
		c.gridy = 2;
		panel.add( searchButton, c );		

		Button singleSearchButton = new Button("Single Search"); 
		singleSearchButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					Client sClient = new Client(new URL(endpointURL));
					String searchBase = sparqlQuery.getText() ;

					Results results = sClient.select( searchBase );

					List<String> resVars = results.getResultVars();					
					String srcName = resVars.get(0);
					if ( resVars.contains("src") )
						srcName = "src";					
					String predName = null;
					if ( resVars.size() > 1 ) {
						predName = resVars.get(1);
						if ( resVars.contains("edge" ) ) 
							predName = "edge";						
					}
					String dstName = null;
					if ( resVars.size() > 1 ) {
						dstName = resVars.get(1);
						if ( resVars.contains("dst" ) ) 
							dstName = "dst";						
					}
					for (Row row : results ) {
						String srcURI = row.getResource(srcName).getURI();
						String edgeURI = null;
						if ( predName != null )
							edgeURI = row.getResource(predName).getURI();
						String dstURI = null;
						if ( dstName != null) {						
							if ( row.get(dstName).isURI() )
								dstURI = row.getResource(dstName).getURI();
							else
								dstURI = row.getLiteral(dstName).toString();
						}
						String srcType = getType(srcURI);
						String edgeType = getType(edgeURI);
						String dstType = getType(dstURI);							
						nameList.put( new LinkTriple( new SparqlData(srcURI, srcType), new SparqlData(edgeURI, edgeType), new SparqlData(dstURI, dstType) ), 1 );
					}					
					linkCountLabel.setText( Integer.toString( nameList.size() ) );
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		c.gridx = 1;
		c.gridy = 2;
		panel.add( singleSearchButton, c );		

		c.gridx = 0;
		c.gridy = 4;
		Button filterButton = new Button("Filter");
		filterButton.addActionListener( new ActionListener( ) {			
			@Override
			public void actionPerformed(ActionEvent e) {
				LinkTriple []out = NodeFilter(null, nameList.keySet().toArray(new LinkTriple[0]) ); 
				nameList.clear();
				for ( LinkTriple t : out ) {
					nameList.put(t, 1);
				}
				linkCountLabel.setText( Integer.toString( nameList.size() ) );
			}
		});
		panel.add( filterButton, c );		

		Button importButton = new Button("IMPORT");
		importButton.addActionListener( new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		c.gridx=0;
		c.gridy=5;
		panel.add(importButton, c);


		frame.add(panel);
		frame.setLocationRelativeTo(null);
		frame.setSize(600,300);
		frame.setModal(true);		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		frame.setVisible(true);		
		return nameList.keySet().toArray(new LinkTriple[0]);
	}



	public LinkTriple [] ConnectionFinder( final List<Map<String,Object>> nodeAttributes, final String searchCol, final String dstCol ) {
		List<LinkTriple> outList = new LinkedList<LinkTriple>();

		try {
			Client sClient = new Client(new URL(endpointURL));

			for ( Map<String,Object> varMap : nodeAttributes) {
				String url = nsMap.get( searchCol ) + varMap.get(searchCol);

				String typeQuery = "select distinct ?o2 where {" + 
				"<" + url + "> ?p ?o . " + 
				"?o ?p2 ?o2 }";
				System.err.println( typeQuery );
				Results typeResult = sClient.select( typeQuery );
				if (typeResult != null ) {
					List<String> cols = typeResult.getResultVars();
					for ( Row row : typeResult ) {
						System.out.println( row.get( cols.get(0) ) );

					}
				}				
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}





		return outList.toArray(new LinkTriple[0]);		
	}



}
