package org.topsan;

import java.awt.Button;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.sparql.LinkTriple;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
//import cytoscape.groups.CyGroup;
import cytoscape.data.CyAttributes;
import cytoscape.groups.CyGroupManager;
import cytoscape.layout.algorithms.GridNodeLayout;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;

public class TOPScape  extends CytoscapePlugin {
	ConfigureMenuAction configureMenu;
	TOPScapeContainer tpContainer;
	public TOPScape() throws Exception{
		System.out.print("\tTOPScape starting up... ");
		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
		configureMenu = new ConfigureMenuAction(this);
		Cytoscape.getDesktop().getCyMenus().addCytoscapeAction((CytoscapeAction) configureMenu);
		tpContainer = new TOPScapeContainer(this);		
		cytoPanel.add("SPARQLScape PlugIn", null, tpContainer, "SPARQLScape PlugIn");
		cytoPanel.setState(CytoPanelState.DOCK);
	}

	@SuppressWarnings("serial")
	public class TOPScapeContainer extends JPanel implements ActionListener {
		TextField urlText;
		TOPScape plugin;
		SparqlInterface iSparql;

		public TOPScapeContainer(TOPScape myPlugin) {
			this.setLayout( new GridBagLayout() );
			GridBagConstraints c = new GridBagConstraints();

			c.anchor = GridBagConstraints.NORTH;
			c.gridx = 0;
			c.gridy = 0;

			this.plugin = myPlugin;
			this.add( plugin.configureMenu.curServer, c );

			c.gridx = 0;
			c.gridy = 1;

			urlText = new TextField(30);
			this.add(urlText, c);
			Button fetchButton = new Button("Fetch");
			fetchButton.addActionListener(this);
			c.gridx = 0;
			c.gridy = 2;
			this.add( fetchButton, c );

			Button fetchSelectedButton = new Button("Fetch Selected");
			fetchSelectedButton.addActionListener( this );
			c.gridx = 0;
			c.gridy = 4;
			this.add( fetchSelectedButton, c  );

			
			c.gridx = 0;
			c.gridy = 3;
			Button searchButton = new Button("Find Nodes");
			searchButton.addActionListener(this);
			this.add( searchButton, c );			
			this.setVisible(true);

			iSparql = new SparqlInterface( plugin.configureMenu.serverURL );
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
				addLinks( iSparql.NodeFilter(null, nodeList.toArray(new LinkTriple[0]) ) );
			}
			
			if ( action.equals("Find Nodes") ) {
				CyNetwork network = Cytoscape.getCurrentNetwork();
				Set nodeset = network.getSelectedNodes();
				java.util.List<String> nodeList = new LinkedList<String>();
				for (Object o : nodeset ) {
					CyNode node = (CyNode) o;
					nodeList.add( node.getIdentifier() );
				}				
				LinkTriple [] links = iSparql.NodeSearch(null, nodeList.toArray(new String[0] ));
				addLinks(links);
			}
			
		}
		
		private void addLinks( LinkTriple []links ) {
			CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
			CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
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

		public String serverURL = "http://proteins:8890/sparql"; //"http://localhost/sparql";
			
		public Label curServer;
		public TOPScape plugin;

		public ConfigureMenuAction(TOPScape myPlugin) {
			super("SPARQLScape Configure...");
			setPreferredMenu("Plugins");
			curServer = new Label(serverURL);
			plugin = myPlugin;
		}

		public void actionPerformed(ActionEvent e) {			
			/*
			TextField textField = new TextField( serverURL );
			Object [] array = { "Server URL", textField };
			//Object[] options = { "Enter", "Cancel" };
			JOptionPane option = new JOptionPane(array, 
					JOptionPane.QUESTION_MESSAGE, 
					JOptionPane.YES_NO_OPTION );
					//null, 
					//options,
					//options[0] );
			 */
			String out = JOptionPane.showInputDialog(Cytoscape.getDesktop(), "New SPARQL URL" );
			if ( out != null ) {
				serverURL = out;
				curServer.setText(out);
				plugin.tpContainer.iSparql.SetEndpoint(serverURL);
			}
		}
	}


}
