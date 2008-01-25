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
 * * Description: Actionlistener which starts and ends GOSlimmer sessions according to plugins submenu actions by the user
 */
package org.ccbr.bader.yeast;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.view.gui.AdvancedViewSettingsPanel;
import org.ccbr.bader.yeast.view.gui.FileExportPanel;
import org.ccbr.bader.yeast.view.gui.GOSlimmerGeneAssociationDialog;
import org.ccbr.bader.yeast.view.gui.UserGeneSetImportPanel;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.visual.VisualMappingManager;

/**Actionlistener which starts and ends GOSlimmer sessions according to plugins submenu actions by the user
 * 
 * @author mikematan
 *
 */
public class GOSlimPanelAction implements ActionListener {

	/*Notes whether or not the GOSlimPanel has been opened yet or now */
	boolean alreadyOpened = false;
//	GOSlimPanel goSlimPanel =null;

    GOSlimmerUndo undo;
    private int undoLimit = 15;
    private int prevUndoLimit;


    public GOSlimPanelAction() {
		// TODO Auto-generated constructor stub
        undo = new GOSlimmerUndo();
    }


	private static final String lsep = System.getProperty("line.separator");
	
	/** Either starts a new GOSlimmer session or ends an existing session.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
//		display GOSlimmer Main Panel in left cytopanel
		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
		
		if (event.getSource() instanceof JMenuItem) {
			JMenuItem src = (JMenuItem) event.getSource();
			if (src.getText().equals("Exit GOSlimmer")) {
				cytoPanel.remove(goSlimmerSessionsTabbedPane);
				goSlimmerSessionsTabbedPane = null;
				//delete goslimmer specific attributes:
				GOSlimmerUtil.deleteGOSlimmerAttributes();
				alreadyOpened=false;

                // Reset the limit of depth for undo to original value
                undo.setLimit(prevUndoLimit);
            }
			else if (src.getText().equals("Start GOSlimmer")) {
				
				//Unfortuntately, because cyattributes are defined globally for all nodes with the same id, we can't at this time 
				//use goslimmer on two DAGs at the same time
				
				if (alreadyOpened) {
					JOptionPane.showMessageDialog(desktop, "GOSlimmer cannot be used to edit more than one GO Tree at a time."
							+ lsep + "To edit a new graph, select to close GOSlimmer from Pluggins->GOSlimmer and then start "
							+ lsep + "on the new GO Tree you wish to edit.");
					return;
				}
				
                // Store the current undo depth limit, and reset to GOSlimmer limit
                prevUndoLimit = undo.getLimit();
                undo.setLimit(undoLimit);

				

//				if (!alreadyOpened) {
//					//initialize the goSlimPanel and add it to the cytopanel
//					goSlimPanel = new GOSlimPanel();
//					cytoPanel.add(goSlimPanel);
//					alreadyOpened = true;
//				}
				VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		        if (!vmm.getCalculatorCatalog().getVisualStyleNames().contains("GOSLIMMERVS")) {
		            vmm.getCalculatorCatalog().addVisualStyle(new GOSlimmerVisualStyle(vmm.getVisualStyle(),"GOSLIMMERVS"));
		        }
				//vmm.setVisualStyle("GOSLIMMERVS");
				
				//create a new goslimmersession and add it to the gomainpanel tabbed panel
				//note that we probably can't manipulate multiple go graphs at the same time due to the way attributes are saved
				
				CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
				
				//ensure that the network is not null
				if (currentNetwork==null) {
					JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Cannot start GOSlimmer without a GO Tree Network.  Please load a GO Ontology Tree, selected it as the current network, and then start GOSlimmer","Error - cannot start GOSlimmer",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//ensure that the network corresponds to a loaded Gene Ontology
				if (!GOSlimmerUtil.isOntology(currentNetwork.getTitle())) {
					JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Cannot start GOSlimmer without a Gene Ontology Tree Network."
							+ lsep + "Network name '" + currentNetwork.getTitle() + "' does not match the name of a loaded Gene Ontology."
							+ lsep + "Please load a GO Tree through the \"File->Import->Ontology and Annotation\" dialog,"
							+ lsep + "selected it as the current network, and then start GOSlimmer","Error - cannot start GOSlimmer",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				GOSlimmerSession newSession =  new GOSlimmerSession(currentNetwork);
				GOSlimPanel newSessionPanel = new GOSlimPanel(newSession.getNamespaceToController(),newSession);
				{
					//initially, these should be invisible
					newSessionPanel.setNamespaceSubpanelsVisible(false);
					newSessionPanel.setViewSettingsPanelVisible(false);
					newSessionPanel.setFileExportPanelVisible(false);
					newSessionPanel.setUserGeneSetImportPanelVisible(false);
                    newSessionPanel.setAutomaticGOSetGeneratorPanelVisible(false);                    
                }

				if (!alreadyOpened) {
//					goSlimmerSessionsTabbedPane = new JTabbedPane();
					goSlimmerSessionsTabbedPane = newSessionPanel;
					
					JPanel layoutPanel = new JPanel();
//					layoutPanel.setLayout(new CardLayout());
					
					goSlimmerSessionsTabbedPane = layoutPanel;
					goSlimmerSessionsTabbedPane.add(newSessionPanel);
					goSlimmerSessionsTabbedPane.add(new JPanel());
//					goSlimmerSessionsTabbedPane.add(newSessionPanel,BorderLayout.PAGE_START);
//					goSlimmerSessionsTabbedPane.add(new JPanel(),BorderLayout.PAGE_END);
					
					
					cytoPanel.add("GOSlimmer",goSlimmerSessionsTabbedPane);
					alreadyOpened = true;
				}
				
				//add the new session pane to the goslimmer cytopanel
//				goSlimmerSessionsTabbedPane.add(currentNetwork.getTitle(),newSessionPanel);
				
				//get the index of the panel and tell it to dock it
				int index = cytoPanel.indexOfComponent(goSlimmerSessionsTabbedPane);
				cytoPanel.setSelectedIndex(index);
				cytoPanel.setState(CytoPanelState.DOCK);
			}
		}
		


	}
	private JPanel goSlimmerSessionsTabbedPane;


	
}
