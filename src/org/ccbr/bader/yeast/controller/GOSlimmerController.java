package org.ccbr.bader.yeast.controller;

import giny.model.Node;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.GONamespace;
import org.ccbr.bader.yeast.GOSlimmer;
import org.ccbr.bader.yeast.GOSlimmerSession;
import org.ccbr.bader.yeast.GOSlimmerUtil;
import org.ccbr.bader.yeast.export.GeneAnnotationRemapWriter;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.attr.MultiHashMapDefinition;
import cytoscape.view.CyNetworkView;

public class GOSlimmerController  {

	private CyNetwork network;
	private CyNetworkView networkView;
	private GOSlimmerCoverageStatBean statBean;
	private JLabel inferredCoverageStatisticViewLabel;
	private GONamespace namespace;
	private GOSlimmerSession session;
	
	public GOSlimmerController(GONamespace namespace,CyNetwork network, CyNetworkView networkView, GOSlimmerCoverageStatBean statBean,GOSlimmerSession session) {
		this.namespace = namespace;
		this.network=network;
		this.networkView = networkView;
		this.statBean = statBean;
		this.session = session;
	}

//	public GOSlimmerController(CyNetwork network, CyNetworkView networkView, GOSlimmerCoverageStatBean statBean, JLabel viewStatLabel) {
//		this.network=network;
//		this.networkView = networkView;
//		this.statBean = statBean;
//		this.coverageStatisticViewLabel = viewStatLabel; 
//	}

	private String collapseButtonText = "Collapse";
	private String expandButtonText = "Expand";
	private String pruneButtonText = "Prune";
	private String cancelButtonText = "Cancel";
	
	private boolean useFiniteExpansionDepth = false;
	private int nodeExpansionDepth = 1;

	
	//two ways to implement:  hide as we go down, or hide only as we find edges without childre
	//two kinds of parents:  those which are part of the dag to be collapsed, and those which aren't
	//nodes with parents of the latter type should not be collapsed - they are not part of the dag to be collapsed
	//collateral damage mode
	public void collapseNode(Node snode) {
		networkView.getNodeView(snode);
		
		//retrieve the outgoing edges, such that we can collapse them into this one
		int[] outgoingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, true, false);
		if (outgoingEdges==null) return;
		for (int outgoingEdge:outgoingEdges) {
			EdgeView ev = networkView.getEdgeView(outgoingEdge);
			
//			int edgeId =ev.getEdge().getRootGraphIndex();
//			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
//			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || !edgeIsHiddenPropertyValue) {
//				networkView.putClientData(edgeIsHiddenPropertyName, true);
				networkView.hideGraphObject(ev);
				
				pruneNode(ev.getEdge().getSource());
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
		}
		updateViewStatistics();
	}
	
	public void pruneNode(Node snode) {
		removeNodeFromSlimSet(snode);
		//hide this node for starters
		networkView.hideGraphObject(networkView.getNodeView(snode));
		//retrieve the outgoing edges, such that we can collapse them into this one
		int[] outgoingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, true, false);
		for (int outgoingEdge:outgoingEdges) {
			EdgeView ev = networkView.getEdgeView(outgoingEdge);
			
//			int edgeId =ev.getEdge().getRootGraphIndex();
//			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
//			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || !edgeIsHiddenPropertyValue) {
//				networkView.putClientData(edgeIsHiddenPropertyName, true);
				networkView.hideGraphObject(ev);
				
				pruneNode(ev.getEdge().getSource());
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
			
		}
		//TODO move this call to a special public method, and create private version of prunenode which doesn't call this, since it is wasteful
		updateViewStatistics();
	}
	

	
	public void expandNode(Node snode) {
		if (useFiniteExpansionDepth) {
			expandNodeToDepth(snode, nodeExpansionDepth);
		}
		else {
			expandNodeUnlimited(snode);
		}
	}
	
	public void expandNodeUnlimited(Node snode) {
		networkView.showGraphObject(networkView.getNodeView(snode));
		
		//retrieve the incoming edges, such that we can expand them
		int[] incomingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, true, false);
		for (int incomingEdge:incomingEdges) {
			EdgeView ev = networkView.getEdgeView(incomingEdge);
			
			int edgeId =ev.getEdge().getRootGraphIndex();
//			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
//			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || edgeIsHiddenPropertyValue) {
//				networkView.putClientData(edgeIsHiddenPropertyName, false);
				networkView.showGraphObject(ev);
//				BioDataServer server = Cytoscape.getBioDataServer();
//				AnnotationDescription[] desc = server.getAnnotationDescriptions();
//				desc[0].getCurator();
//				CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
//				nodeAtt.getListAttribute(snode.getIdentifier(), "peh");
//				String[] attNames = nodeAtt.getAttributeNames();
				
//				List annotIDList = nodeAtt.getListAttribute(snode.getIdentifier(), "annotation.DB_Object_ID");
				expandNode(ev.getEdge().getSource());
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
		}
	}
	
	/**
	 * @param snode the node to expand
	 * @param depth the depth to which the DAG should be expanded
	 */
	public void expandNodeToDepth(Node snode,int depth) {
		if (depth <=0) return;
		networkView.showGraphObject(networkView.getNodeView(snode));
		
		//retrieve the incoming edges, such that we can expand them
		int[] incomingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, true, false);
		for (int incomingEdge:incomingEdges) {
			EdgeView ev = networkView.getEdgeView(incomingEdge);
			
			int edgeId =ev.getEdge().getRootGraphIndex();
			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || edgeIsHiddenPropertyValue) {
//				networkView.putClientData(edgeIsHiddenPropertyName, false);
				networkView.showGraphObject(ev);
//				BioDataServer server = Cytoscape.getBioDataServer();
//				AnnotationDescription[] desc = server.getAnnotationDescriptions();
//				desc[0].getCurator();
//				CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
//				nodeAtt.getListAttribute(snode.getIdentifier(), "peh");
//				String[] attNames = nodeAtt.getAttributeNames();
				
//				List annotIDList = nodeAtt.getListAttribute(snode.getIdentifier(), "annotation.DB_Object_ID");
				expandNodeToDepth(ev.getEdge().getSource(),depth -1);
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
		}
	}
	
	/**
	 * @param snode the node to expand
	 * @param depth the depth to which the DAG should be expanded
	 */
	public Collection<Node> expandNodeToDepthAndReturnDAGNodes(Node snode,int depth) {
		List<Node> l= new ArrayList<Node>();
		l.add(snode);
		if (depth <=0) {
			l.add(snode);
			return l;
		}
		networkView.showGraphObject(networkView.getNodeView(snode));
		
		//retrieve the incoming edges, such that we can expand them
		int[] incomingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, true, false);
		for (int incomingEdge:incomingEdges) {
			EdgeView ev = networkView.getEdgeView(incomingEdge);
			
			int edgeId =ev.getEdge().getRootGraphIndex();
			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || edgeIsHiddenPropertyValue) {
//				networkView.putClientData(edgeIsHiddenPropertyName, false);
				networkView.showGraphObject(ev);
				
//				BioDataServer server = Cytoscape.getBioDataServer();
//				AnnotationDescription[] desc = server.getAnnotationDescriptions();
//				desc[0].getCurator();
//				CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
//				nodeAtt.getListAttribute(snode.getIdentifier(), "peh");
//				String[] attNames = nodeAtt.getAttributeNames();
				
//				List annotIDList = nodeAtt.getListAttribute(snode.getIdentifier(), "annotation.DB_Object_ID");
				l.addAll(expandNodeToDepthAndReturnDAGNodes(ev.getEdge().getSource(),depth -1));
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
		}
		return l;
	}
	
	private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	private static final CyAttributes netAtt = Cytoscape.getNetworkAttributes();
	
	
	public void addNodeToSlimSet(Node node) {
		//set the 'selected for slim set' attribute to true
		nodeAtt.setAttribute(node.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName, true);

//		System.out.println("att val type is: " + netAtt.getMultiHashMapDefinition().getAttributeValueType("GOSLIMMER.STATBEAN"));
//		if (netAtt.getMultiHashMapDefinition().getAttributeValueType("GOSLIMMER.STATBEAN") == -1) {
//			//attribute has not yet been defined
//			MultiHashMapDefinition netMHMDef = netAtt.getMultiHashMapDefinition();
//			netMHMDef.defineAttribute("GOSLIMMER.STATBEAN", Byte.valueOf("5"), null); //5 is chosen because it is out of the range of the predefined attribute types
//		}
//		Object something = netAtt.getMultiHashMap().getAttributeValue(network.getIdentifier(), "GOSLIMMER.STATBEAN", null);
		//netAtt.getMultiHashMap().setAttributeValue(network.getIdentifier(), "GOSLIMMER.STATBEAN", something, null);
		//netAtt.getMultiHashMapDefinition();
		//network.getClientData(GOSlimmer.coverageDataBeanClientDataName);
		//TODO update coverage statistics
		statBean.addToSlimSet(node);
		updateViewStatistics();
	}
	
	public void removeNodeFromSlimSet(Node node) {
		//set the 'selected for slim set' attribute to false
		nodeAtt.setAttribute(node.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName, false);
		//TODO update coverage statistics
		statBean.removeFromSlimSet(node);
		updateViewStatistics();
	}
	
	
	DecimalFormat formatter = new DecimalFormat("00.00%");
	private void updateViewStatistics() {
		
		//depending on whether displayUserGeneCoverageStatistics has been set or not, either show the full coverage information, or only for the user specified genes.
		double inferredCoverage = displayUserGeneCoverageStatistics?statBean.fractionInferredCoveredUserGenes():statBean.fractionInferredCovered();
		double directCoverage = displayUserGeneCoverageStatistics?statBean.fractionDirectlyCoveredUserGenes():statBean.fractionDirectlyCovered();
		
		//TODO consider revising this condition, since it might hide the face that the coverageStatisticViewLabel hasn't been initialized
		if (this.inferredCoverageStatisticViewLabel!=null) this.inferredCoverageStatisticViewLabel.setText("Inferred Coverage: " + formatter.format(inferredCoverage));
		if (this.directCoverageStatisticViewLabel!=null) this.directCoverageStatisticViewLabel.setText("Direct Coverage: " + formatter.format(directCoverage));
	}

	public GOSlimmerCoverageStatBean getStatBean() {
		return statBean;
	}

	public void setStatBean(GOSlimmerCoverageStatBean statBean) {
		this.statBean = statBean;
	}

	public CyNetwork getNetwork() {
		return network;
	}

	public void setNetwork(CyNetwork network) {
		this.network = network;
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}

	public void setNetworkView(CyNetworkView networkView) {
		this.networkView = networkView;
	}

	public JLabel getInferredCoverageStatisticViewLabel() {
		return inferredCoverageStatisticViewLabel;
	}

	public void setInferredCoverageStatisticViewLabel(JLabel coverageStatisticViewLabel) {
		this.inferredCoverageStatisticViewLabel = coverageStatisticViewLabel;
	}
	
	JLabel directCoverageStatisticViewLabel;
	
	
	public JLabel getDirectCoverageStatisticViewLabel() {
		return directCoverageStatisticViewLabel;
	}

	public void setDirectCoverageStatisticViewLabel(
			JLabel directCoverageStatisticViewLabel) {
		this.directCoverageStatisticViewLabel = directCoverageStatisticViewLabel;
	}
	
	public void removeCoverageAttributes() {
		//TODO possibly do this only for this graph's nodes, by iterating over the nodes of this network
		Iterator<Node> nodeI = network.nodesIterator();
		
		boolean daganAttDefined = false;
		boolean iaganAttDefined = false;
		if (nodeAtt.getMultiHashMapDefinition().getAttributeValueType(GOSlimmer.directlyAnnotatedGenesAttributeName)>0) daganAttDefined = true;
		if (nodeAtt.getMultiHashMapDefinition().getAttributeValueType(GOSlimmer.inferredAnnotatedGenesAttributeName)>0) iaganAttDefined = true;
		while(nodeI.hasNext()) {
			Node node = nodeI.next();
			
			if (daganAttDefined) nodeAtt.deleteAttribute(node.getIdentifier(),GOSlimmer.directlyAnnotatedGenesAttributeName);
			if (iaganAttDefined) nodeAtt.deleteAttribute(node.getIdentifier(),GOSlimmer.inferredAnnotatedGenesAttributeName);
		}
//		nodeAtt.deleteAttribute(GOSlimmer.directlyAnnotatedGenesAttributeName);
//		nodeAtt.deleteAttribute(GOSlimmer.inferredAnnotatedGenesAttributeName);
	}

	public void assignCoverageAttributesToNetworks(Map<String, List<String>> goIdToGeneIdMap) {
		//scratch that, instead iterate through the nodes of the GO DAG graph, and attach annotated gene list attributes accordingly
		Iterator<Node> nodeI = network.nodesIterator();
		while (nodeI.hasNext()) {
			Node node = nodeI.next();
			String nodeGoId = node.getIdentifier();
			//get the genes which this go term annotates, according to the gene association file
			List<String> nodeAnnotatedGeneIds = goIdToGeneIdMap.get(nodeGoId);
			
			//attach the gene id list as an attribute, if it exists 
			if (nodeAnnotatedGeneIds!=null && nodeAnnotatedGeneIds.size()>0) {
				nodeAtt.setListAttribute(nodeGoId, GOSlimmer.directlyAnnotatedGenesAttributeName, nodeAnnotatedGeneIds);
			}
			
		}
		//now annotated each node with the genes it annotated indirectly, through inference, from the genes which it's children inference
		nodeI = network.nodesIterator();
		
		while(nodeI.hasNext()) {
			Node node = nodeI.next();
			//see if the inferred coverred genes list attribute has already been calculated and set
			List<String> inferredCoveredGenesL = nodeAtt.getListAttribute(node.getIdentifier(), GOSlimmer.inferredAnnotatedGenesAttributeName);
			if (inferredCoveredGenesL ==null || inferredCoveredGenesL.size()==0) {
				//inferred coverred genes list has not already been calculated, so calculate and set it
				Set<String> inferredCoveredGenesS = GOSlimmerUtil.getGenesCoveredByChildren(node, network);
				nodeAtt.setListAttribute(node.getIdentifier(), GOSlimmer.inferredAnnotatedGenesAttributeName, GOSlimmerUtil.setToList(inferredCoveredGenesS));
			}
			
		}
		
	}

	/**This method resents the coverage statistics in the model layer, and recalculates them based on which nodes have been selected 
	 * for inclusion in the slim set (based on the GOSlimmer.goNodeInSimlSetAttributeName CyAttribute of the node).
	 * It then updates the view layer to reflect the new statistics information.
	 * 
	 * 
	 * @param maxGeneSetSize the new maxGeneSetSize to be used for initializing the coverage statistics bean
	 */
	public void resetAndRecalculateStatisticsBean(int maxGeneSetSize) {
		//initialize the new stats bean
		this.statBean = new GOSlimmerCoverageStatBean(maxGeneSetSize,this.session);
		//iterate through the network and add nodes which have their .. attribute set to true  to the slimset, updating statistics accordingly
		//TODO possibly do this only for this graph's nodes, by iterating over the nodes of this network
		Iterator<Node> nodeI = network.nodesIterator();
		while(nodeI.hasNext()) {
			Node node = nodeI.next();
			//determine if node has been selected for inclusion in slimset
			Boolean isInSlimSet = nodeAtt.getBooleanAttribute(node.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName);
			//add to the stats bean's statistics info
			if (isInSlimSet!=null && isInSlimSet) this.statBean.addToSlimSet(node);
			
			//remove the user gene attributes from the node, since they are no longer relevant
			if (GOSlimmerUtil.areUserGeneAttributesDefined()) GOSlimmerUtil.removeUserGeneAttributes(node);
		}
		
		this.updateViewStatistics();
	}

	public int getExpansionDepth() {
		return nodeExpansionDepth;
	}

	public void setExpansionDepth(int newExpansionDepth) {
		this.nodeExpansionDepth = newExpansionDepth;
		
	}

	private boolean displayUserGeneCoverageStatistics = false;
	
	public boolean isDisplayUserGeneCoverageStatistics() {
		return displayUserGeneCoverageStatistics;
	}

	public void setDisplayUserGeneCoverageStatistics(
			boolean displayUserGeneCoverageStatistics) {
		this.displayUserGeneCoverageStatistics = displayUserGeneCoverageStatistics;
		updateViewStatistics();
	}
	
	/**This sets the controllers behaviour when expanding node views, for whether it will expand nodes without limit, or only to a finite depth
	 * The depth to which the node will be expanded is set based on the getExpansionDepth and setExpansionDepth methods.
	 * 
	 * @param useFiniteExpansionDepth whether or not to use finite expansion depth
	 */
	public void setUseFiniteExpansionDepth(boolean useFiniteExpansionDepth) {
		this.useFiniteExpansionDepth = useFiniteExpansionDepth;
	}

	
	
//	public void getNetworkViewFocus() {
//		//TODO replace with undeprecated alternatives
//		Cytoscape.setCurrentNetwork(this.network.getIdentifier());
//		Cytoscape.setCurrentNetworkView(this.networkView.getIdentifier());
//	}


	
	
}
