package org.ccbr.bader.yeast.view.gui;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.GOSlimmer;
import org.ccbr.bader.yeast.GOSlimmerUtil;
import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.export.GeneAnnotationRemapWriter;

import cytoscape.CyNetwork;

public class FileExportPanel extends JPanel implements ActionListener {

	private Collection<GOSlimmerController> controllers;

	public FileExportPanel(Collection<GOSlimmerController> controllers) {
		this.controllers = controllers;
	}

	{
		initComponents();
	}
	
	private void initComponents() {
		this.add(getExportToFileButton());
	}
	
	JButton exportToFileButton;
	private static final String exportToFileButtonText = "Export Remapped Gene Association File";
	private static final String exportToFileButtonToolTip = "A new version of the currently set gene annotation file will be " +
			" created where the GO Terms for unselected entries will be remapped to ancestor terms which were selected." +
			" Note that only terms which exist within this GO tree will be exported or remapped;  all others will be ommitted from the output.";
	
	private JButton getExportToFileButton() {
		if (exportToFileButton ==null) {
			exportToFileButton = new JButton(exportToFileButtonText);
			exportToFileButton.addActionListener(this);
			exportToFileButton.setToolTipText(exportToFileButtonToolTip);
		}
		return exportToFileButton;
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src instanceof JButton) {
			if (src == exportToFileButton) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int retval = chooser.showSaveDialog(this);
				if (retval == JFileChooser.APPROVE_OPTION) {
					File exportFile = chooser.getSelectedFile();
					Map<String,String> goTermRemap = new HashMap<String, String>();
					for(GOSlimmerController controller: controllers) {
						goTermRemap.putAll(GOSlimmerUtil.createGoTermRemap(controller.getNetwork()));
						
					}
					try {
						//make sure an annotation file has been loaded
						if (GOSlimmer.geneAssociationReader==null) {
							JOptionPane.showMessageDialog(this,"You must load an annotation file first","Error",JOptionPane.ERROR_MESSAGE);
							return;
						}
						createRemappedGeneAnnotationFile(GOSlimmer.geneAssociationReader, exportFile, goTermRemap);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(this,"Failed to create remapped Gene Annotation File due to IO Error: "+ e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					}
				}
				
			}
		}
		
	}

	private void createRemappedGeneAnnotationFile(GeneAssociationReaderUtil originalFileReader, File remapFile,Map<String,String> goTermRemap) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(remapFile));
		GeneAnnotationRemapWriter gaw = new GeneAnnotationRemapWriter(w,goTermRemap);
		//create header
		gaw.write("!This Gene Annotation File contains entries which have been remapped onto a smaller set of GO terms");
		Collection<String[]> entries = originalFileReader.getAnnotationEntries();
		for (String[] entry:entries) {
			gaw.writeRemappedEntry(entry);
		}
		w.close();
	}
	
}
