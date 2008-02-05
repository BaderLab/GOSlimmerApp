/**
 * * Copyright (c) 2007 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * * Research, University of Toronto
 * *
 * * Code written by: Michael Matan
 * * Authors: Michael Matan, Gary D. Bader
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * University of Toronto
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * University of Toronto
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * University of Toronto
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * Description: Controller for manipulation of a GOSlimmer GO Namespace subgraph network and associated model objects
 */

package org.ccbr.bader.yeast.controller;

import giny.model.Node;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

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
import org.ccbr.bader.yeast.view.gui.UserGeneSetImportPanel;
import org.ccbr.bader.yeast.view.gui.SelectedGOTermsPanel;
import org.ccbr.bader.yeast.view.gui.GOSlimmerGUIViewSettings;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.attr.MultiHashMapDefinition;
import cytoscape.view.CyNetworkView;
import cytoscape.task.TaskMonitor;

/**Controller for manipulation of a GOSlimmer GO Namespace subgraph network and associated model objects.
 * Each instance is associated with a single GO namespace subgraph and provides an interface for manipulating that 
 * subgraph.
 * 
 * @author mikematan
 *
 */
public class GOSlimmerController  {

	/**
	 * The network model of the GO Namespace subgraph which this controller controls
	 */
	private CyNetwork network;
	/**
	 * The network view associated with the controlled network
	 */
	private CyNetworkView networkView;
	/**
	 * The statistics model bean which manages information on the coverage statistics for the controlled network
	 */
	private GOSlimmerCoverageStatBean statBean;
	private JLabel inferredCoverageStatisticViewLabel;
    private SelectedGOTermsPanel selectedGOTermsPanel;      // Collapsable panel for list of currently selected terms

    private Set<Node> visibleNodes;                         // Set of visible nodes for this network
    private Set<Integer> visibleEdges;                      // Set of indices of visible edges for this network
    private Set<Node> prunedNodes;                          // Set of nodes which have been pruned by the user

    /**
	 * The GO Namespace of the controlled network subgraph
	 */
	private GONamespace namespace;
	/**
	 * The session which this controller is a part of
	 */
	private GOSlimmerSession session;
	
	/**Creates a new controller for the specified network, as part of the specified session
	 * @param namespace the GO namespace of the managed network
	 * @param network the network model of the GO namespace subgraph which is being controlled
	 * @param networkView the network view associated with the network
	 * @param statBean the statistics data model bean associated with the network
	 * @param session the GOSlimmer session to which this controller belongs
	 */
	public GOSlimmerController(GONamespace namespace,CyNetwork network, CyNetworkView networkView, GOSlimmerCoverageStatBean statBean,GOSlimmerSession session) {
		this.namespace = namespace;
		this.network=network;
		this.networkView = networkView;
		this.statBean = statBean;
		this.session = session;
        visibleNodes = new HashSet<Node>();
        visibleEdges = new HashSet<Integer>();
        prunedNodes = new HashSet<Node>();
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

    /**
	 * View manipulation method which collapses the descendants of a GO node into that node.  That is, the descendants will 
	 * no longer be visible in the network view. 
	 * @param snode the network node for whom the descendants are to be collapsed
     *
     * Restructured by Laetitia Morrison, Jan 08
     * Modified from recursive implementation to iterative to improve speed of execution
	 */
	public void collapseNode(Node snode) {
		Set<Node> nodesToIterate = new HashSet<Node>();
        Set<Node> nodesToIterateNextLevel = new HashSet<Node>();

        nodesToIterate.add(snode);

        while (!nodesToIterate.isEmpty()) {

            for (Node node : nodesToIterate) {

                // get incoming edges (from children nodes)
                int[] incomingEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
                for (int incomingEdge : incomingEdges) {

                    // hide this edge
                    hideEdge(incomingEdge);

                    // get the child node, and hide it if it has no other visible parent
                    Node childNode = network.getEdge(incomingEdge).getSource();

                    boolean hideNode = true;
                    int[] parentEdges = network.getAdjacentEdgeIndicesArray(childNode.getRootGraphIndex(), false, false, true);
                    for (int parentEdge : parentEdges) {
                        if (isVisibleEdge(parentEdge) && parentEdge != incomingEdge) {
                            hideNode = false;
                            break;
                        }
                    }

                    if (hideNode) {
                        nodesToIterateNextLevel.add(childNode);
                        removeNodeFromSlimSet(childNode);
                        hideNode(childNode);
                    }
                }
            }
            nodesToIterate.clear();
            nodesToIterate.addAll(nodesToIterateNextLevel);
            nodesToIterateNextLevel.clear();
        }

		//updateViewStatistics();
	}

    /**View manipulation method which removes a node and all of its the descendant nodes from the view
     * The nodes is then marked as 'pruned', and is not displayed when a parent node is expanded unless a 'fullExpand'
     * is specified. (added by Laetitia Morrison, Jan 08)
	 * @param snode the network node to be removed from the view, along with its descendants
	 */
	public void pruneNode(Node snode) {
        collapseNode(snode);
        removeNodeFromSlimSet(snode);
        hideNode(snode);

        // hide all edges to parent nodes
        int[] outgoingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, false, true);
        for (int outgoingEdge: outgoingEdges) {
            hideEdge(outgoingEdge);
        }
        prunedNodes.add(snode);
    }
	

	
	/**View manipulation method which expands a nodes descendants so that they are visible in the network view
	 * The depth to which descendants will be made visible depends on what settings the user has selected.
	 * The node views of the descendants are arranged in a heirarchical manner below their parents.
	 * @param snode the network node who's descendants are to be made visible.
     * @param fullExpand true if pruned nodes should be displayed on expand, false otherwise
     *
     * Restructured by Laetitia Morrison, Jan 08
     * Combined two expansion modes into one method
	 */
	public void expandNode(Node snode, boolean fullExpand) {
		if (useFiniteExpansionDepth) {
			expandNodeToDepth(snode, nodeExpansionDepth, fullExpand);
		}
		else {
            expandNodeToDepth(snode, Integer.MAX_VALUE, fullExpand);
        }
	}
	
	/**Refined version of expandNode() which expands all descendants to a specified depth
	 * @param snode the node to expand
	 * @param depth the depth to which the DAG should be expanded.  If depth=Integer.MAX_VALUE, then expansion depth is unlimited.
     * @param fullExpand true if pruned nodes should be displayed on expand, false otherwise
     *
     * Restructured by Laetitia Morrison, Jan 08
     * Modified from recursive implementation to iterative to improve speed of execution, and combined two
     * expansion modes into one method.  
	 */
	public void expandNodeToDepth(Node snode,int depth, boolean fullExpand) {
		if (depth <=0) return;

        Set<Node> nodesToIterate = new HashSet<Node>();
        Set<Node> nodesToIterateNextLevel = new HashSet<Node>();
        int curDepth = depth;
        
        // Determine which children nodes will be shown on 'expand' (all children or just those with associated genes)
        boolean expandNodesWithGenes = GOSlimmerGUIViewSettings.expandNodesWithGenes;

        // Data structures for expand layout
        List<NodeView> childNodeViews = new ArrayList<NodeView>();
        HashMap<NodeView, Dimension> nodeSizes = new HashMap<NodeView, Dimension>();
        
        int directGenes;
        int inferredGenes;

        FontMetrics metrics;
        Component component = networkView.getComponent();

        nodesToIterate.add(snode);  // start with the node to expand

        while (!nodesToIterate.isEmpty() && curDepth > 0) {

            for (Node node : nodesToIterate) {

                double maxNodeHeight = 0;
                childNodeViews.clear();
                nodeSizes.clear();
                double maxXAlreadyVisible = Double.MIN_VALUE;

                // get incoming edges (from children nodes)
                int[] incomingEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
                for (int incomingEdge : incomingEdges) {

                    // Get child node, and determine whether this child should be shown
                    Node childNode = network.getEdge(incomingEdge).getSource();

                    // If node has been pruned, display it only if fullExpand is true (in which case, remove it from the pruned list)
                    if (prunedNodes.contains(childNode)) {
                        if (fullExpand) {
                            prunedNodes.remove(childNode);
                        } else {
                            continue;
                        }
                    }

                    /* If genes have been annotated and 'expand nodes with genes associated only' checkbox is checked
                      * then display/expand child only if it has at least one associated gene (direct or inferred).
                      */
                    if (nodeAtt.hasAttribute(childNode.getIdentifier(), GOSlimmer.directlyAnnotatedGeneNumberAttributeName)) {
                        directGenes = nodeAtt.getIntegerAttribute(childNode.getIdentifier(), GOSlimmer.directlyAnnotatedGeneNumberAttributeName);
                        inferredGenes = nodeAtt.getIntegerAttribute(childNode.getIdentifier(), GOSlimmer.inferredAnnotatedGeneNumberAttributeName);

                        if (expandNodesWithGenes && ((directGenes + inferredGenes) == 0)) {
                            continue;
                        }
                    }

                    showEdge(incomingEdge);
                    nodesToIterateNextLevel.add(childNode);

                    // get corresponding node view
                    NodeView childNodeV = networkView.getNodeView(childNode.getRootGraphIndex());

                    // get width/height of label
                    metrics = component.getFontMetrics(childNodeV.getLabel().getFont());
                    String labelText = childNodeV.getLabel().getText();
                    String[] lines = labelText.split("\n");

                    // calculate width of widest line in the label
                    int labelWidth = 0;
                    for (String line : lines) {
                        int length = metrics.stringWidth(line);
                        if (length > labelWidth) {
                            labelWidth = length;
                        }
                    }

                    // get node width (maximum of label width and node width)
                    double totalWidth = Math.max(labelWidth, childNodeV.getWidth());

                    // calculate height of label
                    int labelHeight = metrics.getHeight() * lines.length;

                    // get node height (maximum of label height and node height)
                    double totalHeight = Math.max(labelHeight, childNodeV.getHeight());

                    if (!isVisibleNode(childNode)) {

                        // if node isn't already visible, show the node, and add it to the list of nodes to be laid out.
                        // Keep track of the maximum node height

                        showNode(childNode);

                        maxNodeHeight = Math.max(maxNodeHeight, totalHeight);

                        childNodeViews.add(childNodeV);
                        Dimension d = new Dimension();
                        d.setSize(totalWidth, totalHeight);
                        nodeSizes.put(childNodeV, d);
                    }
                    else {
                        // child is already visible - figure out if its right most edge is past the current max value

                        if (childNodeV.getXPosition() + totalWidth/2 > maxXAlreadyVisible) {
                            maxXAlreadyVisible = childNodeV.getXPosition() + totalWidth/2;
                        }
                    }
                }

                layoutChildren(node, childNodeViews, nodeSizes, maxNodeHeight, maxXAlreadyVisible);


            }

            nodesToIterate.clear();
            nodesToIterate.addAll(nodesToIterateNextLevel);
            nodesToIterateNextLevel.clear();
            if (curDepth != Integer.MAX_VALUE) {
                curDepth = curDepth -1;
            }
        }

    }

    /**
     * Method to layout the children nodes of a node. Nodes will be laid out below the parent node, and staggered in
     * groups of 5 nodes.  In addition, they will be laid to the right of 'startX' if a value is given.
     * If startX = Double.MIN_VALUE, then the nodes will be centered horizontally under the parent node.
     * @param parent the parent node
     * @param childNodeViews a list of NodeView objects for the children that are to be laid out
     * @param nodeSizes a HashMap containing the nodeViews and their dimensions (encompassing both the node and its label)
     * @param maxNodeHeight the maximum node height of the nodes to be laid out
     * @param startX a parameter which gives the user control over the horizontal location of the children nodes.  The nodes will be placed to the right of startX.  If startX=Double.MIN_VALUE, then there is no restriction, and the nodes will be centered under the parent node.
	 */
    public void layoutChildren(Node parent, List<NodeView> childNodeViews, HashMap<NodeView, Dimension> nodeSizes, double maxNodeHeight, double startX) {

        // Get coordinates of parent node
        NodeView pView = networkView.getNodeView(parent);
        double x = pView.getXPosition();
        double y = pView.getYPosition();
        double h = pView.getHeight();

        double nodeSpacingX = 2.0;
        double nodeSpacingY = 5.0;
        double baseY = y + h / 2 + maxNodeHeight / 2 + nodeSpacingY;
        baseY = Math.max(baseY, y + 50);

        double lastCx = 0.0;
        double lastCw = 0.0;
        double lastCy = baseY;
        double lastCh = 0.0;
        double startLine = 0.0;
        double maxRightEdge = 0.0;

        HashMap<NodeView, Dimension> positions = new HashMap<NodeView, Dimension>(); // store the hierarchical positions of the nodes

        //now iterate through the childnodeviews, position them hierarchically and store the calculated positions
        for (int i = 0; i < childNodeViews.size(); i++) {
            NodeView childNodeV = childNodeViews.get(i);
            double cy, cx;
            double cw = nodeSizes.get(childNodeV).getWidth();
            double ch = nodeSizes.get(childNodeV).getHeight();

            if (i % 5 == 0) { // first node of every group
                cy = baseY + ch / 2;
                cx = startLine + lastCw / 2 + cw / 2;
            } else { // remaining nodes of a group
                cy = lastCy + lastCh / 2 + nodeSpacingY + ch / 2;
                cx = lastCx + lastCw / 2 + nodeSpacingX + cw / 2;
            }

            // Keep track of right most edge to be used in centering nodes under parent node (if necessary)
            double rightEdge = cx + cw / 2;
            if (rightEdge > maxRightEdge) {
                maxRightEdge = rightEdge;
            }

            // Store the hierarchical node positions
            Dimension pos = new Dimension();
            pos.setSize(cx, cy);
            positions.put(childNodeV, pos);

            lastCw = cw;
            lastCx = cx;
            lastCh = ch;
            lastCy = cy;

            // Each group will overlap to start at the same 'x' position as the third node in the previous group, so store
            // coordinate if necessary
            if (((i % 5) + 1) % 3 == 0) {
                startLine = lastCx;
            }
        }

        // Position nodes so that they are below the parent node, and either to the right of 'startX' or centered under the parent node, as necessary
        double totalChildWidth = maxRightEdge;
        double startPos;
        if (startX == Double.MIN_VALUE) {
            startPos = x - totalChildWidth/2;
        }
        else {
            startPos = startX + nodeSpacingX;
        }

        // Position the nodes
        for (NodeView childV : positions.keySet()) {
            Dimension pos = positions.get(childV);
            childV.setXPosition(pos.getWidth() + startPos);
            childV.setYPosition(pos.getHeight());
        }
    }
	
//	/**
//	 * @param snode the node to expand
//	 * @param depth the depth to which the DAG should be expanded
//	 */
//	public Collection<Node> expandNodeToDepthAndReturnDAGNodes(Node snode,int depth) {
//		List<Node> l= new ArrayList<Node>();
//		l.add(snode);
//		if (depth <=0) {
//			l.add(snode);
//			return l;
//		}
//		networkView.showGraphObject(networkView.getNodeView(snode));
//		
//		//retrieve the incoming edges, such that we can expand them
//		int[] incomingEdges = network.getAdjacentEdgeIndicesArray(snode.getRootGraphIndex(), false, true, false);
//		for (int incomingEdge:incomingEdges) {
//			EdgeView ev = networkView.getEdgeView(incomingEdge);
//			networkView.showGraphObject(ev);
//			l.addAll(expandNodeToDepthAndReturnDAGNodes(ev.getEdge().getSource(),depth -1));
//		}
//		return l;
//	}
	
	private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	
	/**Adds a specified node to the GO slim set, updating statistics accordingly
	 * @param node node to be added to slim set
	 */
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
	
	/**Removes a specified node to the GO slim set, updating statistics accordingly
	 * @param node node to be removed to slim set
     * @param taskMonitor TaskMonitor to be updated with task progress (if applicable)
	 */
	public void removeNodeFromSlimSet(Node node, TaskMonitor taskMonitor) {

        if (statBean.getSlimGoNodes().contains(node)) {
            //set the 'selected for slim set' attribute to false
		    nodeAtt.setAttribute(node.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName, false);
		    //TODO update coverage statistics
		    statBean.removeFromSlimSet(node, taskMonitor);
		    updateViewStatistics();
        }
    }
	
   /**Removes a specified node to the GO slim set, updating statistics accordingly
	 * @param node node to be removed to slim set
	 */
    public void removeNodeFromSlimSet(Node node) {
        removeNodeFromSlimSet(node, null);
    }

    DecimalFormat formatter = new DecimalFormat("00.00%");
	/**
	 * Updates the view of the coverage statistics to agree with the datamodel.  This is intended to be called whenever the 
	 * statistics are updated.
	 */
	private void updateViewStatistics() {
		
		//depending on whether displayUserGeneCoverageStatistics has been set or not, either show the full coverage information, or only for the user specified genes.
		double inferredCoverage = displayUserGeneCoverageStatistics?statBean.fractionInferredCoveredUserGenes():statBean.fractionInferredCovered();
		double directCoverage = displayUserGeneCoverageStatistics?statBean.fractionDirectlyCoveredUserGenes():statBean.fractionDirectlyCovered();
		
		//TODO consider revising this condition, since it might hide the face that the coverageStatisticViewLabel hasn't been initialized
		if (this.inferredCoverageStatisticViewLabel!=null) this.inferredCoverageStatisticViewLabel.setText("Inferred Coverage: " + formatter.format(inferredCoverage));
		if (this.directCoverageStatisticViewLabel!=null) this.directCoverageStatisticViewLabel.setText("Direct Coverage: " + formatter.format(directCoverage));
        if (this.selectedGOTermsPanel!=null) {
            String[] listGOTerms = statBean.getListSelectedGONodes();
            this.selectedGOTermsPanel.setList(listGOTerms);
        }
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

    public SelectedGOTermsPanel getSelectedGOTermsPanel() {
        return selectedGOTermsPanel;
    }

    public void setSelectedGOTermsPanel(SelectedGOTermsPanel selectedGOTermsPanel) {
        this.selectedGOTermsPanel = selectedGOTermsPanel;
    }

    
    /**
	 * Removes the coverage attributes from the nodes within this network, if they are defined.  Useful when one wants 
	 * to regenerate the statistics, for example when a new annotation file is loaded
	 */
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

	/**Updates the coverage data on the network based on a new set of gene annotation data, updating statistics accordingly 
	 * @param goIdToGeneIdMap the map which defines which genes are covered by which go term, to be used for setting coverage attributes
	 */
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
	
	
	private void assignCoverageAttributesToNetworks(Map<String,List<String>> goIdToAttValMap,String directCoverageAttributeName,String inferredCoverageAttributeName) {
		//scratch that, instead iterate through the nodes of the GO DAG graph, and attach annotated gene list attributes accordingly
		Iterator<Node> nodeI = network.nodesIterator();
		while (nodeI.hasNext()) {
			Node node = nodeI.next();
			String nodeGoId = node.getIdentifier();
			//get the genes which this go term annotates, according to the gene association file
			List<String> nodeAnnotatedGeneIds = goIdToAttValMap.get(nodeGoId);
			
			//attach the gene id list as an attribute, if it exists 
			if (nodeAnnotatedGeneIds!=null && nodeAnnotatedGeneIds.size()>0) {
				nodeAtt.setListAttribute(nodeGoId, directCoverageAttributeName, nodeAnnotatedGeneIds);
			}
			
		}
		//now annotated each node with the genes it annotated indirectly, through inference, from the genes which it's children inference
		nodeI = network.nodesIterator();
		
		while(nodeI.hasNext()) {
			Node node = nodeI.next();
			//see if the inferred coverred genes list attribute has already been calculated and set
			List<String> inferredCoveredGenesL = nodeAtt.getListAttribute(node.getIdentifier(), inferredCoverageAttributeName);
			if (inferredCoveredGenesL ==null || inferredCoveredGenesL.size()==0) {
				//inferred coverred genes list has not already been calculated, so calculate and set it
				Set<String> inferredCoveredGenesS = GOSlimmerUtil.getGenesCoveredByChildren(node, network,directCoverageAttributeName,inferredCoverageAttributeName);
				nodeAtt.setListAttribute(node.getIdentifier(), inferredCoverageAttributeName, GOSlimmerUtil.setToList(inferredCoveredGenesS));
			}
			
		}
		
	}

    // New method that also assigns as an attribute the number of annotated genes covered by this node
    private void assignCoverageAttributesToNetworks(Map<String, List<String>> goIdToAttValMap, String directCoverageAttributeName, String inferredCoverageAttributeName, String directNumberAttributeName, String inferredNumberAttributeName) {
        //scratch that, instead iterate through the nodes of the GO DAG graph, and attach annotated gene list attributes accordingly
        Iterator<Node> nodeI = network.nodesIterator();
        while (nodeI.hasNext()) {
            Node node = nodeI.next();
            String nodeGoId = node.getIdentifier();
            //get the genes which this go term annotates, according to the gene association file
            List<String> nodeAnnotatedGeneIds = goIdToAttValMap.get(nodeGoId);

            //if gene id list exists, attach list and list size as attributes
            if (nodeAnnotatedGeneIds != null && nodeAnnotatedGeneIds.size() > 0) {
                nodeAtt.setListAttribute(nodeGoId, directCoverageAttributeName, nodeAnnotatedGeneIds);
                // if directNumberAttributeName is not null, attach list size as attribute.  Otherwise, do nothing.
                if (!(directNumberAttributeName == null || directNumberAttributeName.equals(""))) {
                    nodeAtt.setAttribute(nodeGoId, directNumberAttributeName, nodeAnnotatedGeneIds.size());
                }

            }
            // if gene list doesn't exist or has size 0 and directNumberAttributeName is not null, attach 0 as list size
            else if (!(directNumberAttributeName == null || directNumberAttributeName.equals(""))) {
                nodeAtt.setAttribute(nodeGoId, directNumberAttributeName, 0);
            }

        }
        //now annotated each node with the genes it annotated indirectly, through inference, from the genes which it's children inference
        nodeI = network.nodesIterator();

        while (nodeI.hasNext()) {
            Node node = nodeI.next();
            String nodeGoId = node.getIdentifier();
            //see if the inferred coverred genes list attribute has already been calculated and set
            List<String> inferredCoveredGenesL = nodeAtt.getListAttribute(node.getIdentifier(), inferredCoverageAttributeName);

            //if inferred covered genes list has not already been calculated, calculate and set it
            if (inferredCoveredGenesL == null || inferredCoveredGenesL.size() == 0) {
                Set<String> inferredCoveredGenesS = GOSlimmerUtil.getGenesCoveredByChildren(node, network, directCoverageAttributeName, inferredCoverageAttributeName);
                nodeAtt.setListAttribute(nodeGoId, inferredCoverageAttributeName, GOSlimmerUtil.setToList(inferredCoveredGenesS));

                // if inferredNumberAttributeName is not null, attach list size as attribute.  Otherwise, do nothing.
                if (!(inferredNumberAttributeName == null || inferredNumberAttributeName.equals(""))) {
                    nodeAtt.setAttribute(nodeGoId, inferredNumberAttributeName, inferredCoveredGenesS.size());
                }
            }
            // Inferred gene list was already calculated.  If inferredNumberAttributeName is not null, check to see
            // if list size was already attached as attribute.  If not, get size and attach.
            else if (!(inferredNumberAttributeName == null || inferredNumberAttributeName.equals(""))) {
                if (!nodeAtt.hasAttribute(nodeGoId, inferredNumberAttributeName)) {
                    nodeAtt.setAttribute(nodeGoId, inferredNumberAttributeName, inferredCoveredGenesL.size());
                }
            }

        }

    }


	/**This method resets the coverage statistics in the model layer, and recalculates them based on which nodes have been selected 
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

	
	
	/**Attaches information about the given user gene set to the go terms within the controlled network.  
	 * @param geneIds
	 */
	public void attachInferredAnnotatedUserGenesToTerms(Collection<String> geneIds) {
		Iterator<Node> nodeI = this.network.nodesIterator();
		//TODO consider revising this so that instead of traversing the tree, 
		//I simply iterate through the nodes and determine the overlap  
		while(nodeI.hasNext()) {
			Node node = nodeI.next();
			//see if the inferred coverred genes list attribute has already been calculated and set
			List<String> inferredCoveredGenesL = nodeAtt.getListAttribute(node.getIdentifier(), GOSlimmer.inferredAnnotatedUserGenesAttributeName);
			if (inferredCoveredGenesL ==null || inferredCoveredGenesL.size()==0) {
				//inferred coverred genes list has not already been calculated, so calculate and set it
				Set<String> inferredCoveredGenesS = GOSlimmerUtil.getGenesCoveredByChildren(node, this.network,true);
				nodeAtt.setListAttribute(node.getIdentifier(), GOSlimmer.inferredAnnotatedUserGenesAttributeName, GOSlimmerUtil.setToList(inferredCoveredGenesS));
			}
		}
		
	}


	/**Attaches information about the given user gene set to the go terms within the controlled network.  
	 * @param geneIds
	 */
	public void attachDirectlyAnnotatedUserGenesToTerms(Collection<String> geneIds) {
		
		Iterator<Node> nodeI = this.network.nodesIterator();
		while(nodeI.hasNext()) {
			List<String> matchingIds;
			Node node = nodeI.next();
			
			//TODO make this more efficent by eliminating the addAll commands and the retainalls as well.
			//grab the list of directly annotated genes, and performs matches;  attach if matched
			List<String> directlyCoveredGenesAndSynonyms = GOSlimmerUtil.getDirectlyCoveredGenes(node);
			
			//add the list of gene synonyms, so that we can compare against them as well
			directlyCoveredGenesAndSynonyms.addAll(GOSlimmerUtil.getDirectlyCoveredGeneSynonyms(node));
			//TODO determine which of the following implementation is more efficient;  the iteration based one or the retailAll based one
//			matchingIds = new ArrayList<String>();
//			//iterate through the lists based on which one is shorter
//			Collection<String> longer,shorter;
//			if (directlyCoveredGenes.size()>geneIds.size())  {
//				longer = directlyCoveredGenes;
//				shorter = geneIds;
//			}
//			else {
//				longer = geneIds;
//				shorter = directlyCoveredGenes;
//			}
//			//if id is in both sets, add to the list of matches
//			//TODO consider revising and  using a collection method for finding set overlap
//			for(String geneId:shorter) {
//				if (longer.contains(geneId)) {
//					matchingIds.add(geneId);
//				}
//			}
			matchingIds = new ArrayList<String>(geneIds); //make a copy of the collection, since we don't want to alter the original
			matchingIds.retainAll(directlyCoveredGenesAndSynonyms);
			
			nodeAtt.setListAttribute(node.getIdentifier(), GOSlimmer.directlyAnnotatedUserGenesAttributeName, matchingIds);
		}

		
	}

	/**Applies the given user gene set to the network
	 * @param userGeneSet
	 * @return the subset of the input set which were successfully matched to GO terms
	 */
	public Collection<String> applyUserGeneSet(Collection<String> userGeneSet) {
		session.setUserGeneSet(userGeneSet);
		attachDirectlyAnnotatedUserGenesToTerms(userGeneSet);
		attachInferredAnnotatedUserGenesToTerms(userGeneSet);
		//determine the number of genes which were successfully matched, and which ones failed to be matched
		Collection<String> matchedIds = new HashSet<String>();
		matchedIds.addAll(GOSlimmerUtil.getGenesCoveredByGoNode(GOSlimmerUtil.getRootNode(network), true,true));
//		this.updateViewStatistics();

//		difference(geneIds,matchedIds);
//		getStatBean().setupUserGeneStatistics()
		return matchedIds;
	}

	public void setupUserGeneStatistics(int newUserGeneCount) {
		this.statBean.setupUserGeneStatistics(newUserGeneCount);
		this.updateViewStatistics();
		
	}

	public void assignGeneSynonymCoverageAttributesToNetworks(Map<String, List<String>> goIdToGeneSynonymMap) {
		//this.assignCoverageAttributesToNetworks(goIdToGeneSynonymMap, GOSlimmer.directlyAnnotatedGenesSynonymAttributeName, GOSlimmer.inferredAnnotatedGenesSynonymAttributeName);
        this.assignCoverageAttributesToNetworks(goIdToGeneSynonymMap, GOSlimmer.directlyAnnotatedGenesSynonymAttributeName, GOSlimmer.inferredAnnotatedGenesSynonymAttributeName, null, null);
    }

	public void assignGeneIdCoverageAttributesToNetworks(Map<String, List<String>> goIdToGeneIdMap) {
		//this.assignCoverageAttributesToNetworks(goIdToGeneIdMap, GOSlimmer.directlyAnnotatedGenesAttributeName, GOSlimmer.inferredAnnotatedGenesAttributeName);
        this.assignCoverageAttributesToNetworks(goIdToGeneIdMap, GOSlimmer.directlyAnnotatedGenesAttributeName, GOSlimmer.inferredAnnotatedGenesAttributeName, GOSlimmer.directlyAnnotatedGeneNumberAttributeName, GOSlimmer.inferredAnnotatedGeneNumberAttributeName);        

    }

	public GONamespace getNamespace() {
		return namespace;
	}

	/**Appends the IDs of the selected slim nodes for this node to the given file as a newline delimited list 
	 * @param exportFile the file to append the node names to
	 * @return true if the operation was successful, false otherwise
	 * @throws IOException if there were problems writing to the file
	 */
	public boolean appendSlimSetList(File exportFile) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(exportFile,true));
		//TODO get slim set from stat bean
		for(Node slimNode:this.statBean.getSlimGoNodes()) {
			out.println(slimNode.getIdentifier());
		}
		out.close();
		return true;
	}

    public boolean isVisibleNode(Node node) {
        return visibleNodes.contains(node);
    }

    public boolean isVisibleEdge(int edge) {
        return visibleEdges.contains(edge);
    }

    public void hideNode(Node node) {
    	NodeView nodeView = networkView.getNodeView(node);
		networkView.hideGraphObject(nodeView);
        visibleNodes.remove(node);
    }

	public void showNode(Node node) {
        NodeView nodeView = networkView.getNodeView(node);
        networkView.showGraphObject(nodeView);
        visibleNodes.add(node);
    }

    public void hideEdge(int edge) {
        EdgeView edgeView = networkView.getEdgeView(edge);
        networkView.hideGraphObject(edgeView);
        visibleEdges.remove(edge);
    }

    public void showEdge(int edge) {
        EdgeView edgeView = networkView.getEdgeView(edge);
        networkView.showGraphObject(edgeView);
        visibleEdges.add(edge);
    }
	
}
