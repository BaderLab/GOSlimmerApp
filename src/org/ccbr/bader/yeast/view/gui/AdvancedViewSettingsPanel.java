package org.ccbr.bader.yeast.view.gui;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cytoscape.Cytoscape;

public class AdvancedViewSettingsPanel extends JPanel implements ActionListener {

	public AdvancedViewSettingsPanel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AdvancedViewSettingsPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public AdvancedViewSettingsPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public AdvancedViewSettingsPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	{
		initComponents();
	}
	
	JLabel includeDescendentCoverageInNodeSizeCalculationLabel;
	JCheckBox includeDescendentCoverageInNodeSizeCalculationCheckBox;
	private final String includeDescendentCoverageInNodeSizeCalculationText = "Include child nodes when calculating node size";
	
	private JCheckBox getIncludeDeCheckBox() {
		if (includeDescendentCoverageInNodeSizeCalculationCheckBox==null) {
			includeDescendentCoverageInNodeSizeCalculationCheckBox = new JCheckBox(includeDescendentCoverageInNodeSizeCalculationText);
			includeDescendentCoverageInNodeSizeCalculationCheckBox.setToolTipText("If checked, then the size of the node will be proportional to the number of genes"
					+ " directly annotated by the node + the number of genes annotated by all of the nodes descendants in the graph. "
					+ " If unchecked, size is proportional to the directly annotated nodes only.");
			includeDescendentCoverageInNodeSizeCalculationCheckBox.addActionListener(this);
		}
		return includeDescendentCoverageInNodeSizeCalculationCheckBox;
	}
	
	private void initComponents() {
		this.setBorder(BorderFactory.createTitledBorder("Advanced View Settings"));
		this.add(getIncludeDeCheckBox());
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src instanceof JCheckBox) {
			if (src == includeDescendentCoverageInNodeSizeCalculationCheckBox) {
				GOSlimmerGUIViewSettings.includeDescendantInferredCoveredGenesInNodeSizeCalculations= includeDescendentCoverageInNodeSizeCalculationCheckBox.isSelected();
				Cytoscape.getVisualMappingManager().applyAppearances();
			}
		}
		
	}
	
}
