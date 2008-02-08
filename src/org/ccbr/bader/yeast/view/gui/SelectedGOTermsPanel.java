package org.ccbr.bader.yeast.view.gui;

import org.ccbr.bader.yeast.view.gui.misc.JCollapsablePanel;
import org.ccbr.bader.yeast.controller.GOSlimmerController;

import javax.swing.*;
import java.util.Set;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.*;

import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.data.CyNetworkUtilities;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import giny.view.NodeView;
import cytoscape.ding.DingNetworkView;

/**GUI Widget for displaying list of currently selected GO nodes
 * Allows user to click on a selected node in the list to zoom to that node
 *
 *
 * @author laetitiamorrison
 *
 */
public class SelectedGOTermsPanel extends JCollapsablePanel implements MouseListener {

    private String[] selectedTerms = {};
    
    private GOSlimmerController controller;
    

    public SelectedGOTermsPanel(GOSlimmerController controller) {
        super("Selected GO Terms");
        this.controller = controller;

        initComponents();
    }

	private static final String lsep = System.getProperty("line.separator");

    JScrollPane selectedGOTermsScrollPane;
    private static final String selectedGOTermsToolTip = "List of selected GO nodes.";

    private JScrollPane getSelectedGOTermsScrollPane() {
        if (selectedGOTermsScrollPane == null) {
            selectedGOTermsScrollPane = new JScrollPane();
            selectedGOTermsScrollPane.setToolTipText(selectedGOTermsToolTip);
            selectedGOTermsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            // Set size of scroll pane
            Dimension dim = new Dimension();
            dim.setSize(325.0, 94.0);
            selectedGOTermsScrollPane.setPreferredSize(dim);

            // Create JList item for scroll pane
            JList selectedTermsList = new JList(selectedTerms);
            selectedTermsList.setVisibleRowCount(4);
            selectedTermsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectedTermsList.addMouseListener(this);

            selectedGOTermsScrollPane.getViewport().setView(selectedTermsList);
        }
        return selectedGOTermsScrollPane;
    }

	/**
	 * Initializes the widget's subcomponents and layout
	 */
	private void initComponents() {
		//this.setLayout(new GridLayout(0,1));
        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        this.add(getSelectedGOTermsScrollPane());
    }

    public void mouseClicked(MouseEvent event) {
        Object src = event.getSource();
		if (src instanceof JList) {
            JList list = (JList)src;
            int index = list.getSelectedIndex();
            if (index!=-1) {

                /* Get GOId of node from string in list */
                String name = (String)list.getSelectedValue();
                int pIndex = name.lastIndexOf("(");
                String goId = name.substring(pIndex+1, name.length()-1);

                /* Get network and network view associated with this panel/list */
                CyNetwork network = controller.getNetwork();
                CyNetworkView view = controller.getNetworkView();

                /* Select the GO node corresponding to the selected list item, and zoom to the selected node */
                /* If the user has clicked on a panel for another namespace, change the focus to that network */
                network.unselectAllNodes();
                boolean found = CyNetworkUtilities.selectNodesStartingWith(network, goId, view);
                if (found) {

                    /* Zoom to selected node */
                    ((DingNetworkView) view).fitSelected();
                    Set nodeSet = network.getSelectedNodes();

                    /* Modify zoom so that it shows the full node label */
                    if (nodeSet.size()==1) {
                        CyNode node = (CyNode)(nodeSet.toArray())[0];
                        NodeView nodeView = view.getNodeView(node);

                        double nWidth = nodeView.getWidth(); // width of the node

                        FontMetrics metrics = getFontMetrics(nodeView.getLabel().getFont());
                        int lWidth = metrics.stringWidth(nodeView.getLabel().getText()); // width of the label

                        view.setZoom(view.getZoom() * (nWidth/lWidth)); // zoom out to fit full label
                    }

                    view.updateView();
                    /* Change network focus if necessary */
                    if (!view.equals(Cytoscape.getCurrentNetworkView())) {
                        Cytoscape.firePropertyChange(CytoscapeDesktop.NETWORK_VIEW_FOCUS, null, network.getIdentifier());
                    }
                }

            }
        }
	}

    public void mousePressed(MouseEvent event) {
        // Do nothing
    }

    public void mouseEntered(MouseEvent e) {
        // Do nothing
	}

    public void mouseExited(MouseEvent e) {
        // Do nothing
    }

	public void mouseReleased(MouseEvent e) {
	    // Do nothing
	}

    public void setList(String[] listOfTerms) {
        JList tempList = (JList) selectedGOTermsScrollPane.getViewport().getView();
        tempList.setListData(listOfTerms);
    }

}
