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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.GOSlimmer;
import org.ccbr.bader.yeast.GOSlimmerException;
import org.ccbr.bader.yeast.GOSlimmerSession;
import org.ccbr.bader.yeast.GOSlimmerUtil;
import org.ccbr.bader.yeast.controller.GOSlimmerController;
import org.ccbr.bader.yeast.export.GeneAnnotationRemapWriter;
import org.ccbr.bader.yeast.view.gui.misc.JButtonMod;

import cytoscape.CyNetwork;

public class FileExportPanel extends JPanel implements ActionListener {

	private Collection<GOSlimmerController> controllers;

	private GOSlimmerSession session;
	
	public FileExportPanel(Collection<GOSlimmerController> controllers,GOSlimmerSession session) {
		this.controllers = controllers;
		this.session=session;
	}

	{
		initComponents();
	}
	
	private void initComponents() {
		this.setBorder(BorderFactory.createTitledBorder("Export"));
		this.add(getExportToFileButton());
	}
	
	private static final String lsep = System.getProperty("line.separator");
	
	JButton exportToFileButton;
	private static final String exportToFileButtonText = "Export Remapped Gene Association File";
	private static final String exportToFileButtonToolTip = 
		     "A new version of the imported gene annotation file will be " +
		lsep+" created where the GO Terms for unselected entries will be " +
		lsep+"remapped to ancestor terms which were selected.  Note that " +
		lsep+"only terms which exist within this GO tree will be exported" +
		lsep+"or remapped;  all others will be ommitted from the output.";
	
	private JButton getExportToFileButton() {
		if (exportToFileButton ==null) {
			exportToFileButton = new JButtonMod(exportToFileButtonText);
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
						try {
							goTermRemap.putAll(GOSlimmerUtil.createGoTermRemap(controller.getNetwork()));
						} catch (GOSlimmerException e) {
							JOptionPane.showMessageDialog(this,"Failed to remap terms due to exception: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							return;
						}
						
					}
					try {
						//make sure an annotation file has been loaded
						if (session.getGaru()==null) {
							JOptionPane.showMessageDialog(this,"You must load an annotation file first","Error",JOptionPane.ERROR_MESSAGE);
							return;
						}
						createRemappedGeneAnnotationFile(session.getGaru(), exportFile, goTermRemap);
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
