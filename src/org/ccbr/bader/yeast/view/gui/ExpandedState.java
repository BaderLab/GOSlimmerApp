package org.ccbr.bader.yeast.view.gui;

import giny.model.Node;
import giny.view.NodeView;
import giny.view.EdgeView;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.HashMap;

import ding.view.DGraphView;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import cytoscape.Cytoscape;

/**
 * Records the expanded/collapsed state of a node and its descendant.  Used for undo by ExpandCollapseEdit.
 */
public class ExpandedState {

	protected Map<Node, Point2D.Double> points;             // node coordinates
    protected Map<Node, Boolean> visibleNodes;              // node visibility (true/false)
    protected Map<Integer, Boolean> visibleEdges;           // edge visibility (true/false)
    protected Map<Node, Boolean> selected;                  // node selected state (true/false)

    protected DGraphView view;
    protected GOSlimmerController controller;
    protected Node node;                                    // node being expanded/collapsed

    /**
	 * @param controller The controller for the network we're recording.
     * @param node The node being expanded/collapsed
	 */
	public ExpandedState(GOSlimmerController controller, Node node) {
        this.controller = controller;
        this.node = node;

        view = (DGraphView) controller.getNetworkView();

        points = new HashMap<Node, Point2D.Double>();
        visibleNodes = new HashMap<Node, Boolean>();
        visibleEdges = new HashMap<Integer, Boolean>();
        selected = new HashMap<Node, Boolean>();

		points.clear();
        visibleNodes.clear();
        visibleEdges.clear();
        selected.clear();

        // record the states of the descendants
        saveInfo(node);
    }

    /**
	 * Checks if the ExpandedState is the same. If node and view are the same, it begins checking positions,
     * visibilities and selected states of descendants.
	 * @param o The object to test for equality.
	 */
	public boolean equals(Object o) {
		if ( !(o instanceof ExpandedState) ) {
			return false;
		}

		ExpandedState es = (ExpandedState)o;

        if (!node.equals(es.node)) {
            return false;
        }
        if ( view != es.view ) {
			return false;
		}

        // check states of descendants
        return checkInfo(es, node);

   	}


	/**
	 * Applies the recorded states to the descendant nodes.
	 */
	public void apply() {

        // apply recorded states to the descendant nodes
        applyInfo(node);

        // update the view
        Cytoscape.getCurrentNetworkView().redrawGraph(false,false);
    }

    /**
     * Recursive method to record the states of the descendants of a node
     * @param node node for which to record the states of the descendants
     */
    private void saveInfo(Node node) {

        /* Loop through the incoming edges (from child nodes), and save the visibility of the edges, as well as the
         * coordinates, visibility and selected state of the source of the edge (ie. the child node).
         * Then, recursively call the method on the child node.
         */
        int[] incomingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
        for (int incomingEdge:incomingEdges) {
            Boolean visibleEdge = controller.isVisibleEdge(incomingEdge);
            visibleEdges.put(incomingEdge, visibleEdge);

            EdgeView ev = view.getEdgeView(incomingEdge);
            Node childNode = ev.getEdge().getSource();
            NodeView cnv = view.getNodeView(childNode);

            points.put(childNode, new Point2D.Double(cnv.getXPosition(), cnv.getYPosition()));
            visibleNodes.put(childNode, controller.isVisibleNode(childNode));
            selected.put(childNode, controller.getStatBean().getSlimGoNodes().contains(childNode));

            saveInfo(childNode);
        }

        /* Loop through the outgoing edges (to parent nodes), and save the visibility of the edges. */
        int [] outgoingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, false, true);
        for (int outgoingEdge: outgoingEdges) {
            Boolean visibleEdge = controller.isVisibleEdge(outgoingEdge);
            visibleEdges.put(outgoingEdge, visibleEdge);
        }
    }

    /**
     * Recursive method to compare the states of the descendants with another ExpandedState object.
     * @param es ExpandedState object with which to compare the states of the descendants.
     * @param node node for which to compare the states of the descendants
     */
    private boolean checkInfo(ExpandedState es, Node node) {

        /* Loop through the incoming edges (from child nodes) and compare the visibility of the edges, as well as the
         * coordinates, visibility and selected state of the source of the edge (ie. the child Node).
         * Then, recursively call the method on the child node.
         */
        int[] incomingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
        for (int incomingEdge:incomingEdges) {
            EdgeView ev = view.getEdgeView(incomingEdge);
            Node childNode = ev.getEdge().getSource();

            if (!points.get(childNode).equals(es.points.get(childNode))) {
                return false;
            }
            if (!visibleEdges.get(incomingEdge).equals(es.visibleEdges.get(incomingEdge))) {
                return false;
            }
            if (!visibleNodes.get(childNode).equals(es.visibleNodes.get(childNode))) {
                return false;
            }
            if (!selected.get(childNode).equals(es.selected.get(childNode))) {
                return false;
            }
            if (!checkInfo(es, childNode)) {
                return false;
            }
        }

        /* Loop through the outgoing edges (to parent nodes), and compare the visibility of the edges. */
        int[] outgoingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, false, true);
        for (int outgoingEdge:outgoingEdges) {
            if (!visibleEdges.get(outgoingEdge).equals(es.visibleEdges.get(outgoingEdge))) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Recursive method to apply the recorded states to the descendants of a node.
     * @param node node for which to apply the recorded states to the descendants.
	 */
    private void applyInfo(Node node) {

        /* Loop through the incoming edges (from child nodes) and apply the recorded visibility to the edges, as well
         * as the recorded coordinates, visibility and selected state to the source of the edge (ie. the child node).
         * Then, recursively call the method on the child node.
         */
        int[] incomingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
        for (int incomingEdge:incomingEdges) {

            if (visibleEdges.get(incomingEdge)) {
                controller.showEdge(incomingEdge);
            }
            else {
                controller.hideEdge(incomingEdge);
            }

            EdgeView ev = view.getEdgeView(incomingEdge);
            Node childNode = ev.getEdge().getSource();
            NodeView cnv = view.getNodeView(childNode);

            if (visibleNodes.get(childNode)) {
                controller.showNode(childNode);
                Point2D.Double p = points.get(childNode);
                cnv.setXPosition(p.getX());
                cnv.setYPosition(p.getY());
            }
            else {
                controller.hideNode(childNode);
            }
            if (selected.get(childNode)) {
                if (!controller.getStatBean().getSlimGoNodes().contains(childNode)) {
                    controller.addNodeToSlimSet(childNode);
                }
            }
            else {
                if (controller.getStatBean().getSlimGoNodes().contains(childNode)) {
                    controller.removeNodeFromSlimSet(childNode);
                }
            }
            applyInfo(childNode);
        }

        /* Loop through the outgoing edges (to parent nodes), and apply the recorded visibility to the edges */
        int[] outgoingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, false, true);
        for (int outgoingEdge:outgoingEdges) {
            if (visibleEdges.get(outgoingEdge)) {
                controller.showEdge(outgoingEdge);
            }
            else {
                controller.hideEdge(outgoingEdge);
            }
        }
    }

}
