package org.ccbr.bader.yeast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;

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
		//molFunPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK,1));;
		this.add(molFunPanel);
		//this.add(molFunPanel,BorderLayout.NORTH);
		
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

	
	
	GOSlimmerNamespaceSubpanel molFunSubPanel = null;
	GOSlimmerNamespaceSubpanel bioProSubPanel = null;
	GOSlimmerNamespaceSubpanel celComSubPanel = null;
	
	public GOSlimPanel(GOSlimmerController molFunController, GOSlimmerController bioProController, GOSlimmerController celComController) {
		super();
		super.setName("GOSlimmer");
//		this.setLayout(new FlowLayout(FlowLayout.LEADING,1,1));
		this.setLayout(new GridLayout(8,1));
		
		molFunSubPanel = new GOSlimPanel.GOSlimmerNamespaceSubpanel(GONamespace.MolFun.name + " Coverage",molFunController);
		bioProSubPanel = new GOSlimPanel.GOSlimmerNamespaceSubpanel("BioPro",bioProController);
		celComSubPanel = new GOSlimPanel.GOSlimmerNamespaceSubpanel("CelCom",celComController);
		this.add(molFunSubPanel);
		this.add(bioProSubPanel);
		this.add(celComSubPanel);
		

		
//		this.add(new GOSlimPanel.GOSlimmerNamespaceSubpanel("MolFun",molFunController));
//		this.add(new GOSlimPanel.GOSlimmerNamespaceSubpanel("BioPro",bioProController));
//		this.add(new GOSlimPanel.GOSlimmerNamespaceSubpanel("CelCom",celComController));
	}
	
	//public GOSlimPanel(Map<GONa>)

	Map<GONamespace,GOSlimmerNamespaceSubpanel> namespaceToSubpanel = new HashMap<GONamespace, GOSlimmerNamespaceSubpanel>();
	Map<GONamespace,GOSlimmerController> namespaceController = new HashMap<GONamespace, GOSlimmerController>();
	
	public enum GONamespace{
		MolFun("Molecular Function"),
		BioPro("Biological Process"),
		CelCom("Cellular Component");
		
		private String name;
		
		GONamespace(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	
	}
	
	private static final DecimalFormat coverageTextformatter = new DecimalFormat("00.00%");
	
	private class GOSlimmerNamespaceSubpanel extends JPanel{
		
		GOSlimmerCoverageStatBean statBean = null;
		
		JLabel coverageStatisticLabel;
		
		private NumberFormat numFormatter = new DecimalFormat("00.00%");
		
		public GOSlimmerNamespaceSubpanel(String name,GOSlimmerController controller) {
			this.add(new JLabel(name));
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			this.statBean = controller.getStatBean();
			//String coverageStatisticText = String.v statBean.fractionCovered();
			this.coverageStatisticLabel = new JLabel(numFormatter.format(statBean.fractionCovered())){
				
				@Override
				public void setText(String arg0) {
					// TODO Auto-generated method stub
					super.setText(coverageTextformatter.format(arg0));
				}
				
			};
			this.add(coverageStatisticLabel);
			
		}

		public JLabel getCoverageStatisticLabel() {
			return coverageStatisticLabel;
		}

		public void setCoverageStatisticLabel(JLabel coverageStatisticLabel) {
			this.coverageStatisticLabel = coverageStatisticLabel;
		}
		

		
	}
	
	
	
	

	public JLabel getBioProCoverage() {
		return bioProSubPanel.getCoverageStatisticLabel();
	}

	public void setBioProCoverage(JLabel bioProCoverage) {
		this.bioProSubPanel.setCoverageStatisticLabel(bioProCoverage);
	}

	public JLabel getCelComCoverage() {
		return celComSubPanel.getCoverageStatisticLabel();
	}

	public void setCelComCoverage(JLabel celComCoverage) {
		this.celComSubPanel.setCoverageStatisticLabel(celComCoverage);
	}

	public JLabel getMolFunCoverage() {
		return molFunSubPanel.getCoverageStatisticLabel();
	}

	public void setMolFunCoverage(JLabel molFunCoverage) {
		this.molFunSubPanel.setCoverageStatisticLabel(molFunCoverage);
	}
	
}
