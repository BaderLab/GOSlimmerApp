package org.ccbr.bader.yeast;

import giny.model.Edge;
import giny.model.Node;
import giny.view.NodeView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.xml.bind.JAXBException;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;
import org.ccbr.bader.yeast.view.gui.AdvancedViewSettingsPanel;
import org.ccbr.bader.yeast.view.gui.FileExportPanel;
import org.ccbr.bader.yeast.view.gui.GOSlimmerGeneAssociationDialog;
import org.ccbr.bader.yeast.view.gui.NodeContextMenuActionListener;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.bookmarks.Bookmarks;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualStyle;
import ding.view.NodeContextMenuListener;

public class GOSlimmerSession {

	//The Subgraphs of the GO Network representing each of the three sub ontologies of the GO DAG
	private CyNetwork molFunSubGraph = null;
	private CyNetwork bioProSubGraph = null; 
	private CyNetwork celComSubGraph = null; 
	
	//TODO migrate this info to the GONamespace enum
	private final String bio_pro_name = "biological_process";
	private final String mol_fun_name = "molecular_function";
	private final String cel_com_name = "cellular_component";
	
	private GOSlimmerController molFunController = null;
	private GOSlimmerController bioProController = null;
	private GOSlimmerController celComController = null;
	
	private String ontologyName;
	
	private GeneAssociationReaderUtil garu;
	
	public GeneAssociationReaderUtil getGaru() {
		return garu;
	}

	public void setGaru(GeneAssociationReaderUtil garu) {
		this.garu = garu;
	}

	public GOSlimmerSession(CyNetwork goNetwork) {


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

		ontologyName = network.getTitle();

		//GOSlimmerCoverageStatBean statBean=null;
		
		//Get the list of available annotations
//		Bookmarks bookmarks = null;
//		try {
//			bookmarks = Cytoscape.getBookmarks();
//		} catch (JAXBException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		//we can use this to get a list of the ontology names
		//Set<String> ontologyNames = Cytoscape.getOntologyServer().getOntologyNames();

		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
		
		int associatedGeneCount = 0;

		//create the subgraphs
		createOntologyNamespaceSubGraphs(network);
		//initialize the subgraphs with their controllers and coverage statistics beans

		
		subNetworks.add(molFunSubGraph);
		subNetworks.add(bioProSubGraph);
		subNetworks.add(celComSubGraph);
		

			

		for (CyNetwork subNetwork:subNetworks) {	
			CyNetworkView subNetworkView = Cytoscape.createNetworkView(subNetwork);
			
			VisualStyle vs = Cytoscape.getVisualMappingManager().setVisualStyle("GOSLIMMERVS");
			subNetworkView.redrawGraph(false,false);
			//Cytoscape.getVisualMappingManager().setVisualStyle(vs);
			subNetworkView.setVisualStyle("GOSLIMMERVS");
			
		}
		//Note, this must be done after the network views are created, since the controllers are associated with a particular view ; TODO eliminate this dependancy, view should be modifiable on it's own, though the hiding and unhiding of nodes should effect the model
		//TODO continues from above:  view should need to know about the controller, but controller shouldn't need to know view enough to do detailed view manipulation;  implement view manipulation code in node context menu listener, and have it call the controller only when it needs to select/deselect nodes based on their being hidden or not
		initializeControllersForSubGraphs();
		
		//initialize the nodecontextmenu's for each of the subgraphs views;  NOTE if a networkview does not yet exist for these graphs, then this fail without any warning or indication
		Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(molFunController));
		Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(bioProController));
		Cytoscape.getNetworkView(celComSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(celComController));
		

		
		
		
		//get the index of the panel and tell it to dock it
//		int index = cytoPanel.indexOfComponent(goSlimPanel);
//		cytoPanel.setSelectedIndex(index);
//		cytoPanel.setState(CytoPanelState.DOCK);
		
	}
	
	private final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	
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
	
	private void initializeControllersForSubGraphs() {
//		GOSlimmerCoverageStatBean molFunStatBean = new GOSlimmerCoverageStatBean(garu.getMolecularFunctionGeneIds().size());
//		GOSlimmerCoverageStatBean bioProStatBean = new GOSlimmerCoverageStatBean(garu.getBiologicalProcessGeneIds().size());
//		GOSlimmerCoverageStatBean celComStatBean = new GOSlimmerCoverageStatBean(garu.getCellularComponentGeneIds().size());
		
//		molFunController = new GOSlimmerController(molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),molFunStatBean,goSlimPanel.getMolFunCoverage());
//		bioProController = new GOSlimmerController(bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),bioProStatBean,goSlimPanel.getBioProCoverage());
//		celComController = new GOSlimmerController(celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),celComStatBean,goSlimPanel.getCelComCoverage());
		
		
		molFunController = new GOSlimmerController(GONamespace.MolFun,molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),new GOSlimmerCoverageStatBean(1));
		bioProController = new GOSlimmerController(GONamespace.BioPro,bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),new GOSlimmerCoverageStatBean(1));
		celComController = new GOSlimmerController(GONamespace.CelCom,celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),new GOSlimmerCoverageStatBean(1));
		this.namespaceToController.put(GONamespace.BioPro, bioProController);
		this.namespaceToController.put(GONamespace.CelCom, celComController);
		this.namespaceToController.put(GONamespace.MolFun, molFunController);
//		molFunController = new GOSlimmerController(molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),molFunStatBean);
//		bioProController = new GOSlimmerController(bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),bioProStatBean);
//		celComController = new GOSlimmerController(celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),celComStatBean);
		
		
		
	}

	private Map<GONamespace, GOSlimmerController> namespaceToController = new HashMap();
	
	public Map<GONamespace, GOSlimmerController> getNamespaceToController() {
		return namespaceToController;
	}

	public String getOntologyName() {
		return ontologyName;
	}

	public void setOntologyName(String ontologyName) {
		this.ontologyName = ontologyName;
	}

}
