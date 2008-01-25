package org.ccbr.bader.yeast.view.gui;

import giny.model.Node;
import giny.model.Edge;
import giny.view.NodeView;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ding.view.DGraphView;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import cytoscape.Cytoscape;

/**
 * Records the expanded/collapsed state of the nodes in the network.  Used for undo by ExpandCollapseEdit.
 */
public class ExpandedState {

	protected Map<Node, Point2D.Double> points;             // node coordinates
    protected Map<Node, Boolean> visibleNodes;              // node visibility (true/false)
    protected Map<Integer, Boolean> visibleEdges;           // edge visibility (true/false)
    protected Map<Node, Boolean> selected;                  // node selected state (true/false)

    protected DGraphView view;
    protected GOSlimmerController controller;

    /**
	 * @param controller The controller for the network we're recording.
	 */
	public ExpandedState(GOSlimmerController controller) {
        this.controller = controller;

        view = (DGraphView) controller.getNetworkView();

        points = new HashMap<Node, Point2D.Double>();
        visibleNodes = new HashMap<Node, Boolean>();
        visibleEdges = new HashMap<Integer, Boolean>();
        selected = new HashMap<Node, Boolean>();

		points.clear();
        visibleNodes.clear();
        visibleEdges.clear();
        selected.clear();

        Set<Node> selectedNodes = controller.getStatBean().getSlimGoNodes();
        Iterator nodes_i = controller.getNetwork().nodesIterator();
        while (nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();

            NodeView nv = view.getNodeView(node);

            points.put(node, new Point2D.Double(nv.getXPosition(), nv.getYPosition()));
            visibleNodes.put(node, controller.isVisibleNode(node));
            selected.put(node, selectedNodes.contains(node));
        }

        Iterator edges_i = controller.getNetwork().edgesIterator();
        while (edges_i.hasNext()) {
            Edge edge = (Edge) edges_i.next();

            int edgeIndex = edge.getRootGraphIndex();

            visibleEdges.put(edgeIndex, controller.isVisibleEdge(edgeIndex));
        }
    }

    /**
	 * Checks if the ExpandedState is the same. If view is the same, it checks the positions, visibilities
     * and selected states of all the nodes.
	 * @param o The object to test for equality.
	 */
	public boolean equals(Object o) {
		if ( !(o instanceof ExpandedState) ) {
			return false;
		}

		ExpandedState es = (ExpandedState)o;

        if ( view != es.view ) {
			return false;
		}

        Iterator nodes_i = controller.getNetwork().nodesIterator();
        while (nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();

            if (!points.get(node).equals(es.points.get(node))) {
                return false;
            }
            if (!visibleNodes.get(node).equals(es.visibleNodes.get(node))) {
                return false;
            }
            if (!selected.get(node).equals(es.selected.get(node))) {
                return false;
            }
        }

        Iterator edges_i = controller.getNetwork().edgesIterator();
        while (edges_i.hasNext()) {
            Edge edge = (Edge) edges_i.next();

            int edgeIndex = edge.getRootGraphIndex();

            if (!visibleEdges.get(edgeIndex).equals(es.visibleEdges.get(edgeIndex))) {
                return false;
            }
        }

        return true;

   	}


	/**
	 * Applies the recorded states to the nodes.
	 */
	public void apply() {

        Set<Node> selectedNodes = controller.getStatBean().getSlimGoNodes();

        Iterator nodes_i = controller.getNetwork().nodesIterator();
        while (nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();
            NodeView nv = view.getNodeView(node);

            if (visibleNodes.get(node)) {
                controller.showNode(node);
                Point2D.Double p = points.get(node);
                nv.setXPosition(p.getX());
                nv.setYPosition(p.getY());
            }
            else {
                controller.hideNode(node);
            }

            if (selected.get(node)) {
                if (!selectedNodes.contains(node)) {
                    controller.addNodeToSlimSet(node);
                }
            }
            else {
                if (selectedNodes.contains(node)) {
                    controller.removeNodeFromSlimSet(node);
                }
            }
        }

        Iterator edges_i = controller.getNetwork().edgesIterator();
        while (edges_i.hasNext()) {
            Edge edge = (Edge) edges_i.next();

            int edgeIndex = edge.getRootGraphIndex();

            if (visibleEdges.get(edgeIndex)) {
                controller.showEdge(edgeIndex);
            }
            else {
                controller.hideEdge(edgeIndex);
            }
        }

        // update the view
        Cytoscape.getCurrentNetworkView().redrawGraph(false,false);
    }

}
