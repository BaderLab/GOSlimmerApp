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
 * * Description: The main interface panel for GOSlimmer
 */

package org.ccbr.bader.yeast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;
import org.ccbr.bader.yeast.view.gui.AdvancedViewSettingsPanel;
import org.ccbr.bader.yeast.view.gui.FileExportPanel;
import org.ccbr.bader.yeast.view.gui.GOSlimmerGeneAssociationDialog;
import org.ccbr.bader.yeast.view.gui.UserGeneSetImportPanel;
import org.ccbr.bader.yeast.view.gui.misc.JCollapsablePanel;
import org.ccbr.bader.yeast.view.gui.misc.JLabelMod;
import org.ccbr.bader.yeast.view.gui.SelectedGOTermsPanel;
import org.ccbr.bader.yeast.view.gui.AutomaticGOSetGeneratorPanel;

import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;

public class GOSlimPanel extends JPanel {

	/**
	 * Panel displaying advanced options which allows the user to modify the behaviour of GOSlimmer
	 */
	protected AdvancedViewSettingsPanel viewSettingsPanel;
	
	/**
	 * File Export Options panel 
	 */
	private FileExportPanel fileExportPanel;
	
	private GOSlimmerControlPanelToGraphInterface cptgi = null;
	
    private AutomaticGOSetGeneratorPanel generatorPanel;
    
	/**
	 * The GOSlimmer session object which this panel is associated with
	 */
	private GOSlimmerSession session;
	
	/**Constructs a GOSlimPanel to manipulate the networks and controllers associated with the given mapping of namespace to controllers and the session 
	 * @param namespaceToController map of namespaces to the controllers for the corresponding GOSlimmer subgraph
	 * @param session the GOSlimmer session to which this panel belongs
	 */
	public GOSlimPanel(final Map<GONamespace,GOSlimmerController> namespaceToController,GOSlimmerSession session) {
		super();
		this.session = session;
		super.setName("GOSlimmer");
//		this.setLayout(new GridLayout(0,1));
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.add(new GOSlimmerGeneAssociationDialog(session.getNamespaceToController(), session.getOntologyName(),session));
		this.add(getUserGeneSetImportPanel());
		for(GONamespace namespace: namespaceToController.keySet()) {
			GOSlimmerController controller = namespaceToController.get(namespace);
			//GOSlimmerNamespaceSubpanel namespaceSubPanel = new GOSlimmerNamespaceSubpanel(namespace.getName() + " Coverage: ",controller);
			GOSlimmerNamespaceSubpanel namespaceSubPanel = new GOSlimmerNamespaceSubpanel(namespace.getName(),controller);
			namespaceSubPanel.setCollapsed(false);
			namespaceToSubpanel.put(namespace,namespaceSubPanel);
			controller.setInferredCoverageStatisticViewLabel(namespaceSubPanel.inferredCoverageStatisticLabel);
			controller.setDirectCoverageStatisticViewLabel(namespaceSubPanel.directCoverageStatisticLabel);
            controller.setSelectedGOTermsPanel(namespaceSubPanel.selectedGOTermsPanel);

            this.add(namespaceSubPanel); //TODO revise this so that the panels are added in a fixed order
		}
		this.namespaceToController = namespaceToController;
		viewSettingsPanel = new AdvancedViewSettingsPanel(session.getNamespaceToController().values());
		this.add(viewSettingsPanel);
		
		this.add(getFileExportPanel());
		
        generatorPanel = new AutomaticGOSetGeneratorPanel(session);
        this.add(generatorPanel);
        
        session.setGOSlimPanel(this);
	}
	
	private FileExportPanel getFileExportPanel() {
		if (fileExportPanel ==null) {
			fileExportPanel = new FileExportPanel(this.session.getNamespaceToController().values(),this.session);
		}
		return fileExportPanel;
	}

	/**
	 * The gui widget for importing user gene sets
	 */
	private UserGeneSetImportPanel userGeneSetImportPanel;
	
	/** retrieves the user gene set import panel gui widget, initializing it if it is null
	 * @return
	 */
	public UserGeneSetImportPanel getUserGeneSetImportPanel() {
		if (userGeneSetImportPanel == null) {
			userGeneSetImportPanel = new UserGeneSetImportPanel(session);
			userGeneSetImportPanel.setLayout(new GridLayout(0,1));
		}
		return userGeneSetImportPanel;
	}
	
	/**
	 * Maps namespaces to their associated subpanel of this panel
	 */
	private Map<GONamespace,GOSlimmerNamespaceSubpanel> namespaceToSubpanel = new HashMap<GONamespace, GOSlimmerNamespaceSubpanel>();
	/**
	 * Maps namespaces to their associated controller
	 */
	private Map<GONamespace,GOSlimmerController> namespaceToController = new HashMap<GONamespace, GOSlimmerController>();
	
	
	/**Each namespace subgraph has a corresponding instance of this class, which contains relevant stats about that GOSlimmer subgraph, primarily coverages statistics
	 * 
	 * @author mikematan
	 *
	 */
	private class GOSlimmerNamespaceSubpanel extends JCollapsablePanel{
		
		/**
		 * The statbean which serves as the model for this view
		 */
		GOSlimmerCoverageStatBean statBean = null;
		
		/**
		 * displays inferred coverage stats
		 */
		JLabel inferredCoverageStatisticLabel;
		/**
		 * displays direct coverage stats
		 */
		JLabel directCoverageStatisticLabel;

        SelectedGOTermsPanel selectedGOTermsPanel;

        private final String lsep = System.getProperty("line.separator");
		
		String inferredCoverageStatisticLabelToolTip =         "The percentage of gene annotation file genes covered directly " +
														lsep + "by the GO terms selected for inclusion in the slim set, as " +
														lsep + "well as those genes annotated by descendant terms within this " +
														lsep + "tree(whether expanded or collapsed)";
		String directCoverageStatisticLabelToolTip =            "The percentage of gene annotation file genes covered directly " +
														lsep +  "by the GO terms explicitely selected for inclusion in the slim set";

        String selectedGOTermsPanelToolTip = "The list of selected GO terms for this tree";

        /**
		 * The numerical format used for displaying the gene coverage statistics 
		 */
		private NumberFormat numFormatter = new DecimalFormat("00.00%");
		
		/**Contructs a namespace subpanel with the given name for the given controller's associated network model
		 * @param name the title to display for this subpanel
		 * @param controller the controller (and component network) which this subpanel will display information for
		 */
		public GOSlimmerNamespaceSubpanel(String name,final GOSlimmerController controller) {
			super(name);
//			this.setBorder(BorderFactory.createTitledBorder(name));
			
			//this.add(new JLabel(name));
//			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			//this.setLayout(new GridLayout(0,1));

            this.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1;
            c.weighty = 1;

            c.anchor = GridBagConstraints.FIRST_LINE_START;
		    c.gridx=0;
		    c.gridy=0;
            c.fill = GridBagConstraints.HORIZONTAL;

            this.statBean = controller.getStatBean();
			//String coverageStatisticText = String.v statBean.fractionCovered();
			//this.coverageStatisticLabel = new JLabel(numFormatter.format(statBean.fractionCovered())){
			
			this.inferredCoverageStatisticLabel = new JLabelMod("Inferred Coverage: " + numFormatter.format(statBean.fractionInferredCovered()));
			inferredCoverageStatisticLabel.setToolTipText(inferredCoverageStatisticLabelToolTip);
            this.add(inferredCoverageStatisticLabel, c);

            c.gridx=0;
            c.gridy=1;
            c.fill = GridBagConstraints.HORIZONTAL;
            
            this.directCoverageStatisticLabel = new JLabelMod("Direct Coverage: " + numFormatter.format(statBean.fractionDirectlyCovered()));
			directCoverageStatisticLabel.setToolTipText(directCoverageStatisticLabelToolTip);
			this.add(directCoverageStatisticLabel, c);

    		c.gridx=0;
		    c.gridy=2;
		    c.fill = GridBagConstraints.HORIZONTAL;
		    c.gridwidth = GridBagConstraints.REMAINDER;

            this.selectedGOTermsPanel = new SelectedGOTermsPanel(controller);
            selectedGOTermsPanel.setToolTipText(selectedGOTermsPanelToolTip);
            selectedGOTermsPanel.setCollapsed(true);
            this.add(selectedGOTermsPanel,c);

            //add a mouselistener so that clicking on the subpanel will bring the associated network view into focus
			MouseListener changeFocusMouseListener = new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					
					Cytoscape.firePropertyChange(CytoscapeDesktop.NETWORK_VIEW_FOCUS,
							 null, controller.getNetwork().getIdentifier());
					
				}

				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			};
			this.addMouseListener(changeFocusMouseListener);
			this.directCoverageStatisticLabel.addMouseListener(changeFocusMouseListener);
			this.inferredCoverageStatisticLabel.addMouseListener(changeFocusMouseListener);
		}
		
//		public GOSlimmerNamespaceSubpanel(String title,String coverageLabelTitle, GOSlimmerController controller) {
//			this(coverageLabelTitle,controller);
//			this.setBorder(BorderFactory.createTitledBorder(title));
//		}

		public JLabel getInferredCoverageStatisticLabel() {
			return inferredCoverageStatisticLabel;
		}

		public void setInferredCoverageStatisticLabel(JLabel coverageStatisticLabel) {
			this.inferredCoverageStatisticLabel = coverageStatisticLabel;
		}

		protected JLabel getDirectCoverageStatisticLabel() {
			return directCoverageStatisticLabel;
		}

		protected void setDirectCoverageStatisticLabel(
				JLabel directCoverageStatisticLabel) {
			this.directCoverageStatisticLabel = directCoverageStatisticLabel;
		}
		

		
	}
	
	
	public JLabel getBioProCoverage() {
		return getCoverage(GONamespace.BioPro);
	}

	public void setBioProCoverage(JLabel coverage) {
		setCoverageLabel(GONamespace.BioPro,coverage);
	}

	public JLabel getCelComCoverage() {
		return getCoverage(GONamespace.CelCom);
	}

	public void setCelComCoverage(JLabel coverage) {
		setCoverageLabel(GONamespace.CelCom,coverage);
	}

	public JLabel getMolFunCoverage() {
		return getCoverage(GONamespace.MolFun);
	}

	public void setMolFunCoverage(JLabel coverage) {
		setCoverageLabel(GONamespace.MolFun,coverage);
	}
	
	private JLabel getCoverage(GONamespace ns) {
		return namespaceToSubpanel.get(ns).getInferredCoverageStatisticLabel();
	}
	
	private void setCoverageLabel(GONamespace ns, JLabel coverage) {
		namespaceToSubpanel.get(ns).setInferredCoverageStatisticLabel(coverage);
	}
	
//	public AdvancedViewSettingsPanel getViewSettingsPanel() {
//		return viewSettingsPanel;
//	}
//	
//	public void setViewSettingsPanel(AdvancedViewSettingsPanel viewSettingsPanel) {
//		this.viewSettingsPanel = viewSettingsPanel;
//	}
	
	public void setNamespaceSubpanelsVisible(boolean visible) {
		for(GOSlimmerNamespaceSubpanel nsp:namespaceToSubpanel.values()) {
			nsp.setVisible(visible);
		}
	}
	
	public void setViewSettingsPanelVisible(boolean visible) {
		viewSettingsPanel.setVisible(visible);
	}
	
	public void setUserGeneSetImportPanelVisible(boolean visible) {
		getUserGeneSetImportPanel().setVisible(visible);
	}
	
	public  void setFileExportPanelVisible(boolean visible) {
		fileExportPanel.setVisible(visible);
	}

    public void setAutomaticGOSetGeneratorPanelVisible(boolean visible) {
        generatorPanel.setVisible(visible);
    }
}
