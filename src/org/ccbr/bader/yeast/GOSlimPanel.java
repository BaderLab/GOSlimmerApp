package org.ccbr.bader.yeast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;

public class GOSlimPanel extends JPanel {

	private GOSlimmerControlPanelToGraphInterface cptgi = null;
	
//	private JLabel celComCoverage;
//	private JLabel bioProCoverage;
//	private JLabel molFunCoverage;
//	
//	GOSlimmerNamespaceSubpanel molFunSubPanel = null;
//	GOSlimmerNamespaceSubpanel bioProSubPanel = null;
//	GOSlimmerNamespaceSubpanel celComSubPanel = null;
//	
//	public GOSlimPanel(GOSlimmerController molFunController, GOSlimmerController bioProController, GOSlimmerController celComController) {
//		super();
//		super.setName("GOSlimmer");
////		this.setLayout(new FlowLayout(FlowLayout.LEADING,1,1));
//		this.setLayout(new GridLayout(8,1));
//		
//		molFunSubPanel = new GOSlimPanel.GOSlimmerNamespaceSubpanel(GONamespace.MolFun.getName() + " Coverage",molFunController);
//		bioProSubPanel = new GOSlimPanel.GOSlimmerNamespaceSubpanel("BioPro",bioProController);
//		celComSubPanel = new GOSlimPanel.GOSlimmerNamespaceSubpanel("CelCom",celComController);
//		this.add(molFunSubPanel);
//		this.add(bioProSubPanel);
//		this.add(celComSubPanel);
//		
//
//		
////		this.add(new GOSlimPanel.GOSlimmerNamespaceSubpanel("MolFun",molFunController));
////		this.add(new GOSlimPanel.GOSlimmerNamespaceSubpanel("BioPro",bioProController));
////		this.add(new GOSlimPanel.GOSlimmerNamespaceSubpanel("CelCom",celComController));
//	}
	
	public GOSlimPanel(final Map<GONamespace,GOSlimmerController> namespaceToController) {
		super();
		super.setName("GOSlimmer");
		this.setLayout(new GridLayout(0,1));
		for(GONamespace namespace: namespaceToController.keySet()) {
			GOSlimmerController controller = namespaceToController.get(namespace);
			//GOSlimmerNamespaceSubpanel namespaceSubPanel = new GOSlimmerNamespaceSubpanel(namespace.getName() + " Coverage: ",controller);
			GOSlimmerNamespaceSubpanel namespaceSubPanel = new GOSlimmerNamespaceSubpanel(namespace.getName(),controller);
			namespaceToSubpanel.put(namespace,namespaceSubPanel);
			controller.setInferredCoverageStatisticViewLabel(namespaceSubPanel.inferredCoverageStatisticLabel);
			controller.setDirectCoverageStatisticViewLabel(namespaceSubPanel.directCoverageStatisticLabel);
			this.add(namespaceSubPanel); //TODO revise this so that the panels are added in a fixed order
		}
		this.namespaceToController = namespaceToController;
	}

	Map<GONamespace,GOSlimmerNamespaceSubpanel> namespaceToSubpanel = new HashMap<GONamespace, GOSlimmerNamespaceSubpanel>();
	Map<GONamespace,GOSlimmerController> namespaceToController = new HashMap<GONamespace, GOSlimmerController>();
	
	private static final DecimalFormat coverageTextformatter = new DecimalFormat("00.00%");
	
	private class GOSlimmerNamespaceSubpanel extends JPanel{
		
		GOSlimmerCoverageStatBean statBean = null;
		
		JLabel inferredCoverageStatisticLabel;
		JLabel directCoverageStatisticLabel;
		
		String inferredCoverageStatisticLabelToolTip = "The percentage of gene annotation file genes covered directly by the GO terms selected for inclusion in the slim set, as well as those genes annotated by descendant terms within this tree(whether expanded or collapsed)";
		String directCoverageStatisticLabelToolTip = "The percentage of gene annotation file genes covered directly by the GO terms explicitely selected for inclusion in the slim set";
		
		private NumberFormat numFormatter = new DecimalFormat("00.00%");
		
		public GOSlimmerNamespaceSubpanel(String name,final GOSlimmerController controller) {
			this.setBorder(BorderFactory.createTitledBorder(name));
			
			//this.add(new JLabel(name));
//			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			this.setLayout(new GridLayout(0,1));
			this.statBean = controller.getStatBean();
			//String coverageStatisticText = String.v statBean.fractionCovered();
			//this.coverageStatisticLabel = new JLabel(numFormatter.format(statBean.fractionCovered())){
			
			this.inferredCoverageStatisticLabel = new JLabel("Inferred Coverage: " + numFormatter.format(statBean.fractionInferredCovered()));
			inferredCoverageStatisticLabel.setToolTipText(inferredCoverageStatisticLabelToolTip);
			this.add(inferredCoverageStatisticLabel);
			
			this.directCoverageStatisticLabel = new JLabel("Direct Coverage: " + numFormatter.format(statBean.fractionDirectlyCovered()));
			directCoverageStatisticLabel.setToolTipText(directCoverageStatisticLabelToolTip);
			this.add(directCoverageStatisticLabel);
//			this.addMouseListener(new MouseListener() {
//
//				public void mouseClicked(MouseEvent e) {
//					controller.getNetworkViewFocus();
//					
//				}
//
//				public void mouseEntered(MouseEvent e) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				public void mouseExited(MouseEvent e) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				public void mousePressed(MouseEvent e) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				public void mouseReleased(MouseEvent e) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//			});
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
	
}
