package org.ccbr.bader.yeast.view.gui;

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

import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;

public class AdvancedViewSettingsPanel extends JPanel implements ActionListener {

	private Collection<GOSlimmerController> controllers;

	public AdvancedViewSettingsPanel(Collection<GOSlimmerController> controllers) {
		super();
		this.controllers = controllers;
	}
	
	
	
	{
		initComponents();
	}
	
	JCheckBox includeDescendentCoverageInNodeSizeCalculationCheckBox;
	private final String includeDescendentCoverageInNodeSizeCalculationText = "Include child nodes when calculating node size";
	
	private final String lsep = System.getProperty("line.separator");
	
	private JCheckBox getIncludeDeCheckBox() {
		if (includeDescendentCoverageInNodeSizeCalculationCheckBox==null) {
			includeDescendentCoverageInNodeSizeCalculationCheckBox = new JCheckBox(includeDescendentCoverageInNodeSizeCalculationText);
			includeDescendentCoverageInNodeSizeCalculationCheckBox.setToolTipText("If checked, then the size of the node will be proportional to the number of genes"
					+ lsep +" directly annotated by the node + the number of genes annotated by all of the nodes descendants in the graph. "
					+ lsep + " If unchecked, size is proportional to the directly annotated nodes only.");
			includeDescendentCoverageInNodeSizeCalculationCheckBox.addActionListener(this);
		}
		return includeDescendentCoverageInNodeSizeCalculationCheckBox;
	}
	
	JCheckBox labelNodesWithOntologyName;
	private final String labelNodesWithOntologyNameLabel = "Label nodes with Ontology Term Name";
	
	private JCheckBox getLCheckBox() {
		if (labelNodesWithOntologyName==null) {
			labelNodesWithOntologyName = new JCheckBox(labelNodesWithOntologyNameLabel);
			labelNodesWithOntologyName.setToolTipText("If checked, nodes will be labelled with their biologically meaningful ontology term name."
					+ lsep + "  Otherwise, they will be labelled with their GO ID.");
			labelNodesWithOntologyName.addActionListener(this);
		}
		return labelNodesWithOntologyName;
	}
	
	private JCheckBox expandNodeDepthCheckbox;
	private JTextField expandNodeDepthTextField;
	
	private String expandNodeDepthTooltip = "If checked, a node's descendant tree will only be expanded to the specified depth."
		+ lsep + " If uncheck, the full descendant tree will be expanded.";
	
	private String expandNodeDepthCheckboxText = "Expand nodes to specified depth";
	
	private JCheckBox getExpandNodeDepthCheckbox() {
		if (expandNodeDepthCheckbox==null) {
			expandNodeDepthCheckbox = new JCheckBox(expandNodeDepthCheckboxText);
			expandNodeDepthCheckbox.setToolTipText(expandNodeDepthTooltip);
			expandNodeDepthCheckbox.addActionListener(this);
		}
		return expandNodeDepthCheckbox;
	}
	
	
	
	private JTextField getExpandNodeTextField() {
		if (expandNodeDepthTextField == null) {
			expandNodeDepthTextField = new JTextField("1");
			expandNodeDepthTextField.setEnabled(false);
			expandNodeDepthTextField.addActionListener(this);
		}
		return expandNodeDepthTextField;
	}
	
	private void initComponents() {
		this.setBorder(BorderFactory.createTitledBorder("Advanced View Settings"));
		this.add(getIncludeDeCheckBox());
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
	
}
