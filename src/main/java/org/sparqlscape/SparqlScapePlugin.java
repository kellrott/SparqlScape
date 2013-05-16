package org.sparqlscape;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import org.sparql.LinkTriple;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
//import cytoscape.groups.CyGroup;
import cytoscape.data.CyAttributes;
//import cytoscape.groups.CyGroupManager;
import cytoscape.layout.algorithms.GridNodeLayout;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;

public class SparqlScapePlugin  extends CytoscapePlugin {
	ConfigureMenuAction configureMenu;
	SparqlScapeContainer tpContainer;
	public PreferenceInterface prefs;

	public SparqlScapePlugin() throws Exception{
		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
		configureMenu = new ConfigureMenuAction(this);
		Cytoscape.getDesktop().getCyMenus().addCytoscapeAction((CytoscapeAction) configureMenu);
		tpContainer = new SparqlScapeContainer(this);		
		cytoPanel.add("SPARQLScape", null, tpContainer, "SPARQLScape");
		cytoPanel.setState(CytoPanelState.DOCK);
	}

	@SuppressWarnings("serial")
	public class SparqlScapeContainer extends JPanel implements ActionListener {
		TextField urlText;
		SparqlScapePlugin plugin;
		SparqlInterface iSparql;
		Checkbox filterCheckbox;
		
		public SparqlScapeContainer(SparqlScapePlugin myPlugin) {
			this.plugin = myPlugin;
			this.setLayout( new GridBagLayout() );
			GridBagConstraints c = new GridBagConstraints();

			c.anchor = GridBagConstraints.NORTH;

			JPanel endpointPanel = new JPanel();
			endpointPanel.setLayout(new GridBagLayout() );
			endpointPanel.add( new Label("SPARQL Endpoint:"), c );
			c.gridx = 0;
			c.gridy = 1;
			endpointPanel.add( plugin.configureMenu.curServer, c );
			endpointPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			c.gridy = 0;
			this.add( endpointPanel, c);
			
			JPanel urlPanel = new JPanel();
			urlPanel.add( new JLabel("Node Fetch:"), c );
			c.gridx = 0;
			c.gridy = 1;
			urlPanel.setLayout( new GridBagLayout() );
			urlText = new TextField(30);
			urlPanel.add(urlText, c);
			Button fetchButton = new Button("Fetch");
			fetchButton.addActionListener(this);
			c.gridx = 0;
			c.gridy = 2;
			urlPanel.add( fetchButton, c );
			urlPanel.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.LOWERED) );
			c.gridy = 1;
			this.add(urlPanel, c);
			
			c.gridx = 0;
			c.gridy = 3;
			Button searchButton = new Button("Find Nodes");
			searchButton.addActionListener(this);
			this.add( searchButton, c );			
			
			JPanel fetchPanel = new JPanel();
			Button fetchSelectedButton = new Button("Fetch Selected");
			fetchSelectedButton.addActionListener( this );
			fetchPanel.add(fetchSelectedButton);
			filterCheckbox = new Checkbox("Filter", true);
			fetchPanel.add(filterCheckbox);
			
			c.gridx = 0;
			c.gridy = 4;
			this.add( fetchPanel, c  );
			
			c.gridy = 5;
			Button editNamespaces = new Button("Edit Namespaces");
			editNamespaces.addActionListener(this);
			this.add( editNamespaces, c );
						
			this.setVisible(true);			
			
			iSparql = new SparqlInterface( prefs.getEndpoint() );
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent ae) {
			String action = ae.getActionCommand();	
						
			LinkTriple []addNodes = null;
			if ( action.equals("Fetch") ) {
				addNodes = iSparql.ExploreURI( urlText.getText() );
				addLinks(addNodes);			
				//Cytoscape.getCurrentNetwork().addNode( Cytoscape.getCyNode( urlText.getText(), true ) );
			}
			if ( action.equals("Fetch Selected") ) {
				CyNetwork network = Cytoscape.getCurrentNetwork();
				Set nodeset = network.getSelectedNodes();
				java.util.List<LinkTriple> nodeList = new LinkedList<LinkTriple>();
				for (Object o : nodeset ) {
					CyNode node = (CyNode) o;
					for ( LinkTriple newNode : iSparql.ExploreURI(node.getIdentifier()) ) {
						nodeList.add(newNode);
					}
				}
				if ( filterCheckbox.getState() )
					addLinks( iSparql.NodeFilter(null, nodeList.toArray(new LinkTriple[0]) ) );
				else
					addLinks( nodeList.toArray(new LinkTriple[0]) );
					
			}			
			if ( action.equals("Find Nodes") ) {
				CyAttributes nodeAttr = Cytoscape.getNodeAttributes();

				CyNetwork network = Cytoscape.getCurrentNetwork();
				Set nodeset = network.getSelectedNodes();
				java.util.List<Map<String,Object>> nodeList = new LinkedList<Map<String,Object>>();
				for (Object o : nodeset ) {
					CyNode node = (CyNode) o;
					Map<String, Object> map = new HashMap<String,Object>();
					String id = node.getIdentifier();
					map.put( "ID", id );
					for ( String attr : nodeAttr.getAttributeNames() ) {
						if ( nodeAttr.hasAttribute(id, attr )) {
							System.out.println(id + " " + attr + " " + nodeAttr.getType(attr) );
							if ( nodeAttr.getType(attr) == CyAttributes.TYPE_STRING ) {
								Object iAttr = nodeAttr.getAttribute( id, attr );
								map.put( attr, iAttr );
							}
						}
					}
					nodeList.add( map);
				}				
				LinkTriple [] links = iSparql.NodeSearch(null, nodeList, "ID" );
				addLinks(links);
			}
			if ( action.equals("Edit Namespaces") ) {
				Map<String,String> nameMap = prefs.getNameSpaces();
				Map<String,String> newMap = NameSpaceEditor.EditDialog(nameMap);				
				for ( String key : nameMap.keySet() ) {
					prefs.removeNameSpace(key);
				}
				for ( String key : newMap.keySet() ) {
					prefs.addNameSpace(key, newMap.get(key) );
				}				
			}
			
		}
		
		private void addLinks( LinkTriple []links ) {
			CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
			//CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
			Map<CyNode, Boolean> updateList = new HashMap<CyNode, Boolean>();
			CyNetwork net = Cytoscape.getCurrentNetwork();
			for ( LinkTriple link : links ) {
				CyNode src = Cytoscape.getCyNode( link.subj.toString() );
				CyNode dst = Cytoscape.getCyNode( link.obj.toString() );
				if ( src == null ) {
					src = net.addNode( Cytoscape.getCyNode( link.subj.toString() , true ) );
					if ( link.subj.getType() != null  )
						nodeAttr.setAttribute( src.getIdentifier(), "NodeType", link.subj.getType() );
				}
				if ( dst == null ) {
					dst = net.addNode( Cytoscape.getCyNode( link.obj.toString() , true ) );	
					if ( link.obj.getType() != null )
						nodeAttr.setAttribute( dst.getIdentifier(), "NodeType", link.obj.getType() );
				}
				CyEdge edge = Cytoscape.getCyEdge( link.subj.toString(), "link", link.obj.toString(), link.pred.toString() );
				//if ( link.pred != null )
				//	edgeAttr.setAttribute(edge.getIdentifier(), "EdgeType", link.pred.toString() );
				net.addEdge(edge);
				updateList.put( src, true );
				updateList.put( dst, true );
			}
			Cytoscape.getCurrentNetworkView().applyLayout( new GridNodeLayout(), updateList.keySet().toArray(new CyNode[0]), null );
			Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
		}		
	}


	@SuppressWarnings("serial")
	public class ConfigureMenuAction extends CytoscapeAction {
		
		public Label curServer;
		public SparqlScapePlugin plugin;

		public ConfigureMenuAction(SparqlScapePlugin myPlugin) {
			super("SPARQLScape Configure...");
			prefs = new PreferenceInterface();
			setPreferredMenu("Plugins");
			curServer = new Label(prefs.getEndpoint());
			plugin = myPlugin;
		}

		public void actionPerformed(ActionEvent e) {		
			String serverURL = JOptionPane.showInputDialog(Cytoscape.getDesktop(), "New SPARQL URL" );
			if ( serverURL != null ) {
				curServer.setText(serverURL);
				prefs.setEndpoint(serverURL);
				plugin.tpContainer.iSparql.SetEndpoint(serverURL);
			}
		}
	}
}
