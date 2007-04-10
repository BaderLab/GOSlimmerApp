package org.ccbr.bader.yeast.controller;

import giny.model.Node;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.ccbr.bader.yeast.GOSlimmer;
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
	private JLabel coverageStatisticViewLabel;

	public GOSlimmerController(CyNetwork network, CyNetworkView networkView, GOSlimmerCoverageStatBean statBean) {
		this.network=network;
		this.networkView = networkView;
		this.statBean = statBean;
	}

	public GOSlimmerController(CyNetwork network, CyNetworkView networkView, GOSlimmerCoverageStatBean statBean, JLabel viewStatLabel) {
		this.network=network;
		this.networkView = networkView;
		this.statBean = statBean;
		this.coverageStatisticViewLabel = viewStatLabel; 
	}

	private String collapseButtonText = "Collapse";
	private String expandButtonText = "Expand";
	private String pruneButtonText = "Prune";
	private String cancelButtonText = "Cancel";
	

	
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
			
			int edgeId =ev.getEdge().getRootGraphIndex();
			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || !edgeIsHiddenPropertyValue) {
				networkView.putClientData(edgeIsHiddenPropertyName, true);
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
			
			int edgeId =ev.getEdge().getRootGraphIndex();
			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || !edgeIsHiddenPropertyValue) {
				networkView.putClientData(edgeIsHiddenPropertyName, true);
				networkView.hideGraphObject(ev);
				
				pruneNode(ev.getEdge().getSource());
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
			
		}
		updateViewStatistics();
	}
	
	public void expandNode(Node snode) {
		networkView.showGraphObject(networkView.getNodeView(snode));
		
		//retrieve the incoming edges, such that we can expand them
		int[] incomingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, true, false);
		for (int incomingEdge:incomingEdges) {
			EdgeView ev = networkView.getEdgeView(incomingEdge);
			
			int edgeId =ev.getEdge().getRootGraphIndex();
			String edgeIsHiddenPropertyName =  "Edge " + edgeId + " hidden";
			Boolean edgeIsHiddenPropertyValue = (Boolean) networkView.getClientData(edgeIsHiddenPropertyName);
			//if (edgeIsHiddenPropertyValue== null || edgeIsHiddenPropertyValue) {
				networkView.putClientData(edgeIsHiddenPropertyName, false);
				networkView.showGraphObject(ev);
//				BioDataServer server = Cytoscape.getBioDataServer();
//				AnnotationDescription[] desc = server.getAnnotationDescriptions();
//				desc[0].getCurator();
				CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
				nodeAtt.getListAttribute(snode.getIdentifier(), "peh");
				String[] attNames = nodeAtt.getAttributeNames();
				
				List annotIDList = nodeAtt.getListAttribute(snode.getIdentifier(), "annotation.DB_Object_ID");
				expandNode(ev.getEdge().getSource());
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
		}
	}
	
	/**
	 * @param snode the node to expand
	 * @param depth the depth to which the DAG should be expanded
	 */
	public void expandNode(Node snode,int depth) {
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
				networkView.putClientData(edgeIsHiddenPropertyName, false);
				networkView.showGraphObject(ev);
//				BioDataServer server = Cytoscape.getBioDataServer();
//				AnnotationDescription[] desc = server.getAnnotationDescriptions();
//				desc[0].getCurator();
				CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
				nodeAtt.getListAttribute(snode.getIdentifier(), "peh");
				String[] attNames = nodeAtt.getAttributeNames();
				
				List annotIDList = nodeAtt.getListAttribute(snode.getIdentifier(), "annotation.DB_Object_ID");
				expandNode(ev.getEdge().getSource(),depth -1);
			//}
			//else, we've already traversed this part of the graph, so just in case it is cyclic, don't proceed; otherwise we will have a stack overflow
		}
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

		this.coverageStatisticViewLabel.setText(String.valueOf(formatter.format(statBean.fractionCovered())));
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

	public JLabel getCoverageStatisticViewLabel() {
		return coverageStatisticViewLabel;
	}

	public void setCoverageStatisticViewLabel(JLabel coverageStatisticViewLabel) {
		this.coverageStatisticViewLabel = coverageStatisticViewLabel;
	}
	
	public void removeCoverageAttributes() {
		nodeAtt.deleteAttribute(GOSlimmer.directlyAnnotatedGenesAttributeName);
		nodeAtt.deleteAttribute(GOSlimmer.inferredAnnotatedGenesAttributeName);
	}
	
	
}
