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
 * * Description: GUI Widget for manipulating advanced settings of GOSlimmer's view   
 */
package org.ccbr.bader.yeast.view.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.view.gui.misc.JCheckBoxMod;
import org.ccbr.bader.yeast.view.gui.misc.JCollapsablePanel;

import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;

/**GUI Widget for manipulating advanced settings of GOSlimmer's view.
 * Allows manipulation of settings like how the size of nodes should be determined and what kinds of statistics should be displayed 
 *
 * 
 * @author mikematan
 *
 */
public class AdvancedViewSettingsPanel extends JCollapsablePanel implements ActionListener, PropertyChangeListener {

	private Collection<GOSlimmerController> controllers;

	public AdvancedViewSettingsPanel(Collection<GOSlimmerController> controllers) {
		super("Advanced View Settings");
		this.controllers = controllers;
	}
	
	
	

	
	private static final String lsep = System.getProperty("line.separator");
	
	JCheckBox includeDescendentCoverageInNodeSizeCalculationCheckBox;
	private static final String includeDescendentCoverageInNodeSizeCalculationText = "Include child nodes when calculating node size";
	private static final String includeDescendentCoverageInNodeSizeCalculationToolTip = 
				"If checked, then the size of the node will be proportional to the number of genes" 
		+ lsep +"directly annotated by the node + the number of genes annotated by all of the nodes " 
		+ lsep +"descendants in the graph. If unchecked, size is proportional to the directly " 
		+ lsep +"annotated nodes only.";
	
	
	
	private JCheckBox getIncludeDeCheckBox() {
		if (includeDescendentCoverageInNodeSizeCalculationCheckBox==null) {
			includeDescendentCoverageInNodeSizeCalculationCheckBox = new JCheckBoxMod(includeDescendentCoverageInNodeSizeCalculationText);
			includeDescendentCoverageInNodeSizeCalculationCheckBox.setSelected(GOSlimmerGUIViewSettings.includeDescendantInferredCoveredGenesInNodeSizeCalculations);
			includeDescendentCoverageInNodeSizeCalculationCheckBox.setToolTipText(includeDescendentCoverageInNodeSizeCalculationToolTip);
			includeDescendentCoverageInNodeSizeCalculationCheckBox.addActionListener(this);
		}
		return includeDescendentCoverageInNodeSizeCalculationCheckBox;
	}
	
	JCheckBox labelNodesWithOntologyName;
	private final String labelNodesWithOntologyNameLabel = "Label nodes with Ontology Term Name";
	private final String labelNodesWithOntologyNameToolTip = "If checked, nodes will be labelled with their biologically meaningful " +
													  lsep + "ontology term name.  Otherwise, they will be labelled with their GO ID.";
	
	private JCheckBox getLCheckBox() {
		if (labelNodesWithOntologyName==null) {
			labelNodesWithOntologyName = new JCheckBoxMod(labelNodesWithOntologyNameLabel);
			labelNodesWithOntologyName.setSelected(GOSlimmerGUIViewSettings.labelNodesWithOntologyName);
			labelNodesWithOntologyName.setToolTipText(labelNodesWithOntologyNameToolTip);
			labelNodesWithOntologyName.addActionListener(this);
		}
		return labelNodesWithOntologyName;
	}


    /* Added by Laetitia Morrison */
    JCheckBox showGODefinitionAsToolTip;
	private final String showGODefinitionAsToolTipLabel = "Show GO definition as node tool tip";
	private final String showGODefinitionAsToolTipToolTip = "If checked, nodes will have their GO term definition " +
													  lsep + "appear as a tool tip.  Otherwise, no tool tip will appear.";

	private JCheckBox getDefnCheckBox() {
		if (showGODefinitionAsToolTip==null) {
			showGODefinitionAsToolTip = new JCheckBoxMod(showGODefinitionAsToolTipLabel);
			showGODefinitionAsToolTip.setSelected(GOSlimmerGUIViewSettings.showGODefinitionAsToolTip);
			showGODefinitionAsToolTip.setToolTipText(showGODefinitionAsToolTipToolTip);
			showGODefinitionAsToolTip.addActionListener(this);
		}
		return showGODefinitionAsToolTip;
	}
    /* End of added code */

    /* Added by Laetitia Morrison
     * Checkbox to determine what children nodes to display when a node is expanded.
     * If box is checked, then only the children nodes which have associated genes (either direct or inferred)
     * will be shown.  If box is unchecked, then all children will be displayed.
     */
    JCheckBox expandNodesWithGenes;
	private final String expandNodesWithGenesLabel = "Expand nodes with associated genes only";
	private final String expandNodesWithGenesToolTip = "If checked, only those children with associate genes " +
													  lsep + "(direct or inferred) will be shown when a node is " +
                                                      lsep + "expanded.";

	private JCheckBox getExpandNodesCheckBox() {
		if (expandNodesWithGenes==null) {
			expandNodesWithGenes = new JCheckBoxMod(expandNodesWithGenesLabel);
			expandNodesWithGenes.setSelected(GOSlimmerGUIViewSettings.expandNodesWithGenes);
			expandNodesWithGenes.setToolTipText(expandNodesWithGenesToolTip);
			expandNodesWithGenes.addActionListener(this);
		}
		return expandNodesWithGenes;
	}
    /* End of added code */

    private JCheckBox expandNodeDepthCheckbox;
	private JTextField expandNodeDepthTextField;
	
	private final String expandNodeDepthTooltip = 
				 "If checked, a node's descendant tree will only be expanded to the " +
			lsep+"specified depth.  If uncheck, the full descendant tree will be expanded.";
	
	private final String expandNodeDepthCheckboxText = "Expand nodes to specified depth";
	
	private JCheckBox getExpandNodeDepthCheckbox() {
		if (expandNodeDepthCheckbox==null) {
			expandNodeDepthCheckbox = new JCheckBoxMod(expandNodeDepthCheckboxText);
			//expandNodeDepthCheckbox.setText(expandNodeDepthCheckboxText);
			expandNodeDepthCheckbox.setToolTipText(expandNodeDepthTooltip);
			expandNodeDepthCheckbox.setSelected(true);
			expandNodeDepthCheckbox.addActionListener(this);
		}
		return expandNodeDepthCheckbox;
	}
	
	
	
	private JTextField getExpandNodeTextField() {
		if (expandNodeDepthTextField == null) {
			expandNodeDepthTextField = new JFormattedTextField("1");
			expandNodeDepthTextField.setEnabled(expandNodeDepthCheckbox.isSelected());
			expandNodeDepthTextField.addActionListener(this);
			expandNodeDepthTextField.addPropertyChangeListener("value",this);
			
//			expandNodeDepthTextField.sets
		}
		return expandNodeDepthTextField;
	}
	
	private JCheckBox sizeNodesBasedOnNumUserGenesAnnotatedCheckbox;
	private static final String sizeNodesBasedOnNumUserGenesAnnotatedCheckboxText = "Size Nodes according to User Gene Set";
	private static final String sizeNodesBasedOnNumUserGenesAnnotatedCheckboxToolTip =
			 "If checked, the size of the GO nodes will be determined based on the number" +
		lsep+"of user genes which are annotated by that node(directly or by inferrence,  " +
		lsep+"according to the setting of the " + includeDescendentCoverageInNodeSizeCalculationText + "'" + 
		lsep+"CheckBox).";
	
	private JCheckBox getSizeNodesBasedOnNumUserGenesAnnotatedCheckbox() {
		if (sizeNodesBasedOnNumUserGenesAnnotatedCheckbox==null) {
			sizeNodesBasedOnNumUserGenesAnnotatedCheckbox = new JCheckBoxMod(sizeNodesBasedOnNumUserGenesAnnotatedCheckboxText);
			sizeNodesBasedOnNumUserGenesAnnotatedCheckbox.setToolTipText(sizeNodesBasedOnNumUserGenesAnnotatedCheckboxToolTip);
			sizeNodesBasedOnNumUserGenesAnnotatedCheckbox.addActionListener(this);
		}
		return sizeNodesBasedOnNumUserGenesAnnotatedCheckbox;
	}
	
	/**
	 * Initializes the widget's subcomponents and layout
	 */
	private void initComponents() {
//		this.setPreferredSize(new Dimension(10,40));
//		this.setBorder(BorderFactory.createTitledBorder("Advanced Settings"));
		this.setLayout(new GridLayout(0,1));
		this.add(getIncludeDeCheckBox());
        this.add(getDefnCheckBox());
		this.add(getSizeNodesBasedOnNumUserGenesAnnotatedCheckbox());
        this.add(getDisplayUserGeneStatisticsCheckBox());
        this.add(getExpandNodesCheckBox());
        this.add(getLCheckBox());
        this.add(getExpandNodeDepthCheckbox());
		this.add(getExpandNodeTextField());
		
	}

	private JCheckBox displayUserGeneStatisticsCheckBox;
	private static final String displayUserGeneStatisticsCheckBoxText = "Calculate Coverage of User Specified Genes";
	private static final String displayUserGeneStatisticsCheckBoxToolTip = 
		     "If checked, the coverage statistics displayed in each GO Namespace's"+
		lsep+"panel will be based on the GO tree's coverage of the genes which the"+
		lsep+"user imported using the 'Import Gene Set' button.  Unchecked, stats "+
		lsep+"are calculated based on the GO tree's coverage of all the Genes in  "+
		lsep+"the applied Gene Annotation File.";
	
	private JCheckBox getDisplayUserGeneStatisticsCheckBox() {
		if (displayUserGeneStatisticsCheckBox == null) {
			displayUserGeneStatisticsCheckBox = new JCheckBoxMod(displayUserGeneStatisticsCheckBoxText);
			displayUserGeneStatisticsCheckBox.setToolTipText(displayUserGeneStatisticsCheckBoxToolTip);
			displayUserGeneStatisticsCheckBox.addActionListener(this);
		}
		return displayUserGeneStatisticsCheckBox;
	}
	




	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src instanceof JCheckBox) {
			if (src == includeDescendentCoverageInNodeSizeCalculationCheckBox) {
				GOSlimmerGUIViewSettings.includeDescendantInferredCoveredGenesInNodeSizeCalculations= includeDescendentCoverageInNodeSizeCalculationCheckBox.isSelected();
				Cytoscape.getVisualMappingManager().applyAppearances();
			}
			else if (src == labelNodesWithOntologyName) {
				GOSlimmerGUIViewSettings.labelNodesWithOntologyName = labelNodesWithOntologyName.isSelected();
			}
            else if (src == showGODefinitionAsToolTip) {
                GOSlimmerGUIViewSettings.showGODefinitionAsToolTip = showGODefinitionAsToolTip.isSelected();
            }
            else if (src == expandNodesWithGenes) {
                GOSlimmerGUIViewSettings.expandNodesWithGenes = expandNodesWithGenes.isSelected();
            }
            else if (src == expandNodeDepthCheckbox) {
				boolean useFiniteExpansionDepth = expandNodeDepthCheckbox.isSelected();
				//only enable the text field for specifying the node expansion depth if the  feature is enabled.
				expandNodeDepthTextField.setEnabled(useFiniteExpansionDepth);
				for(GOSlimmerController controller: controllers) controller.setUseFiniteExpansionDepth(useFiniteExpansionDepth);
			}
			else if (src == sizeNodesBasedOnNumUserGenesAnnotatedCheckbox) {
				GOSlimmerGUIViewSettings.sizeNodesBasedOnUserGeneAnnotation = sizeNodesBasedOnNumUserGenesAnnotatedCheckbox.isSelected();
			}
			else if (src == displayUserGeneStatisticsCheckBox) {
				for(GOSlimmerController controller: controllers) controller.setDisplayUserGeneCoverageStatistics(displayUserGeneStatisticsCheckBox.isSelected());
			}
			CyNetworkView curNet = Cytoscape.getCurrentNetworkView();
			if (curNet!=null) curNet.redrawGraph(false, true);
		}
		else if (src instanceof JTextField) {
			if (src == expandNodeDepthTextField) {
				updateExpansionDepth();
			}
		}
		
	}
	
	{
		initComponents();
	}

	public void propertyChange(PropertyChangeEvent event) {
		Object src = event.getSource();
		if (src instanceof JTextField) {
			if (src == expandNodeDepthTextField) {
				updateExpansionDepth();
			}
		}
	}
	
	/**
	 * Update the controller parameters which determine the depth to expand node's descendant subgraphs to based on the 
	 * value of the expandNodeDepthTextField textbox
	 */
	private void updateExpansionDepth() {
		//retrieve the new expansion depth, and see if it is a valid integer entry:
		int newExpansionDepth;
		try {
			newExpansionDepth = Integer.parseInt(expandNodeDepthTextField.getText());
		}
		catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Invalid value '" + expandNodeDepthTextField.getText() + "' for expansion depth;  expanded node depth must be a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
			for(GOSlimmerController controller: controllers) expandNodeDepthTextField.setText(String.valueOf(controller.getExpansionDepth()));
			return;
		}
		//this next line is only performed if the text was successfully parsed as an integer
		for(GOSlimmerController controller: controllers) controller.setExpansionDepth(newExpansionDepth);
	}

    public void setUserGeneOptions(boolean enableUserGeneOptions) {
        sizeNodesBasedOnNumUserGenesAnnotatedCheckbox.setSelected(enableUserGeneOptions);
        GOSlimmerGUIViewSettings.sizeNodesBasedOnUserGeneAnnotation = enableUserGeneOptions;

        displayUserGeneStatisticsCheckBox.setSelected(enableUserGeneOptions);
        for(GOSlimmerController controller: controllers) {
            controller.setDisplayUserGeneCoverageStatistics(enableUserGeneOptions);
        }
        Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
    }
	
}
