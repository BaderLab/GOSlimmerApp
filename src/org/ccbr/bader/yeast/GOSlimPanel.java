package org.ccbr.bader.yeast;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ccbr.bader.yeast.controller.GOSlimmerController;

public class GOSlimPanel extends JPanel {

	private GOSlimmerControlPanelToGraphInterface cptgi = null;
	
	private JLabel celComCoverage;
	private JLabel bioProCoverage;
	private JLabel molFunCoverage;
	
	public GOSlimPanel() {
		super();
		super.setName("GOSlimmer");
//		this.setLayout(new FlowLayout(FlowLayout.LEADING,1,1));
		this.setLayout(new GridLayout(8,1));
//		this.setLayout(new BorderLayout());
//		JButton recalculateButton = new JButton("Recalculate");
//		this.add(recalculateButton);
//		recalculateButton.addActionListener(cptgi);
		
//		JLabel molFunSlimCoverageLabel = new JLabel("MOLECULAR.FUNCTION Slim Coverage:");
//		this.add(molFunSlimCoverageLabel);
//		JLabel bioProSlimCoverageLabel = new JLabel("BIOLOGICAL.PROCESS Slim Coverage:");
//		this.add(molFunSlimCoverageLabel);
//		JLabel celComSlimCoverageLabel = new JLabel("CELLULAR.COMPONENT Slim Coverage:");
//		this.add(molFunSlimCoverageLabel);
		//molFunSlimCoverageLabel
		JPanel molFunPanel = new JPanel(new GridLayout(1,2));
		molFunPanel.add(new JLabel("MOLECULAR.FUNCTION Slim Coverage:"));
		molFunCoverage = new JLabel("0");
		molFunPanel.add(molFunCoverage);
		this.add(molFunPanel);
//		this.add(molFunPanel,BorderLayout.NORTH);
		
		JPanel bioProPanel = new JPanel(new GridLayout(1,2));
		bioProPanel.add(new JLabel("BIOLOGICAL.PROCESS Slim Coverage:"));
		bioProCoverage = new JLabel("0");
		bioProPanel.add(bioProCoverage);
		this.add(bioProPanel);
//		this.add(bioProPanel, BorderLayout.CENTER);
		
		JPanel celComPanel = new JPanel(new GridLayout(1,2));
		celComPanel.add(new JLabel("CELLULAR.COMPONENT Slim Coverage:"));
		celComCoverage = new JLabel("0");
		celComPanel.add(celComCoverage);
		this.add(celComPanel);
//		this.add(celComPanel,BorderLayout.SOUTH);
		
//		this.setSize(molFunPanel.getSize());
		
		
	}

	public GOSlimPanel(GOSlimmerController molFunController, GOSlimmerController bioProController, GOSlimmerController celComController) {
		this();
		
	}
	
	private class GOSlimmerNamespaceSubpanel {
		
		public GOSlimmerNamespaceSubpanel(String name,GOSlimmerController controller) {
			
		}
		
	}
	
	

	public JLabel getBioProCoverage() {
		return bioProCoverage;
	}

	public void setBioProCoverage(JLabel bioProCoverage) {
		this.bioProCoverage = bioProCoverage;
	}

	public JLabel getCelComCoverage() {
		return celComCoverage;
	}

	public void setCelComCoverage(JLabel celComCoverage) {
		this.celComCoverage = celComCoverage;
	}

	public JLabel getMolFunCoverage() {
		return molFunCoverage;
	}

	public void setMolFunCoverage(JLabel molFunCoverage) {
		this.molFunCoverage = molFunCoverage;
	}
	
}
