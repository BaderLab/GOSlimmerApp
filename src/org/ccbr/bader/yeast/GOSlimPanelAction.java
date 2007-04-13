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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import org.ccbr.bader.yeast.view.gui.AdvancedViewSettingsPanel;
import org.ccbr.bader.yeast.view.gui.FileExportPanel;
import org.ccbr.bader.yeast.view.gui.GOSlimmerGeneAssociationDialog;
import org.ccbr.bader.yeast.view.gui.NodeContextMenuActionListener;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.bookmarks.Attribute;
import cytoscape.bookmarks.Bookmarks;
import cytoscape.bookmarks.DataSource;
import cytoscape.data.CyAttributes;
import cytoscape.data.annotation.AnnotationDescription;
import cytoscape.data.attr.MultiHashMapDefinition;
import cytoscape.data.servers.BioDataServer;
import cytoscape.ding.DingNetworkView;
import cytoscape.util.BookmarksUtil;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelContainer;
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
	
	//private GOSlimmerController controller;
	
//	{
//		//define attributes
//		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
//		
//			nodeAtt.getMultiHashMapDefinition().defineAttribute(GOSlimmer.directlyAnnotatedGenesAttributeName, MultiHashMapDefinition., keyTypes)
//		}
//	}
	
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

		GOSlimmer.ontologyName = network.getTitle();

		//GOSlimmerCoverageStatBean statBean=null;
		
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
			
			//TODO implement this in a better manner
			networkView.putClientData(networkViewGoSlimClientDataPropertyName,Boolean.TRUE);
			
		}
		else {
			//TODO perhaps shift focus to goslimpanel, or give info message
		}

		
		if (!alreadyOpened) {
			//initialize the goSlimPanel and add it to the cytopanel
			Map<GONamespace,GOSlimmerController> namespaceToController = new HashMap<GONamespace, GOSlimmerController>();
			namespaceToController.put(GONamespace.BioPro, bioProController);
			namespaceToController.put(GONamespace.MolFun,molFunController);
			namespaceToController.put(GONamespace.CelCom, celComController);
			
			goSlimPanel = new GOSlimPanel(namespaceToController);
			bioProController.setCoverageStatisticViewLabel(goSlimPanel.getBioProCoverage()); //TODO revise so that this step is unnecessary
			molFunController.setCoverageStatisticViewLabel(goSlimPanel.getMolFunCoverage()); //TODO revise so that this step is unnecessary
			celComController.setCoverageStatisticViewLabel(goSlimPanel.getCelComCoverage()); //TODO revise so that this step is unnecessary
			
			//add the advanced view settings panel to the goSlimPanel
			goSlimPanel.add(new AdvancedViewSettingsPanel(namespaceToController.values()));
			
			goSlimPanel.add(new FileExportPanel(namespaceToController.values()));
			
			cytoPanel.add(goSlimPanel);
			goSlimPanel.add(new GOSlimmerGeneAssociationDialog(namespaceToController),0);
			alreadyOpened = true;
		}
		
		
		
		//get the index of the panel and tell it to dock it
		int index = cytoPanel.indexOfComponent(goSlimPanel);
		cytoPanel.setSelectedIndex(index);
		cytoPanel.setState(CytoPanelState.DOCK);

	}
	
	private void promptUserForGeneAssociationFile() {
//		GOSlimmerGeneAssociationDialog gad = new GOSlimmerGeneAssociationDialog();
		
		//TODO remove obsolete prototype code of JDialog subclass, as we've copied the contents to a non-anonymous type
//		JDialog dialog = new JDialog(){
//			
//			{
//				//TODO revise with proper exception handling
//				try {
//					add(createAnnotationComboBox());
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			
//			//TODO inform developers that they should use the covert exceptions design pattern and not throw a jaxbexception, since this method should abstract above the details of xml parsing
//			private JComboBox createAnnotationComboBox() throws JAXBException, IOException {
//				
//				//create the new combo box
//				JComboBox acb = new JComboBox();
//				
//				Map<String,String> annotationURLMap = new HashMap<String, String>();
//				
//				
//				//retrieve the gene association annotations from the bookmarks library
//				Bookmarks bookmarks = Cytoscape.getBookmarks();
//				List<DataSource> annotations = BookmarksUtil.getDataSourceList("annotation", bookmarks.getCategory());
//				
//				//intialize the annotation to url map, and populate the combo box with the annotation names
//				for(DataSource annot:  annotations) {
//					String annotName = annot.getName();
//					annotationURLMap.put(annotName, annot.getHref());
//					acb.addItem(annotName);
//					//TODO see if we need to extract the source attributes
//				}
//				return null;
//			}
//		};
		
	}
	
	//TODO remove this commented out method, as it was copied from the ucsd.edu ontology and annotation TableImport plugin
//	private void setAnnotationComboBox() throws JAXBException, IOException {
//		Bookmarks bookmarks = Cytoscape.getBookmarks();
//		List<DataSource> annotations = BookmarksUtil.getDataSourceList("annotation",
//		                                                               bookmarks.getCategory());
//		String key = null;
//
//		annotationComboBox.addItem(DEF_ANNOTATION_ITEM);
//
//		for (DataSource source : annotations) {
//			key = source.getName();
//			annotationComboBox.addItem(key);
//			annotationUrlMap.put(key, source.getHref());
//			annotationFormatMap.put(key, source.getFormat());
//
//			final Map<String, String> attrMap = new HashMap<String, String>();
//
//			for (Attribute attr : source.getAttribute()) {
//				attrMap.put(attr.getName(), attr.getContent());
//			}
//
//			annotationAttributesMap.put(key, attrMap);
//		}
//
//		// annotationComboBox.setToolTipText(getAnnotationTooltip());
//	}

	private GOSlimmerController molFunController = null;
	private GOSlimmerController bioProController = null;
	private GOSlimmerController celComController = null;
	
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
//		molFunController = new GOSlimmerController(molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),molFunStatBean);
//		bioProController = new GOSlimmerController(bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),bioProStatBean);
//		celComController = new GOSlimmerController(celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),celComStatBean);
		
		
		
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
