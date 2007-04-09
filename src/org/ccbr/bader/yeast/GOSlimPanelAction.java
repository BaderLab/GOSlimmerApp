package org.ccbr.bader.yeast;

import giny.model.Edge;
import giny.model.Node;
import giny.view.EdgeView;
import giny.view.GraphViewChangeEvent;
import giny.view.GraphViewChangeListener;
import giny.view.NodeView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.text.AsyncBoxView.ChildState;
import javax.xml.bind.JAXBException;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;
import org.ccbr.bader.yeast.view.gui.NodeContextMenuActionListener;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.bookmarks.Bookmarks;
import cytoscape.bookmarks.DataSource;
import cytoscape.data.CyAttributes;
import cytoscape.data.annotation.AnnotationDescription;
import cytoscape.data.servers.BioDataServer;
import cytoscape.ding.DingNetworkView;
import cytoscape.util.BookmarksUtil;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualStyle;
import ding.view.NodeContextMenuListener;

public class GOSlimPanelAction implements ActionListener {

	/*Notes whether or not the GOSlimPanel has been opened yet or now */
	boolean alreadyOpened = false;
	GOSlimPanel goSlimPanel =null;

	protected String directlyAnnotatedGenesAttributeName = "GENE_ASSOC.DIRECTLY_ANNOTATED_GENES";
	protected String inferredAnnotatedGenesAttributeName = "GENE_ASSOC.INFERRED_ANNOTATED_GENES";
	
	public GOSlimPanelAction() {
		// TODO Auto-generated constructor stub
	}

	//A boolean is attached to the networkview with this property name to indicate whether or not is has already had a goslimmer attached to it
	private String networkViewGoSlimClientDataPropertyName = "GoSlimmerAttached";
	
	private GOSlimmerController controller;
	
	public void actionPerformed(ActionEvent arg0) {
		//display GOSlimmer Main Panel in left cytopanel
		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

		

//		if (!alreadyOpened) {
//			//initialize the goSlimPanel and add it to the cytopanel
//			goSlimPanel = new GOSlimPanel();
//			cytoPanel.add(goSlimPanel);
//			alreadyOpened = true;
//		}
		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
        if (!vmm.getCalculatorCatalog().getVisualStyleNames().contains("GOSLIMMERVS")) {
            vmm.getCalculatorCatalog().addVisualStyle(new GOSlimmerVisualStyle(vmm.getVisualStyle(),"GOSLIMMERVS"));
        }
		vmm.setVisualStyle("GOSLIMMERVS");
		
		CyNetworkView networkView = Cytoscape.getCurrentNetworkView();
		CyNetwork network = Cytoscape.getCurrentNetwork();
		
	
		//List<CyNetwork> subNetworks = createOntologyNamespaceSubGraphs(network);
		List<CyNetwork> subNetworks = new ArrayList<CyNetwork>();


		GOSlimmerCoverageStatBean statBean=null;
		
		Boolean goSlimmerAttachedToView = (Boolean) networkView.getClientData(networkViewGoSlimClientDataPropertyName);
		
		if (goSlimmerAttachedToView==null || !goSlimmerAttachedToView){
			
			//Get the list of available annotations
			Bookmarks bookmarks = null;
			try {
				bookmarks = Cytoscape.getBookmarks();
			} catch (JAXBException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//we can use this to get a list of the ontology names
			//Set<String> ontologyNames = Cytoscape.getOntologyServer().getOntologyNames();

			CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
			
			int associatedGeneCount = 0;
//			int molFunAssociatedGeneCount = 0;
//			int bioProAssociatedGeneCount = 0;
//			int celComAssociatedGeneCount = 0;
			
			//set attach gene annotation attributes to go terms, for the genes which they are annotated to:
			try {
				//initialize the gene association reader for SCer 
				//GeneAssociationReaderUtil garu = new GeneAssociationReaderUtil("Gene Association file for Saccharomyces cerevisiae",new URL("http://www.geneontology.org/cgi-bin/downloadGOGA.pl/gene_association.sgd.gz"),"GO:ID");
				GeneAssociationReaderUtil garu = new GeneAssociationReaderUtil("Yeast GO slim",new URL("http://www.geneontology.org/cgi-bin/downloadGOGA.pl/gene_association.sgd.gz"),"GO:ID");
				//process the gene association file
				garu.readTable();
				//retrieve the mapping of GOIDs to GeneIDs from the parsed asssociation file
				Map<String,List<String>> goIdToGeneIdMap = garu.getGOIDToGeneIDMap();
				
				Set<String> associatedGeneIds = garu.getGeneIds();
				
				associatedGeneCount = associatedGeneIds.size();
				
				//create the subgraphs
				createOntologyNamespaceSubGraphs(network);
				//initialize the subgraphs with their controllers and coverage statistics beans
				initializeControllersForSubGraphs(garu);
				
				subNetworks.add(molFunSubGraph);
				subNetworks.add(bioProSubGraph);
				subNetworks.add(celComSubGraph);
				
				//initialize the nodecontextmenu's for each of the subgraphs views
				Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(molFunController));
				Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(bioProController));
				Cytoscape.getNetworkView(celComSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(celComController));
				
				//molFunAssociatedGeneCount = garu.getMolecularFunctionGeneIds().size();
				//bioProAssociatedGeneCount = garu.getBiologicalProcessGeneIds().size();
				//celComAssociatedGeneCount = garu.getCellularComponentGeneIds().size();
				
				//initialize the statbean which will keep track of the degree of coverage of the gene set for the GO Slim set
				statBean = new GOSlimmerCoverageStatBean(associatedGeneIds.size());
				
				//with the statbean, which is part of the model layer, initialized, we can now initialize the controller bean
				controller = new GOSlimmerController(network,networkView,statBean);
				
				//iterate through the keyset of the goIdToGeneIdMap, attaching the geneid list attributes to the nodes of the GO DAG as you go along
//				for(String goid:goIdToGeneIdMap.keySet()) {
//					
//					int[] nodes = network.getNodeIndicesArray();
//				}
				//scratch that, instead iterate through the nodes of the GO DAG graph, and attach annotated gene list attributes accordingly
				Iterator<Node> nodeI = network.nodesIterator();
				while (nodeI.hasNext()) {
					Node node = nodeI.next();
					String nodeGoId = node.getIdentifier();
					//get the genes which this go term annotates, according to the gene association file
					List<String> nodeAnnotatedGeneIds = goIdToGeneIdMap.get(nodeGoId);
					
					//attach the gene id list as an attribute, if it exists 
					if (nodeAnnotatedGeneIds!=null && nodeAnnotatedGeneIds.size()>0) {
						nodeAtt.setListAttribute(nodeGoId, directlyAnnotatedGenesAttributeName, nodeAnnotatedGeneIds);
					}
					
				}
				//now annotated each node with the genes it annotated indirectly, through inference, from the genes which it's children inference
				nodeI = network.nodesIterator();
				
				while(nodeI.hasNext()) {
					Node node = nodeI.next();
					//see if the inferred coverred genes list attribute has already been calculated and set
					List<String> inferredCoveredGenesL = nodeAtt.getListAttribute(node.getIdentifier(), inferredAnnotatedGenesAttributeName);
					if (inferredCoveredGenesL ==null || inferredCoveredGenesL.size()==0) {
						//inferred coverred genes list has not already been calculated, so calculate and set it
						Set<String> inferredCoveredGenesS = getGenesCoveredByChildren(node, network);
						nodeAtt.setListAttribute(node.getIdentifier(), inferredAnnotatedGenesAttributeName, setToList(inferredCoveredGenesS));
					}
					
				}
				/* annotated the nodes of the DAG with the genes covered by inference from coverage by descendant nodes
				 * pseudocode:  
				 * 	select a node of the graph at random
				 * 	ascend to the root node
				 *  call the initInferedGeneCoverageAttribute(node) on the root node, which will descend the tree, setting up inferred coverage
				 * flaw:  this will not work, since there are three separate graphs at present
				*/
				
				//now that the graph and its attributes have been initialized, apply the GOSlimmer visual style, which makes use of those attributes
				//networkView.setVisualStyle("org.ccbr.bader.yeast.GOSlimmerVisualStyle");
				
				

				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			for (CyNetwork subNetwork:subNetworks) {	
				CyNetworkView subNetworkView = Cytoscape.createNetworkView(subNetwork);
				
				VisualStyle vs = Cytoscape.getVisualMappingManager().setVisualStyle("GOSLIMMERVS");
				subNetworkView.redrawGraph(false,false);
				//Cytoscape.getVisualMappingManager().setVisualStyle(vs);
				subNetworkView.setVisualStyle("GOSLIMMERVS");
				
			}
			
			//TODO implement this in a better manner
			networkView.putClientData(networkViewGoSlimClientDataPropertyName,Boolean.TRUE);
			
			NodeContextMenuListener ncml = getGOSlimmerNodeContextMenuListener(controller);
			networkView.addNodeContextMenuListener(ncml);
			
//			GOSlimmerCoverageStatBean molFunStatBean = new GOSlimmerCoverageStatBean(molFunAssociatedGeneCount);
//			GOSlimmerCoverageStatBean bioProStatBean = new GOSlimmerCoverageStatBean(bioProAssociatedGeneCount);
//			GOSlimmerCoverageStatBean celComStatBean = new GOSlimmerCoverageStatBean(celComAssociatedGeneCount);
//			
//			for(CyNetwork subNetwork:subNetworks) {
//				CyNetworkView subNetworkView = Cytoscape.getNetworkView(subNetwork.getIdentifier());
//				String subNetworkTitle = subNetwork.getTitle();
//				GOSlimmerCoverageStatBean subNetworkStatBean;
//				if (subNetworkTitle.matches(".*_molecular_function")) {
//					subNetworkStatBean = new GOSlimmerCoverageStatBean(molFunAssociatedGeneCount);
//				}
//				else if (subNetworkTitle.matches(".*_biological_process")) {
//					subNetworkStatBean = new GOSlimmerCoverageStatBean(bioProAssociatedGeneCount);
//				}
//				else if (subNetworkTitle.matches(".*_cellular_component")) {
//					subNetworkStatBean = new GOSlimmerCoverageStatBean(celComAssociatedGeneCount);
//				}
//				else {
//					throw new RuntimeException("Graph title '" + subNetworkTitle + "' is not recognized.");
//				}
//				
//				GOSlimmerController subNetworkController = new GOSlimmerController(subNetwork,subNetworkView,subNetworkStatBean);
//				NodeContextMenuListener subNetworkNcml = getGOSlimmerNodeContextMenuListener(subNetworkController);
//				subNetworkView.addNodeContextMenuListener(subNetworkNcml);
//			}
			
			//set the controllers for the goSlimPanel
			
			
		}
		else {
			//TODO perhaps shift focus to goslimpanel, or give info message
		}

		
		if (!alreadyOpened) {
			//initialize the goSlimPanel and add it to the cytopanel
			goSlimPanel = new GOSlimPanel(molFunController,bioProController,celComController);
			bioProController.setCoverageStatisticViewLabel(goSlimPanel.getBioProCoverage()); //TODO revise getter method
			molFunController.setCoverageStatisticViewLabel(goSlimPanel.getMolFunCoverage());
			celComController.setCoverageStatisticViewLabel(goSlimPanel.getCelComCoverage());
			cytoPanel.add(goSlimPanel);
			alreadyOpened = true;
		}
		
		
		
		//get the index of the panel and tell it to dock it
		int index = cytoPanel.indexOfComponent(goSlimPanel);
		cytoPanel.setSelectedIndex(index);
		cytoPanel.setState(CytoPanelState.DOCK);

	}
	
	private GOSlimmerController molFunController = null;
	private GOSlimmerController bioProController = null;
	private GOSlimmerController celComController = null;
	
	private void initializeControllersForSubGraphs(GeneAssociationReaderUtil garu) {
		GOSlimmerCoverageStatBean molFunStatBean = new GOSlimmerCoverageStatBean(garu.getMolecularFunctionGeneIds().size());
		GOSlimmerCoverageStatBean bioProStatBean = new GOSlimmerCoverageStatBean(garu.getBiologicalProcessGeneIds().size());
		GOSlimmerCoverageStatBean celComStatBean = new GOSlimmerCoverageStatBean(garu.getCellularComponentGeneIds().size());
		
//		molFunController = new GOSlimmerController(molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),molFunStatBean,goSlimPanel.getMolFunCoverage());
//		bioProController = new GOSlimmerController(bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),bioProStatBean,goSlimPanel.getBioProCoverage());
//		celComController = new GOSlimmerController(celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),celComStatBean,goSlimPanel.getCelComCoverage());
		
		molFunController = new GOSlimmerController(molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),molFunStatBean);
		bioProController = new GOSlimmerController(bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),bioProStatBean);
		celComController = new GOSlimmerController(celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),celComStatBean);
		
		
		
	}
	

	
	private NodeContextMenuListener getGOSlimmerNodeContextMenuListener(final GOSlimmerController controller) {
		//final GOSlimmerController slimmerController = this.controller;
		
		//instead of adding the above graphviewchangeevent listener, could instead add to the node context menu
		return new NodeContextMenuListener(){

			public void addNodeContextMenuItems(NodeView node, JPopupMenu menu) {
				
				//initialize the listener which will act when the buttons are pressed
				NodeContextMenuActionListener listener = new NodeContextMenuActionListener(node.getNode(),controller);
				
				//for each button, initialize it with the proper name, add the above actionlistener and add to the context menu
				for(String menuItemName:new String[]{"Collapse","Prune","Expand","Cancel","Select","Deselect"}) {
					JMenuItem menuItem = new JMenuItem(menuItemName);
					//menuItem.addActionListener(slimmerController);
					menuItem.addActionListener(listener);
					menu.add(menuItem);
				}
			}
			
		};
	}
	
	private final String bio_pro_name = "biological_process";
	private final String mol_fun_name = "molecular_function";
	private final String cel_com_name = "cellular_component";
	
	//The Subgraphs of the GO Network representing each of the three sub ontologies of the GO DAG
	private CyNetwork molFunSubGraph = null;
	private CyNetwork bioProSubGraph = null; 
	private CyNetwork celComSubGraph = null; 
	
	private List<CyNetwork> createOntologyNamespaceSubGraphs(CyNetwork network) {
		
		Set<Node> molFunNodes = new HashSet<Node>();
		Set<Edge> molFunEdges = new HashSet<Edge>();
		Set<Node> bioProNodes = new HashSet<Node>();
		Set<Edge> bioProEdges = new HashSet<Edge>();
		Set<Node> celComNodes = new HashSet<Node>();
		Set<Edge> celComEdges = new HashSet<Edge>();
		
		Iterator nodeIterator = network.nodesIterator();
		Node node = null;
		while(nodeIterator.hasNext()) {
			node = (Node) nodeIterator.next();
			String nodeOntologyNamespace = nodeAtt.getStringAttribute(node.getIdentifier(), "ontology.namespace");
			if (bio_pro_name.equalsIgnoreCase(nodeOntologyNamespace)) {
				bioProNodes.add(node);
				bioProEdges.addAll(network.getAdjacentEdgesList(node, true, true, true));
			}
			else if (mol_fun_name.equalsIgnoreCase(nodeOntologyNamespace)) {
				molFunNodes.add(node);
				molFunEdges.addAll(network.getAdjacentEdgesList(node, true, true, true));
			}
			else if (cel_com_name.equalsIgnoreCase(nodeOntologyNamespace)) {
				celComNodes.add(node);
				celComEdges.addAll(network.getAdjacentEdgesList(node, true, true, true));
			}
		}
		
		String networkTitle = network.getTitle();
		CyNetwork molFunNetwork = Cytoscape.createNetwork(molFunNodes, molFunEdges, networkTitle + "_molecular_function", network, true);
		CyNetwork bioProNetwork = Cytoscape.createNetwork(bioProNodes, bioProEdges, networkTitle + "_biological_process", network, true);
		CyNetwork celComNetwork = Cytoscape.createNetwork(celComNodes, celComEdges, networkTitle + "_cellular_component", network, true);
		List<CyNetwork> subNetworks = new ArrayList<CyNetwork>();
		subNetworks.add(molFunNetwork);
		subNetworks.add(bioProNetwork);
		subNetworks.add(celComNetwork);
		
		molFunSubGraph = molFunNetwork;
		bioProSubGraph = bioProNetwork;
		celComSubGraph = celComNetwork;
		
		return subNetworks;
	}

	private final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	
	/**This method will create a new attribute for the nodes of the DAG rooted at <code>node</code> which 
	 * contains a list of the genes which that node's GO term annotates by inference based on what genes 
	 * are annotated by that GO term's children.
	 * 
	 * It is implemented as a dynamic programming algorithm in that it will use the already calculated 
	 * list of inferred genes if it is already present for a node, only calculating it if it is not.
	 * 
	 * It is a recursive implementation, though it will only descend if the values have not already been 
	 * calculated.  Note that this will not scale to very deep graphs.
	 * 
	 * @param node root node of DAG which is to have parent attributions attached
	 * @param network TODO
	 * @return list of genes covered by <code>node</code>'s children (not including those covered by node itself 
	 * which are not covered by its children).
	 */
	private Set<String> getGenesCoveredByChildren(Node node, CyNetwork network) {
		//base case 1:  node has already been annotated with inferred covered gene list, so just return that
		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
		List<String> inferredCoveredGenes = nodeAtt.getListAttribute(node.getIdentifier(), inferredAnnotatedGenesAttributeName);
		//NOTE:  cannot accept empty lists as belonging to case 1 because the getListAttribute method will return empty lists for attributes which have not be set at all (instead of the expected null)
		if (inferredCoveredGenes != null && inferredCoveredGenes.size()>0) {
			//convert to a set, then return
			return listToSet(inferredCoveredGenes);
		}
		//base case 2: inferred covered genes needs to be calculated, but there are no children
		//get the children nodes;  note that children direct edges towards parents, that's why we're getting incoming edges
		int[] childEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
		if (childEdges == null || childEdges.length==0) {
			return new HashSet<String>();
		}
		//case 3:  inferred covered genes based on children needs to be calculated
		Set<String> inferredCoveredGenesS = new HashSet<String>();
		//iterate through the children of the node, for each one retrieving the directly and inferred covered genes and adding them to the inferredCoveredGenesS collection
		for(int childEdgeI:childEdges) {
			//get the child node; note that children direct edges to parents, so we use 'getSource()' instead of 'getTarget()'
			Node child = network.getEdge(childEdgeI).getSource();
			//get the child's directly covered genes
			List<String> childsDirectlyCoveredGenes = nodeAtt.getListAttribute(child.getIdentifier(), directlyAnnotatedGenesAttributeName);
			inferredCoveredGenesS.addAll(childsDirectlyCoveredGenes);
			
			//get the childs inferred covered genes
			Set<String> childsInferredCoveredGenes = getGenesCoveredByChildren(child,network);
			//set the child node's inferred covered genes attribute
			nodeAtt.setListAttribute(child.getIdentifier(), inferredAnnotatedGenesAttributeName, setToList(childsInferredCoveredGenes));
			inferredCoveredGenesS.addAll(childsInferredCoveredGenes);
		}
		return inferredCoveredGenesS;
	}
	
	private Set<String> listToSet(List<String> list) {
		Set<String> set = new HashSet<String>();
		set.addAll(list);
		return set;
	}
	
	private List<String> setToList(Set<String> set) {
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		return list;
	}

	

}
