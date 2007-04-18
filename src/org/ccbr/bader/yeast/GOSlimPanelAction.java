 package org.ccbr.bader.yeast;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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

public class GOSlimPanelAction implements ActionListener {

	/*Notes whether or not the GOSlimPanel has been opened yet or now */
	boolean alreadyOpened = false;
//	GOSlimPanel goSlimPanel =null;

	protected String directlyAnnotatedGenesAttributeName = "GENE_ASSOC.DIRECTLY_ANNOTATED_GENES";
	protected String inferredAnnotatedGenesAttributeName = "GENE_ASSOC.INFERRED_ANNOTATED_GENES";
	
	public GOSlimPanelAction() {
		// TODO Auto-generated constructor stub
	}


	private static final String lsep = System.getProperty("line.separator");
	
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
			}
			else if (src.getText().equals("Start GOSlimmer")) {
				
				//Unfortuntately, because cyattributes are defined globally for all nodes with the same id, we can't at this time 
				//use goslimmer on two dags at the same time
				
				if (alreadyOpened) {
					JOptionPane.showMessageDialog(desktop, "GOSlimmer cannot be used to edit more than one GO Tree at a time."
							+ lsep + "To edit a new graph, select to close GOSlimmer from Pluggins->GOSlimmer and then start "
							+ lsep + "on the new GO Tree you wish to edit.");
					return;
				}
				


				

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
				GOSlimPanel newSessionPanel = new GOSlimPanel(newSession.getNamespaceToController());
				
				//add the advanced view settings panel to the goSlimPanel 
				//TODO revise so that these are automatically created within GOSlimPanel
				newSessionPanel.add(new AdvancedViewSettingsPanel(newSession.getNamespaceToController().values()));
				newSessionPanel.add(new UserGeneSetImportPanel(newSession));
				newSessionPanel.add(new FileExportPanel(newSession.getNamespaceToController().values(),newSession));
//				newSessionPanel.add(new GOSlimmerGeneAssociationDialog(newSession.getNamespaceToController(),newSession.getOntologyName()),0);
				newSessionPanel.add(new GOSlimmerGeneAssociationDialog(newSession.getNamespaceToController(), newSession.getOntologyName(),newSession),0);
				
//				new GOSlimmerGeneAssociationDialog(newSession.getNamespaceToController(), newSession.getOntologyName(),newSession);
				if (!alreadyOpened) {
					goSlimmerSessionsTabbedPane = new JTabbedPane();
					
					cytoPanel.add("GOSlimmer",goSlimmerSessionsTabbedPane);
					alreadyOpened = true;
				}
				
				//add the new session pane to the goslimmer cytopanel
				goSlimmerSessionsTabbedPane.add(currentNetwork.getTitle(),newSessionPanel);
				
				//get the index of the panel and tell it to dock it
				int index = cytoPanel.indexOfComponent(goSlimmerSessionsTabbedPane);
				cytoPanel.setSelectedIndex(index);
				cytoPanel.setState(CytoPanelState.DOCK);
			}
		}
		


	}
	private JTabbedPane goSlimmerSessionsTabbedPane;
	
}
