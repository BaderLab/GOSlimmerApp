package org.ccbr.bader.yeast.view.gui;

import giny.model.Node;
import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;

import org.ccbr.bader.yeast.controller.GOSlimmerController;

import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.util.TaskManager;
import cytoscape.visual.VisualStyle;

public class NodeContextMenuActionListener implements ActionListener {

	private Node node;
	private GOSlimmerController controller;
	public NodeContextMenuActionListener(Node node,GOSlimmerController controller) {
		this.node=node;
		this.controller=controller;
	}

	private String collapseButtonText = "Collapse";
	private String expandButtonText = "Expand";
	private String pruneButtonText = "Prune";
	private String cancelButtonText = "Cancel";
	private String selectButtonText = "Select";
	private String deselectButtonText = "Deselect";
	
	public void actionPerformed(ActionEvent e) {
		
		Object source = e.getSource();
		if (source instanceof JMenuItem) {
			JMenuItem jbSource = (JMenuItem) source;
			
			if (jbSource.getText().equals(collapseButtonText)) {
				controller.collapseNode(node);
				
				//the dialog has fulfilled it's purpose, so dispose of it
			}
			else if (jbSource.getText().equals(expandButtonText)) {
				System.out.println("expand button depressed");
				controller.expandNode(node);
				
			}
			else if (jbSource.getText().equals(pruneButtonText)) {
				System.out.println("Prune button depressed");
				controller.pruneNode(node);
			}
			else if (jbSource.getText().equals(cancelButtonText)) {
				System.out.println("Cancel button depressed");
				//obsolete testing code
//				VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
//				vmm.getVisualStyle();
//				vmm.applyNodeAppearances();
			}
			else if (jbSource.getText().equals(selectButtonText)) {
				controller.addNodeToSlimSet(node);
			}
			else if (jbSource.getText().equals(deselectButtonText)) {
				TaskManager.executeTask(new Task() {

					public String getTitle() {
						return "Recalculating Coverage Statistics";
					}

					public void halt() {
						// TODO Auto-generated method stub
						
					}

					public void run() {
						controller.removeNodeFromSlimSet(node);
						
					}

					public void setTaskMonitor(TaskMonitor arg0) throws IllegalThreadStateException {
						// TODO Auto-generated method stub
						
					}
					
				}, null);
				
			}
			else {
				//TODO deal with unexpecte button press
			}
			//redraw the graph with the appropriate changes
			//TODO remove this setting and unsetting of the visual style, as it has little impact
			//VisualStyle vs = Cytoscape.getVisualMappingManager().setVisualStyle("GOSLIMMERVS");
			Cytoscape.getCurrentNetworkView().redrawGraph(false,false);
			//Cytoscape.getVisualMappingManager().setVisualStyle(vs);
			
			
			//collapseExpandDialog.dispose();
		}
		else {
			//TODO deal with unexpected event
		}

	}

}
