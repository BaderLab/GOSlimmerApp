package org.ccbr.bader.yeast.view.gui;

/**GUI Widget for automatically generating list of GO terms with the best gene coverage.
 * Allows the user to select a term and find it in the GO graph.
 *
 *
 * @author laetitiamorrison
 *
 */

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.view.CyNetworkView;

import giny.view.NodeView;
import giny.view.EdgeView;
import giny.model.Node;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.AutomaticGOSetGenerator.AutomaticGeneratorAlgorithm;
import org.ccbr.bader.yeast.GONamespace;
import org.ccbr.bader.yeast.GOSlimmerSession;
import org.ccbr.bader.yeast.view.gui.misc.JCollapsablePanel;
import org.ccbr.bader.yeast.view.gui.misc.JButtonMod;
import org.ccbr.bader.yeast.view.gui.misc.JLabelMod;

public class AutomaticGOSetGeneratorPanel extends JCollapsablePanel implements ActionListener, PropertyChangeListener {

    Map<GONamespace, GOSlimmerController> namespaceToController;
    private GOSlimmerSession session;
    private String[] coveringSetNames = {};
    private int defaultNumTerms = 10;               // default


    public AutomaticGOSetGeneratorPanel(GOSlimmerSession session) {
		super("Automatic GO Set Term Generator");
		this.namespaceToController = session.getNamespaceToController();
        this.session = session;

        initComponents();
    }

    private JFormattedTextField numTermsTextField;
	private JFormattedTextField getNumTermsTextField() {
		if (numTermsTextField == null) {
            numTermsTextField = new JFormattedTextField(defaultNumTerms);
            numTermsTextField.setEnabled(true);
			numTermsTextField.addActionListener(this);
			numTermsTextField.addPropertyChangeListener("value",this);
            numTermsTextField.setSize(100,10);
        }
		return numTermsTextField;
	}

    private JLabel numTermsLabel;
    private JLabel getNumTermsLabel() {
        if (numTermsLabel == null) {
            numTermsLabel = new JLabelMod("Number of terms to generate:");
        }
        return numTermsLabel;
    }

    private JButton generateCoveringTermsButton;
	private String generateCoveringTermsButtonText = "Find Best Covering Terms";
	private String generateCoveringTermsButtonToolTip = "Generate a list of the best covering terms for the remaining uncovered genes";
	private JButton getGenerateCoveringTermsButton() {
		if (generateCoveringTermsButton==null) {
			generateCoveringTermsButton = new JButtonMod(generateCoveringTermsButtonText);
			generateCoveringTermsButton.addActionListener(this);
			generateCoveringTermsButton.setToolTipText(generateCoveringTermsButtonToolTip);
        }
		return generateCoveringTermsButton;
	}

    private JScrollPane coveringSetScrollPane;
    private String coveringSetScrollPaneToolTip = "List of best covering terms.";
    private JScrollPane getCoveringSetScrollPane() {
        if (coveringSetScrollPane == null) {
            coveringSetScrollPane = new JScrollPane();
            coveringSetScrollPane.setToolTipText(coveringSetScrollPaneToolTip);

            // Create JList item for scroll pane
            JList coveringSetList = new JList(coveringSetNames);
            coveringSetList.setVisibleRowCount(4);
            coveringSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            coveringSetScrollPane.getViewport().setView(coveringSetList);
        }
        return coveringSetScrollPane;
    }

    private JButton findCoveringTermButton;
	private String findCoveringTermButtonText = "Find";
	private String findCoveringTermButtonToolTip = "Find selected covering term in GO diagram";
	private JButton getFindCoveringTermButton() {
		if (findCoveringTermButton==null) {
			findCoveringTermButton = new JButtonMod(findCoveringTermButtonText);
			findCoveringTermButton.addActionListener(this);
			findCoveringTermButton.setToolTipText(findCoveringTermButtonToolTip);
        }
		return findCoveringTermButton;
	}

	/**
	 * Initializes the widget's subcomponents and layout
	 */
	private void initComponents() {

        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1;
        c.gridwidth = 3;
        c.gridx=0;
		c.gridy=0;
        this.add(getGenerateCoveringTermsButton(),c);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        this.add(getNumTermsLabel(), c);

        c.gridwidth = 2;
        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 1;
        this.add(getNumTermsTextField(), c);

        c.anchor = GridBagConstraints.LINE_END;
        c.weightx = 1;
        c.gridwidth = 1;
        c.gridx=2;
        c.gridy=2;
        this.add(getFindCoveringTermButton(),c);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = .9;
        c.gridx=0;
        c.gridy=2;
        this.add(getCoveringSetScrollPane(),c);

    }

    public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
        if (src instanceof JButton) {
            JButton bsrc = (JButton) src;
            if (bsrc.equals(generateCoveringTermsButton)) {  // Generate list of covering terms
                System.out.println("generating list of GO terms with highest gene coverage...");
                AutomaticGeneratorAlgorithm generator = new AutomaticGeneratorAlgorithm(session);
                CyNetwork network = Cytoscape.getCurrentNetwork();

                GOSlimmerController controller = null;
                GONamespace namespace = null;

                // Find controller for this network
                for (GONamespace name_sp: namespaceToController.keySet()) {
                    GOSlimmerController control = namespaceToController.get(name_sp);
                    if (control.getNetwork().equals(network)) {
                        namespace = name_sp;
                        controller = control;
                        break;
                    }
                }

                if (namespace!=null) {
                    System.out.println("network is: " + namespace.getName());

                    int numTopTerms = (Integer) numTermsTextField.getValue();

                    // Get list of Go terms with best gene coverage (based on current set of selected go terms)
                    String[] topTermsString = generator.getTopTermsStrings(controller.getStatBean().getSlimGoNodes(), numTopTerms);

                    // Display list of GO terms in scroll pane
                    JList tempList = (JList) coveringSetScrollPane.getViewport().getView();

                    tempList.setListData(topTermsString);

                }

            }
            else if (bsrc.equals(findCoveringTermButton)) {
                                
                JList list = (JList) coveringSetScrollPane.getViewport().getView();
                if (list.getSelectedIndex()!= -1) {
                    String fullName = (String) list.getSelectedValue();

                    /* Get GOId of node from string in list */
                    int pStartIndex = fullName.lastIndexOf("(");
                    int pEndIndex = fullName.indexOf(")",pStartIndex);
                    String goId = fullName.substring(pStartIndex+1, pEndIndex);

                    System.out.println("Finding GO term " + goId + "......");

                    /* Get network and network view associated with this panel/list */
                    CyNetwork network = Cytoscape.getCurrentNetwork();
                    CyNetworkView view = Cytoscape.getCurrentNetworkView();

                    /* Find controller for current network */
                    GOSlimmerController controller = null;
                    for (GOSlimmerController control : namespaceToController.values()) {
                        CyNetwork curNetwork = control.getNetwork();
                        if (curNetwork.equals(network)) {
                            controller = control;
                        } 
                    }

                    /* Iterate through nodes in network, and find node corresponding to goId (if it exists) */
                    Iterator nodes_i = network.nodesIterator();
                    Node goNode = null;
                    while(nodes_i.hasNext()) {
                        Node node = (Node) nodes_i.next();
                        if (node.toString().equals(goId)) {
                            goNode = node;
                            break;
                        }
                    }
                    if (goNode==null) {
                        System.out.println("this node is not in this network");
                    }
                    else {
                        //System.out.println("found the node: " + goNode);
                        network.unselectAllNodes();

                        if (controller != null && controller.isVisibleNode(goNode)) {
                            // Node is currently visible, so select it in cytoscape
                            network.setSelectedNodeState(goNode, true);

                            // Update the view
                            view.updateView();

                        } else {
                            //System.out.println("have to go into network to find node...");
                            if (controller!=null) {

                                // Save expand/collapse state for all nodes in graph
                                ExpandCollapseEdit undoableEdit = new ExpandCollapseEdit(controller, "Find Node");

                                // Find and display the node in the graph, and select it in cytoscape
                                displayNode(goNode, controller, view);
                                network.setSelectedNodeState(goNode, true);

                                // Post the 'undo' command to the undo menu
                                undoableEdit.post();

                                // update the view
                                view.redrawGraph(false,false);
                            }

                        }

                    }

                }
            }



        }
    }

    public void propertyChange(PropertyChangeEvent event) {
		Object src = event.getSource();
        if (src instanceof JTextField) {
			if (src == numTermsTextField) {
                try {
                    Integer.parseInt(numTermsTextField.getText());
                }
                catch (NumberFormatException e) {
                    numTermsTextField.setText(String.valueOf(defaultNumTerms));
                }
            }
        }
    }

    /**
     * Displays a node in the network by finding all the 'parentage lines' from this node to the first visible parent
     * in the graph, and displays these lines, laying out the nodes as necessary.
     * @param node the GO term node to be displayed in the network
     * @param controller the GOSlimmer controller for this network
     * @param view the network view for this network
     */
    private void displayNode(Node node, GOSlimmerController controller, CyNetworkView view) {

        HashMap<Node, Set<Node>> parentChildList = new HashMap<Node,Set<Node>>();

        // Get all 'parentage lines' from 'node' to the first visible parent in the graph
        Set<List<Node>> allLines = getParentLines(node, controller, view, parentChildList);

        // Loop through each 'parentage line', and display it as necessary
        for (List<Node> line : allLines) {

            // The first node in the list is the already visible parent, so we loop through the other nodes, and
            // display them as necessary (if they haven't already been displayed by another 'parentage line')
            for (int i = 1; i < line.size(); i++) {

                Node childNode = line.get(i);
                Node parentNode = line.get(i - 1);
                if (!controller.isVisibleNode(childNode)) {  // not visible, so must display it

                    double maxX = Double.NEGATIVE_INFINITY;

                    if (i == 1) {
                        // child of the top-most parent in the line, so check and see if any other children are
                        // visible, and layout as necessary

                        // Loop through the incoming edges (from children nodes) of the top-most node in the line
                        // and determine which (if any) of the children are visible
                        Set<Node> visibleChildren = new HashSet<Node>();

                        // Get incoming edges (from children)
                        int[] incomingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(parentNode.getRootGraphIndex(), false, true, false);
                        for (int incomingEdge : incomingEdges) {
                            EdgeView ev = view.getEdgeView(incomingEdge);
                            Node child = ev.getEdge().getSource();

                            if (controller.isVisibleNode(child)) {
                                visibleChildren.add(child);
                            }

                        }


                        if (visibleChildren.size() > 0) {

                            // Want to display new children to the right of the already displayed children
                            // Loop through already displayed children, and determine the right-most coordinate.
                            for (Node nextChild : visibleChildren) {
                                NodeView cNodeView = view.getNodeView(nextChild);
                                if (cNodeView.getXPosition() > maxX) {
                                    maxX = cNodeView.getXPosition();
                                }
                            }
                        }
                    }

                    // draw the node and its siblings that need to be drawn at the same time

                    Set<Node> childrenToDraw = parentChildList.get(parentNode);

                    // Get coordinates and node size of parent node
                    NodeView pView = view.getNodeView(parentNode);
                    double x = pView.getXPosition();
                    double y = pView.getYPosition();
                    double h = pView.getHeight();

                    double maxNodeHeight = 0;
                    double maxNodeWidth = 0;
                    double nodeWidthSum = 0;

                    List<NodeView> childNodeViews = new ArrayList<NodeView>();

                    // Loop through the children nodes to be drawn.  Calculate the maximum height and width, as well
                    // as the sum of the widths, and store the node views.
                    // Also, show the node and the edge between the node and the parent node.
                    for (Node thisChild : childrenToDraw) {
                        NodeView cView = view.getNodeView(thisChild);

                        // Show the node, and the edge between this node and the parent node
                        controller.showNode(thisChild);

                        // find edge to show
                        int[] nodesToConnect = {thisChild.getRootGraphIndex(), parentNode.getRootGraphIndex()};
                        int[] edges = controller.getNetwork().getConnectingEdgeIndicesArray(nodesToConnect);

                        for (int edge : edges) {
                            controller.showEdge(edge);
                        }

                        maxNodeHeight = Math.max(maxNodeHeight, cView.getHeight());
                        maxNodeWidth = Math.max(maxNodeWidth, cView.getWidth());
                        nodeWidthSum += cView.getWidth();
                        childNodeViews.add(cView);
                    }

                    // Layout nodes

                    double nodeSpacingX = 10.0;
                    double nodeSpacingY = 5.0;
                    double baseY = y + h / 2 + maxNodeHeight / 2 + nodeSpacingY;
                    baseY = Math.max(baseY, y + 50);

                    double startX;
                    if (maxX == Double.NEGATIVE_INFINITY) {
                        startX = x - (nodeWidthSum + nodeSpacingX * childNodeViews.size()) / 2;
                    } else {
                        startX = maxX + nodeSpacingX;
                    }

                    double verticalStaggar = 34.0;

                    // Now iterate through the childnodeviews and position them heirarchically
                    double lastCx = 0.0;
                    double lastCw = 0.0;
                    boolean firstIter = true;
                    for (int j = 0; j < childNodeViews.size(); j++) {
                        NodeView childNodeV = childNodeViews.get(j);
                        double cy, cx;
                        double cw = childNodeV.getWidth();
                        if (firstIter) {
                            cx = startX;
                            firstIter = false;
                        } else {
                            cx = lastCx + lastCw / 2 + nodeSpacingX + cw;
                        }
                        cy = baseY + (j % 5 * verticalStaggar);
                        childNodeV.setXPosition(cx);
                        childNodeV.setYPosition(cy);
                        lastCw = cw;
                        lastCx = cx;
                    }


                } else { // already visible (displayed in another parentage line), but need to show edge

                    // find edge to show
                    int[] nodesToConnect = {childNode.getRootGraphIndex(), parentNode.getRootGraphIndex()};
                    int[] edges = controller.getNetwork().getConnectingEdgeIndicesArray(nodesToConnect);

                    for (int edge : edges) {
                        controller.showEdge(edge);
                    }
                }

            }

        }
        
    }

    /**
     * Method to get the 'parentage lines' from this node to the first visible parent in the network.
     * @param node the GO term node for which to determine the 'parentage lines'
     * @param controller the GOSlimmer controller for this network
     * @param view the network view for this network
     * @param parentChildList a hashmap storing the list of children that need to be displayed for each node that needs to be drawn in the network
     * @return set containing lists of nodes representing a 'parentage line', where the first entry is the first visible parent in the network, and the last entry is the 'node'.
     */
    private Set<List<Node>> getParentLines(Node node, GOSlimmerController controller, CyNetworkView view, HashMap<Node, Set<Node>> parentChildList) {

        Set<List<Node>> allpaths = new HashSet<List<Node>>();

        if (controller.isVisibleNode(node)) {
            return allpaths;
        }

        /* Loop through the outgoing edges (to parent nodes)*/
        int [] outgoingEdges = controller.getNetwork().getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, false, true);
        for (int outgoingEdge: outgoingEdges) {

            EdgeView ev = view.getEdgeView(outgoingEdge);
            Node parentNode = ev.getEdge().getTarget();

            // If parent node is already in parentChildList, then add this node to the set of nodes that need to be displayed
            // for this parent node
            if (parentChildList.containsKey(parentNode)) {
                parentChildList.get(parentNode).add(node);
            }
            else {  // Otherwise, add the parent node and this node to the parentChildList
                Set<Node> childSet = new HashSet<Node>();
                childSet.add(node);
                parentChildList.put(parentNode, childSet);
            }

            // Create a set to hold the 'parentage lines' for this node
            Set<List<Node>> parentLines;

            // If the parent node is not currently visible, then call this method recursively
            if (!controller.isVisibleNode(parentNode)) {
                parentLines = getParentLines(parentNode, controller, view, parentChildList);

            }
            else {  // Otherwise, create a new set and add the parent node
                List<Node> singleList = new ArrayList<Node>();
                singleList.add(parentNode);

                parentLines = new HashSet<List<Node>>();
                parentLines.add(singleList);
            }

            // Loop through all 'parentage lines', and add this node to the end of the line, then add this line to the set
            for (List<Node> thisList : parentLines) {
                thisList.add(node);
                allpaths.add(thisList);
            }

        }

        return allpaths;
    }



}
