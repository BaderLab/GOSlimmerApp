package org.ccbr.bader.yeast.view.gui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.GONamespace;
import org.ccbr.bader.yeast.GOSlimmer;
import org.ccbr.bader.yeast.GOSlimmerSession;
import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.export.GOFormatException;

import cytoscape.Cytoscape;
import cytoscape.bookmarks.Bookmarks;
import cytoscape.bookmarks.DataSource;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.util.TaskManager;
import cytoscape.util.BookmarksUtil;

//public class GOSlimmerGeneAssociationDialog extends JDialog implements ActionListener {
public class GOSlimmerGeneAssociationDialog extends JPanel implements ActionListener {
	
	Map<GONamespace, GOSlimmerController> namespaceToController;

	private String ontologyName;
	private GOSlimmerSession session;
	
	
	public GOSlimmerGeneAssociationDialog(Map<GONamespace, GOSlimmerController> namespaceToController,String ontologyName,GOSlimmerSession session) throws HeadlessException {
		super();
		this.namespaceToController = namespaceToController;
		this.ontologyName = ontologyName;
		this.session = session;
	}

	private Map<String,String> annotationURLMap = new HashMap<String, String>();
	
	{
		
		initComponents();
	}
	
	private void initComponents()  {
		this.setBorder(BorderFactory.createTitledBorder("Select Gene Association Data to use for Coverage Calculation"));
		
		this.setLayout(new BorderLayout());
//		this.setLayout(new GridLayout(0,1));
//			this.setLayout(new FlowLayout());
//		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
//		this.add(getAnnotationComboBox(),BorderLayout.WEST);
//		this.add(getAnnotationBrowseButton(),BorderLayout.EAST);
//		this.add(getSelectedAnnotationFileLabel(), BorderLayout.NORTH);
//		this.add(getApplyButton(),BorderLayout.PAGE_END);
//		this.add(getAnnotationComboBox());
//		this.add(getAnnotationBrowseButton());
		this.add(getSelectedAnnotationFileLabel(),BorderLayout.NORTH);
		this.add(getSelectionPanel(),BorderLayout.CENTER);
//		this.add(getAnnotationComboBox());
//		this.add(getAnnotationBrowseButton());
		this.add(getApplyButton(),BorderLayout.SOUTH);
	}
	
	private JPanel selectionPanel;
	private JPanel getSelectionPanel() {
		if (selectionPanel == null) {
			selectionPanel = new JPanel();
//			selectionPanel.setLayout(new GridLayout(1,2));
			selectionPanel.setLayout(new FlowLayout());
//			selectionPanel.setComponentOrientation(ComponentOrientation.))));
			selectionPanel.add(getAnnotationComboBox());
			selectionPanel.add(getAnnotationBrowseButton());
		}
		return selectionPanel;
	}
	
	JButton applyButton;
	
	private static final String applyButtonText = "Apply To Graphs";
	
	private JButton getApplyButton() {
		if (applyButton !=null) return applyButton;
		
		applyButton = new JButton(applyButtonText);
		applyButton.addActionListener(this);
		int height = applyButton.getHeight();
//		applyButton.setMaximumSize(new Dimension(height*6, height));
		return applyButton;
	}
	
	JComboBox annotationComboBox;
	
	JButton annotationBrowseButton;
	
	private JButton getAnnotationBrowseButton() {
		if (annotationBrowseButton!=null) return annotationBrowseButton;
		annotationBrowseButton = new JButton(browseButtonText);
		
		annotationBrowseButton.addActionListener(this);
		
		return annotationBrowseButton;
	}
	
	JLabel selectedAnnotationFileLabel;
	
	
	
	//TODO inform developers that they should use the covert exceptions design pattern and not throw a jaxbexception, since this method should abstract above the details of xml parsing
	private JComboBox getAnnotationComboBox()  {
		if (annotationComboBox !=null) return annotationComboBox;
		
		//create the new combo box
		annotationComboBox = new JComboBox();
		annotationComboBox.setMaximumSize(new Dimension(60,15));
		
		
		//retrieve the gene association annotations from the bookmarks library
		Bookmarks bookmarks=null;
		try {
			bookmarks = Cytoscape.getBookmarks();
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to retrieve cytoscape bookmarks to determine gene annotation options",e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to retrieve cytoscape bookmarks to determine gene annotation options",e);
		}
		List<DataSource> annotations = BookmarksUtil.getDataSourceList("annotation", bookmarks.getCategory());
		
		//intialize the annotation to url map, and populate the combo box with the annotation names
		for(DataSource annot:  annotations) {
			String annotName = annot.getName();
			annotationURLMap.put(annotName, annot.getHref());
			annotationComboBox.addItem(annotName);
			//TODO see if we need to extract the source attributes
		}
		annotationComboBox.addActionListener(this);
		return annotationComboBox;
	}
	
	private JLabel getSelectedAnnotationFileLabel() {
		if (selectedAnnotationFileLabel !=null) return selectedAnnotationFileLabel;
		
		//initialize the JLabel
		selectedAnnotationFileLabel = new JLabel();
		int height = selectedAnnotationFileLabel.getHeight();
//		selectedAnnotationFileLabel.setSize(height*6,height);
		
		updateSelectedAnnotationFileLabelText();
		return selectedAnnotationFileLabel;
	}
	
	/**Note:  this expects that the selected annotation file label has already been initilized
	 * 
	 */
	private void updateSelectedAnnotationFileLabelText() {
		//determine the proper text of the label
		//if user specified file is true, then use the userselectedfile name
		String comboBoxSelection = null;
		if (useUserSpecifiedFile) {
			//TODO consider displaying the absolute path to the file
			comboBoxSelection = userSelectedFile.getName();
		}
		else { //otherwise, use the combo box selection
			comboBoxSelection = (String) (getAnnotationComboBox().getSelectedItem());
		}
		selectedAnnotationFileLabel.setText("Selected Annotation File: " + comboBoxSelection);
	}
	
	private File userSelectedFile;
	private boolean useUserSpecifiedFile = false;
	
	private static final String browseButtonText = "Browse";
	
	public void actionPerformed(ActionEvent event) {
		//TODO consider reimplementing this to use object reference equivalence checks instead of text value checks, since we might want to have multiple 'browse' buttons
		if (event.getSource() instanceof JButton)  {
			JButton bSource = (JButton) event.getSource();
			if (bSource.getText().equals(browseButtonText)) {
				//TODO popup filechooser dialog
				JFileChooser annotFileChooser = new JFileChooser();
				int retVal = annotFileChooser.showOpenDialog(this);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					userSelectedFile = annotFileChooser.getSelectedFile();
					useUserSpecifiedFile = true;
					//TODO modify view label which displays identity of selected gene association file
					updateSelectedAnnotationFileLabelText();
				}
				
			}
			else if (bSource.getText().equals(applyButtonText)) {
				//TODO apply the selected annotation to the go dags
				System.out.println("Apply button pressed");
				
				TaskManager.executeTask(new Task() {

					public String getTitle() {
						// TODO Auto-generated method stub
						return "Retrieving and applying Gene Annotation Data";
					}

					public void halt() {
						// TODO Auto-generated method stub
						
					}

					public void run() {
						try {
							applySelectedAnnotationToGOGraphs();
						} catch (FileNotFoundException e) {
							JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Failed to apply gene annotation data because File could not be found: " + e.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
						} catch (MalformedURLException e) {
							JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Failed to apply gene annotation data because URL is not valid: " + e.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
						} catch (GOFormatException e) {
							JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Failed to apply gene annotation data because annotation file is not valid: " + e.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
						}
						
					}

					public void setTaskMonitor(TaskMonitor arg0) throws IllegalThreadStateException {
						// TODO Auto-generated method stub
						
					}
					
				}, null);

			}
			else {
				throw new RuntimeException("Unrecognized button press event received: " + event.getSource());
			}
		}
		else if (event.getSource() instanceof JComboBox) { //annotation combo box has been changed
			if (event.getSource() == annotationComboBox) { 
				useUserSpecifiedFile = false;  //use the combo box selection as the selected annotation
				updateSelectedAnnotationFileLabelText(); //update the view with the new selection
			}
		}
		else {
			throw new RuntimeException("Unrecognized event received: " + event.getSource());
		}
	}

	/**This method will apply the currently selected gene association annotation file's data 
	 * to the 3 GO graphs, by manipulating the controller of each respective namespaces GO graph, 
	 * as stored in namespaceToController.
	 * @throws FileNotFoundException 
	 * @throws MalformedURLException 
	 * @throws GOFormatException 
	 * 
	 */
	private void applySelectedAnnotationToGOGraphs() throws FileNotFoundException, MalformedURLException, GOFormatException {
		//construct the annotation url, whether it is remote or local
		URL annotURL = getSelectedAnnotationURL();
		GeneAssociationReaderUtil garu;
		//process the annotation file
		try {
			garu = new GeneAssociationReaderUtil(ontologyName,annotURL,"GO:ID");
			garu.readTable();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Failed to access gene association file",e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to access gene association file",e);
		} catch (GOFormatException e) {
			throw new GOFormatException("Failed to parse Gene Annotation file because it is not a valid Gene Annotation File",e);
		}
		
//		retrieve the mapping of GOIDs to GeneIDs from the parsed asssociation file;  this will be used to assign directly and indirectlly inferred attributes to the networks
		Map<String,List<String>> goIdToGeneIdMap = garu.getGOIDToGeneIDMap();
		
		for(GONamespace ns: namespaceToController.keySet()) {
			GOSlimmerController controller = namespaceToController.get(ns);
			Set<String> nsGeneIds = garu.getNamespaceGeneIds(ns);
			//delete the coverage attributes;  TODO do this globally instead of locally
			controller.removeCoverageAttributes();
			//assign the new coverage attributes to the nodes of the network based on this goIdToGeneIdMap
			controller.assignCoverageAttributesToNetworks(goIdToGeneIdMap);
			//reinitialize the statistics been with the new gene association data
			controller.resetAndRecalculateStatisticsBean(nsGeneIds.size());
			controller.getNetworkView().applyVizmapper(Cytoscape.getVisualMappingManager().getVisualStyle());
			//TODO either unselect all nodes, or recalculate the statistics based on the new coverage attributes
		}
		
		//record the gene association reader in the GOSlimmer static field, since it will be needed when exporting remapped versions of the file
		//TODO store the gene association reader in a more model centric place
		session.setGaru(garu);
	}
	
	private URL getSelectedAnnotationURL() throws MalformedURLException {
		//construct the annotation url, whether it is remote or local
		URL annotURL = null;
		if (useUserSpecifiedFile) {
			annotURL =  userSelectedFile.toURL();
		}
		else {
			annotURL = new URL(annotationURLMap.get((String) annotationComboBox.getSelectedItem()));
		}
		return annotURL;
	}
	

}
