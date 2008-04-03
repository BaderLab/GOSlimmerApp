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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

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

import cytoscape.task.TaskMonitor;
import cytoscape.task.Task;
import cytoscape.task.util.TaskManager;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.CyNetwork;

import giny.model.Node;
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
		this.setBorder(BorderFactory.createTitledBorder("Import/Export"));
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

        c.gridx = 0;
        c.gridy = 2;
        this.add(getImportSlimSetFileButton(), c);
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

    JButton importSlimSetFileButton;
        private static final String importSlimSetFileButtonText = "Import Slim Set Term List File";
        private static final String importSlimSetFileButtonToolTip =
                 "Import a file containing a newline delimited list of your  " +
            lsep+"selected GO Slim Set terms.";

        private JButton getImportSlimSetFileButton() {
            if (importSlimSetFileButton ==null) {
                importSlimSetFileButton = new JButtonMod(importSlimSetFileButtonText);
                importSlimSetFileButton.addActionListener(this);
                importSlimSetFileButton.setToolTipText(importSlimSetFileButtonToolTip);
            }
            return importSlimSetFileButton;
        }


	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src instanceof JButton) {
			//all export options require choosing a file, so do so before determining which export operation to perform
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (src == importSlimSetFileButton) {
                int retval = chooser.showOpenDialog(this);
                if (retval==JFileChooser.APPROVE_OPTION) {
			        final File slimSetFile = chooser.getSelectedFile();
			        //execute task in background for importing the user gene set.
			        TaskManager.executeTask(new Task() {

                        private TaskMonitor taskMonitor=null;

                        public String getTitle() {
					        return "Importing slim set term list file and building slim set";
				        }

				        public void halt() {

				        }

				        public void run() {
					        try {
						        Collection<String> goTermIds = parseSlimSetFile(slimSetFile);
						        buildSlimSet(goTermIds);
                            } catch (FileNotFoundException e) {
						        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Failed to import slim set;  Could not find specified file","Error",JOptionPane.ERROR_MESSAGE);
						        return;
					        } catch (IOException e) {
						        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Failed to import slim Set;  Error while reading file","Error",JOptionPane.ERROR_MESSAGE);
						        e.printStackTrace();
						        return;
					        }
                            catch (RuntimeException e) {
                                taskMonitor.setException(e, e.getMessage());
                                return;
                            }

                        }

				        public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
					        this.taskMonitor = taskMonitor;

				        }

			        }, null);

		        }

	        }
            else {

                int retval = chooser.showSaveDialog(this);
			    if (retval == JFileChooser.APPROVE_OPTION) {
				    File exportFile = chooser.getSelectedFile();
				    if (src == exportAnnotationFileButton) {
                        // For the file to be re-imported as a gene annotation file, the file name must being with 'gene_association'.
                        // Check if we should prefix the file name with 'gene_association' or not.
                        if (!exportFile.getName().startsWith("gene_association")) {
                            int appendName = JOptionPane.showConfirmDialog(this, "For this file to be re-imported as a gene annotation file" + lsep +
                                "the file name must begin with 'gene_association'." + lsep +
                                "Would you like to prefix your file name by 'gene_association'?", "Confirm File Name", JOptionPane.YES_NO_OPTION);
                            if (appendName==JOptionPane.YES_OPTION) {
                                exportFile = new File(exportFile.getParentFile(),"gene_association_" + exportFile.getName());
                            }

                        }
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
		gaw.write("!This Gene Annotation File contains entries which have been remapped onto a smaller set of GO terms" + lsep);
		Collection<String[]> entries = originalFileReader.getAnnotationEntries();
		for (String[] entry:entries) {
			gaw.writeRemappedEntry(entry);
		}
		w.close();
	}

    /**
     * Parse the slim set list file imported by the user
     *
     * @param slimSetFile the file specified by the user which contains the slim set
     * @return a collection of GO term IDs parsed from the file
     * @throws IOException
     */
    private Collection<String> parseSlimSetFile(File slimSetFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(slimSetFile));
        Collection<String> GOIds = new HashSet<String>();
        String line = null;
        while ((line = in.readLine()) != null) {
            String GOId = parseSlimSetLine(line);
            if (GOId != null) GOIds.add(GOId);
        }
        return GOIds;
    }


    /**
     * Parses a line of the slim set list file.  Line is expected to be a tab delimited line, with the first field containing the GO term ID.  Trailing whitespace is allowed.
     *
     * @param line Line of slim set list file to be parsed
     * @return GO term ID from this line of the slim set file
     */
    private String parseSlimSetLine(String line) {
        if (line == null || line.matches("\\s*")) return null;
        line = line.trim();
        String[] parts = line.split("\t");
        return parts[0];
    }

    /**
     * Builds the slim set from a collection of GO term Ids.
     *
     * @param goTermIds Collection of GO term Ids to be added to the slim set
     */
    private void buildSlimSet(Collection<String> goTermIds) {
        Collection<String> remainingGOIds = new HashSet<String>(goTermIds);

        for (GOSlimmerController controller:controllers) {
            CyNetwork network = controller.getNetwork();
            CyNetworkView view = controller.getNetworkView();
            Iterator nodes_i = network.nodesIterator();
            while (nodes_i.hasNext() && remainingGOIds.size()>0) {
                Node node = (Node) nodes_i.next();
                if (remainingGOIds.contains(node.toString())) {
                    remainingGOIds.remove(node.toString());

                    if (!controller.isVisibleNode(node)) {
                        controller.displayNode(node);
                    }

                    controller.addNodeToSlimSet(node);
                }
            }
            view.redrawGraph(false,false);
        }

        if (remainingGOIds.size()>0) {
             JOptionPane.showMessageDialog(this, "The following GO ids were invalid: " + lsep + remainingGOIds.toString(), "Invalid GO term Ids", JOptionPane.WARNING_MESSAGE);
             System.out.println("the following GO ids were invalid:" + remainingGOIds.toString());
        }

    }
}
