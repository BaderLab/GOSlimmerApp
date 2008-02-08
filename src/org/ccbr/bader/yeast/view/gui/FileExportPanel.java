/**
 * * Copyright (c) 2007 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * * Research, University of Toronto
 * *
 * * Code written by: Michael Matan
 * * Authors: Michael Matan, Gary D. Bader
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * University of Toronto
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * University of Toronto
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * University of Toronto
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * Description: GUI Widget for exporting remapped annotation files   
 */
package org.ccbr.bader.yeast.view.gui;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
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
import java.util.Set;

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
import org.ccbr.bader.yeast.export.RootNodeNotSelectedException;
import org.ccbr.bader.yeast.view.gui.misc.JButtonMod;

import cytoscape.CyNetwork;

/**GUI Widget for exporting remapped annotation files.  
 * 
 * @author mikematan
 *
 */
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
		//this.setLayout(new BorderLayout());
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        this.add(getExportAnnotationFileButton(),c);

        c.gridx = 0;
        c.gridy = 1;
        this.add(getExportSlimSetFileButton(), c);
	}
	
	private static final String lsep = System.getProperty("line.separator");
	
	JButton exportAnnotationFileButton;
	private static final String exportAnnotationFileButtonText = "Export Remapped Gene Association File";
	private static final String exportAnnotationFileButtonToolTip = 
		     "A new version of the imported gene annotation file will be " +
		lsep+" created where the GO Terms for unselected entries will be " +
		lsep+"remapped to ancestor terms which were selected.  Note that " +
		lsep+"only terms which exist within this GO tree will be exported" +
		lsep+"or remapped;  all others will be ommitted from the output.";
	
	private JButton getExportAnnotationFileButton() {
		if (exportAnnotationFileButton ==null) {
			exportAnnotationFileButton = new JButtonMod(exportAnnotationFileButtonText);
			exportAnnotationFileButton.addActionListener(this);
			exportAnnotationFileButton.setToolTipText(exportAnnotationFileButtonToolTip);
		}
		return exportAnnotationFileButton;
	}
	
	JButton exportSlimSetFileButton;
	private static final String exportSlimSetFileButtonText = "Export Slim Set Term List File";
	private static final String exportSlimSetFileButtonToolTip = 
		     "Create a file containing a newline delimited list of your  " +
		lsep+"selected GO Slim Set terms.";
	
	private JButton getExportSlimSetFileButton() {
		if (exportSlimSetFileButton ==null) {
			exportSlimSetFileButton = new JButtonMod(exportSlimSetFileButtonText);
			exportSlimSetFileButton.addActionListener(this);
			exportSlimSetFileButton.setToolTipText(exportSlimSetFileButtonToolTip);
		}
		return exportSlimSetFileButton;
	}
	
	

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src instanceof JButton) {
			//all export options require choosing a file, so do so before determining which export operation to perform
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int retval = chooser.showSaveDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File exportFile = chooser.getSelectedFile();
				if (src == exportAnnotationFileButton) {
					Map<String,Set<String>> goTermRemap = new HashMap<String, Set<String>>();
					for(GOSlimmerController controller: controllers) {
						try {
							try {
								goTermRemap.putAll(GOSlimmerUtil.createGoTermMultipleRemap(controller.getNetwork()));
							} catch (RootNodeNotSelectedException e) {
								//TODO	
								int rv = JOptionPane.showConfirmDialog(this, "Root node of GO namespace " + controller.getNamespace().getName() + " must be included in slim set for export.  Include root node and continue?", "Warning:  root term not selected", JOptionPane.YES_NO_OPTION);//, arg1)Dialog(this,"Failed to remap terms due to exception: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
								if (rv == JOptionPane.YES_OPTION) {
									//add the root node to the slimset and try the remapping again
									controller.addNodeToSlimSet(GOSlimmerUtil.getRootNode(controller.getNetwork()));
									goTermRemap.putAll(GOSlimmerUtil.createGoTermMultipleRemap(controller.getNetwork()));
								}
								else {
									JOptionPane.showMessageDialog(this, "File export has been aborted");
									break;
								}
							}
							
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
						return;
					}
					
					
				}
				else if (src == exportSlimSetFileButton) {
					if (exportFile.exists()) {
						if (!exportFile.delete()) {
							JOptionPane.showMessageDialog(this, "Failed to overwrite selected export file '" + exportFile.getName() + "'.");
						}
					}
					boolean writeSuccess = true;
					for(GOSlimmerController controller: controllers) {
						try {
							writeSuccess &= controller.appendSlimSetList(exportFile);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(this, "Failed to create file listing selected Slim Set terms due to exception: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					if (!writeSuccess) {
						JOptionPane.showMessageDialog(this, "Failed to create file listing selected Slim Set terms ","Error",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		
	}

	/**Create a modified version of the imported gene annotation file where unselected GO terms are replaced with selected ancestor terms to which they have 
	 * been remapped.
	 * @param originalFileReader the reader used to read the original annotation file, which still contains that file's data
	 * @param remapFile the file to write the remapped annotation data to
	 * @param goTermRemap the map from unselected GO terms to the selected GO terms which they are to be remapped to
	 * @throws IOException
	 */
	private void createRemappedGeneAnnotationFile(GeneAssociationReaderUtil originalFileReader, File remapFile,Map<String,Set<String>> goTermRemap) throws IOException {
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
