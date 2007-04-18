package org.ccbr.bader.yeast.view.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.view.gui.misc.JCheckBoxMod;

import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;

public class AdvancedViewSettingsPanel extends JPanel implements ActionListener {

	private Collection<GOSlimmerController> controllers;

	public AdvancedViewSettingsPanel(Collection<GOSlimmerController> controllers) {
		super();
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
			expandNodeDepthTextField = new JTextField("1");
			expandNodeDepthTextField.setEnabled(expandNodeDepthCheckbox.isSelected());
			expandNodeDepthTextField.addActionListener(this);
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
	
	private void initComponents() {
//		this.setPreferredSize(new Dimension(10,40));
		this.setBorder(BorderFactory.createTitledBorder("Advanced View Settings"));
		this.setLayout(new GridLayout(0,1));
		this.add(getIncludeDeCheckBox());
		this.add(getSizeNodesBasedOnNumUserGenesAnnotatedCheckbox());
		this.add(getLCheckBox());
		this.add(getExpandNodeDepthCheckbox());
		this.add(getExpandNodeTextField());
		
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
			else if (src == expandNodeDepthCheckbox) {
				boolean useFiniteExpansionDepth = expandNodeDepthCheckbox.isSelected();
				//only enable the text field for specifying the node expansion depth if the  feature is enabled.
				expandNodeDepthTextField.setEnabled(useFiniteExpansionDepth);
				for(GOSlimmerController controller: controllers) controller.setUseFiniteExpansionDepth(useFiniteExpansionDepth);
			}
			else if (src == sizeNodesBasedOnNumUserGenesAnnotatedCheckbox) {
				GOSlimmerGUIViewSettings.sizeNodesBasedOnUserGeneAnnotation = sizeNodesBasedOnNumUserGenesAnnotatedCheckbox.isSelected();
			}
			CyNetworkView curNet = Cytoscape.getCurrentNetworkView();
			if (curNet!=null) curNet.redrawGraph(false, true);
		}
		else if (src instanceof JTextField) {
			if (src == expandNodeDepthTextField) {
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
		}
		
	}
	
	{
		initComponents();
	}
	
}
