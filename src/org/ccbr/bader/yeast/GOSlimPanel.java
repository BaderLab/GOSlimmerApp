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

import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;

public class GOSlimPanel extends JPanel {

	protected AdvancedViewSettingsPanel viewSettingsPanel;
	
	private FileExportPanel fileExportPanel;
	
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
	
	private GOSlimmerSession session;
	
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
			this.add(namespaceSubPanel); //TODO revise this so that the panels are added in a fixed order
		}
		this.namespaceToController = namespaceToController;
		viewSettingsPanel = new AdvancedViewSettingsPanel(session.getNamespaceToController().values());
		this.add(viewSettingsPanel);
		
		this.add(getFileExportPanel());
		

		session.setGOSlimPanel(this);
	}
	
	private FileExportPanel getFileExportPanel() {
		if (fileExportPanel ==null) {
			fileExportPanel = new FileExportPanel(this.session.getNamespaceToController().values(),this.session);
		}
		return fileExportPanel;
	}

	private UserGeneSetImportPanel userGeneSetImportPanel;
	
	public UserGeneSetImportPanel getUserGeneSetImportPanel() {
		if (userGeneSetImportPanel == null) {
			userGeneSetImportPanel = new UserGeneSetImportPanel(session);
			userGeneSetImportPanel.setLayout(new GridLayout(0,1));
		}
		return userGeneSetImportPanel;
	}
	
	Map<GONamespace,GOSlimmerNamespaceSubpanel> namespaceToSubpanel = new HashMap<GONamespace, GOSlimmerNamespaceSubpanel>();
	Map<GONamespace,GOSlimmerController> namespaceToController = new HashMap<GONamespace, GOSlimmerController>();
	
	private static final DecimalFormat coverageTextformatter = new DecimalFormat("00.00%");
	
	private class GOSlimmerNamespaceSubpanel extends JCollapsablePanel{
		
		GOSlimmerCoverageStatBean statBean = null;
		
		JLabel inferredCoverageStatisticLabel;
		JLabel directCoverageStatisticLabel;
		
		private final String lsep = System.getProperty("line.separator");
		
		String inferredCoverageStatisticLabelToolTip =         "The percentage of gene annotation file genes covered directly " +
														lsep + "by the GO terms selected for inclusion in the slim set, as " +
														lsep + "well as those genes annotated by descendant terms within this " +
														lsep + "tree(whether expanded or collapsed)";
		String directCoverageStatisticLabelToolTip =            "The percentage of gene annotation file genes covered directly " +
														lsep +  "by the GO terms explicitely selected for inclusion in the slim set";
		
		private NumberFormat numFormatter = new DecimalFormat("00.00%");
		
		public GOSlimmerNamespaceSubpanel(String name,final GOSlimmerController controller) {
			super(name);
//			this.setBorder(BorderFactory.createTitledBorder(name));
			
			//this.add(new JLabel(name));
//			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			this.setLayout(new GridLayout(0,1));
			this.statBean = controller.getStatBean();
			//String coverageStatisticText = String.v statBean.fractionCovered();
			//this.coverageStatisticLabel = new JLabel(numFormatter.format(statBean.fractionCovered())){
			
			this.inferredCoverageStatisticLabel = new JLabelMod("Inferred Coverage: " + numFormatter.format(statBean.fractionInferredCovered()));
			inferredCoverageStatisticLabel.setToolTipText(inferredCoverageStatisticLabelToolTip);
			this.add(inferredCoverageStatisticLabel);
			
			this.directCoverageStatisticLabel = new JLabelMod("Direct Coverage: " + numFormatter.format(statBean.fractionDirectlyCovered()));
			directCoverageStatisticLabel.setToolTipText(directCoverageStatisticLabelToolTip);
			this.add(directCoverageStatisticLabel);
			MouseListener changeFocusMouseListener = new MouseListener() {

				public void mouseClicked(MouseEvent e) {
//					controller.getNetworkViewFocus();
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
	
}
