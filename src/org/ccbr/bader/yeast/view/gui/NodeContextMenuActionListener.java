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
 * * Description: Adds GOSlimmer actions to the node context menu    
 */
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

/**Adds GOSlimmer actions to the node context menu.  The actions added allow the user to collapse, expand, 
 * and prune nodes, as well as to select or unselect the node for inclusion in the GO Slim Set.
 * 
 * @author mikematan
 *
 */
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
            else if (jbSource.getText().startsWith(selectButtonText)) { // either 'Select' or 'Select (No Associated Genes)'
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
