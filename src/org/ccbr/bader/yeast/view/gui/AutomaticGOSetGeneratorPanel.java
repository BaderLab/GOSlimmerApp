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
            numTermsLabel = new JLabelMod("Number of terms to find:");
        }
        return numTermsLabel;
    }

    private JButton findCoveringTermsButton;
	private String findCoveringTermsButtonText = "Find Best Covering Terms";
	private String findCoveringTermsButtonToolTip = "Find the best covering terms for the remaining uncovered genes";
	private JButton getFindCoveringTermsButton() {
		if (findCoveringTermsButton==null) {
			findCoveringTermsButton = new JButtonMod(findCoveringTermsButtonText);
			findCoveringTermsButton.addActionListener(this);
			findCoveringTermsButton.setToolTipText(findCoveringTermsButtonToolTip);
        }
		return findCoveringTermsButton;
	}

    private JScrollPane coveringSetScrollPane;
    private String coveringSetScrollPaneToolTip = "List of best covering terms.";
    private JScrollPane getCoveringSetScrollPane() {
        if (coveringSetScrollPane == null) {
            coveringSetScrollPane = new JScrollPane();
            coveringSetScrollPane.setToolTipText(coveringSetScrollPaneToolTip);
            coveringSetScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            // set size of scroll pane
            Dimension dim = new Dimension();
            dim.setSize(275.0, 94.0);
            coveringSetScrollPane.setPreferredSize(dim);

            // Create JList item for scroll pane
            JList coveringSetList = new JList(coveringSetNames);
            coveringSetList.setVisibleRowCount(4);
            coveringSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            coveringSetScrollPane.getViewport().setView(coveringSetList);
        }
        return coveringSetScrollPane;
    }

    private JButton showCoveringTermButton;
	private String showCoveringTermButtonText = "Show";
	private String showCoveringTermButtonToolTip = "Show selected covering term in GO diagram";
	private JButton getShowCoveringTermButton() {
		if (showCoveringTermButton==null) {
			showCoveringTermButton = new JButtonMod(showCoveringTermButtonText);
			showCoveringTermButton.addActionListener(this);
			showCoveringTermButton.setToolTipText(showCoveringTermButtonToolTip);
        }
		return showCoveringTermButton;
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
        this.add(getFindCoveringTermsButton(),c);

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
        this.add(getShowCoveringTermButton(),c);

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
            if (bsrc.equals(findCoveringTermsButton)) {  // Generate list of covering terms
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

                    int numTopTerms = (Integer) numTermsTextField.getValue();

                    // Get list of Go terms with best gene coverage (based on current set of selected go terms)
                    String[] topTermsString = generator.getTopTermsStringsInferred(controller.getStatBean().getSlimGoNodes(), numTopTerms);

                    // Display list of GO terms in scroll pane
                    JList tempList = (JList) coveringSetScrollPane.getViewport().getView();

                    tempList.setListData(topTermsString);

                }

            }
            else if (bsrc.equals(showCoveringTermButton)) {
                                
                JList list = (JList) coveringSetScrollPane.getViewport().getView();
                if (list.getSelectedIndex()!= -1) {
                    String fullName = (String) list.getSelectedValue();

                    /* Get GOId of node from string in list */
                    int pStartIndex = fullName.lastIndexOf("(");
                    int pEndIndex = fullName.indexOf(")",pStartIndex);
                    String goId = fullName.substring(pStartIndex+1, pEndIndex);

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
                        System.out.println("This node is not in this network");
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
                                ExpandCollapseEdit undoableEdit = new ExpandCollapseEdit(controller, "Show Node");

                                // Find and display the node in the graph, and select it in cytoscape
                                controller.displayNode(goNode);
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

}
