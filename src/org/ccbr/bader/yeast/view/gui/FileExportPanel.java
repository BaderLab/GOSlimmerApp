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
import org.ccbr.bader.yeast.export.OBOExtensionFileFilter;
import org.ccbr.bader.yeast.view.gui.misc.JButtonMod;
import org.ccbr.bader.yeast.export.GOTermEntry;
import org.ccbr.bader.yeast.export.GOOBOWriter;
import org.ccbr.bader.yeast.export.GOOBOHeader;
import org.ccbr.bader.yeast.export.GOOBOTypeDef;
import org.ccbr.bader.yeast.export.OBOFormatException;

import cytoscape.task.TaskMonitor;
import cytoscape.task.Task;
import cytoscape.task.util.TaskManager;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.CyNetwork;

import giny.model.Node;

import static cytoscape.data.ontology.readers.OBOTags.ID;
import static cytoscape.data.ontology.readers.OBOTags.NAME;
import static cytoscape.data.ontology.readers.OBOTags.NAMESPACE;
import static cytoscape.data.ontology.readers.OBOTags.ALT_ID;
import static cytoscape.data.ontology.readers.OBOTags.DEF;
import static cytoscape.data.ontology.readers.OBOTags.DEF_ORIGIN;
import static cytoscape.data.ontology.readers.OBOTags.COMMENT;
import static cytoscape.data.ontology.readers.OBOTags.SUBSET;
import static cytoscape.data.ontology.readers.OBOTags.SYNONYM;
import static cytoscape.data.ontology.readers.OBOTags.XREF;

import cytoscape.data.CyAttributes;

/**GUI Widget for exporting remapped annotation files.
 * 
 * @author mikematan
 *
 */
public class FileExportPanel extends JPanel implements ActionListener {

	private Collection<GOSlimmerController> controllers;

	private GOSlimmerSession session;

    private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();

    protected static final String TERM_TAG = "[Term]";

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

        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        this.add(getExportAnnotationFileButton(),c);

        c.gridx = 0;
        c.gridy = 1;
        this.add(getExportSlimSetFileButton(), c);

        c.gridx = 0;
        c.gridy = 2;
        this.add(getExportSlimSetOBOFileButton(),c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;      
        c.gridx = 0;
        c.gridy = 3;
        this.add(getOBOSettingsPanel(), c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 4;
        this.add(getImportSlimSetFileButton(), c);

        c.gridx = 0;
        c.gridy = 5;
        this.add(getImportSlimSetOBOFileButton(),c);
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


    JButton importSlimSetOBOFileButton;
    private static final String importSlimSetOBOFileButtonText = "Import Slim Set OBO File";
    private static final String importSlimSetOBOFileButtonToolTip =
            "Import an 'OBO' formatted file containing the desired slim set.";

    private JButton getImportSlimSetOBOFileButton() {
        if (importSlimSetOBOFileButton == null) {
            importSlimSetOBOFileButton = new JButtonMod(importSlimSetOBOFileButtonText);
            importSlimSetOBOFileButton.addActionListener(this);
            importSlimSetOBOFileButton.setToolTipText(importSlimSetOBOFileButtonToolTip);
        }
        return importSlimSetOBOFileButton;
    }

    JButton exportSlimSetOBOFileButton;
    private static final String exportSlimSetOBOFileButtonText = "Export Slim Set OBO File";
    private static final String exportSlimSetOBOFileButtonToolTip =
            "Export an 'OBO' formatted file containing your selected slim set.";

    private JButton getExportSlimSetOBOFileButton() {
        if (exportSlimSetOBOFileButton == null) {
            exportSlimSetOBOFileButton = new JButtonMod(exportSlimSetOBOFileButtonText);
            exportSlimSetOBOFileButton.addActionListener(this);
            exportSlimSetOBOFileButton.setToolTipText(exportSlimSetOBOFileButtonToolTip);
        }
        return exportSlimSetOBOFileButton;
    }

    private AdvancedOBOSettingsPanel OBOSettingsPanel;
    private AdvancedOBOSettingsPanel getOBOSettingsPanel() {
        if (OBOSettingsPanel == null) {
            OBOSettingsPanel = new AdvancedOBOSettingsPanel();
        }
        return OBOSettingsPanel;
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
            
            else if (src == importSlimSetOBOFileButton) {
                chooser.setFileFilter(new OBOExtensionFileFilter());
                int retval = chooser.showOpenDialog(this);
                if (retval==JFileChooser.APPROVE_OPTION) {
			        final File slimSetFile = chooser.getSelectedFile();

                    TaskManager.executeTask(new Task() {

                        private TaskMonitor taskMonitor=null;

                        public String getTitle() {
					        return "Importing slim set obo file and building slim set";
				        }

				        public void halt() {

				        }

				        public void run() {
					        try {
						        Collection<String> goTermIds = readOBOFile(slimSetFile);
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

            else { // one of the export options

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
								    	//controller.addNodeToSlimSet(GOSlimmerUtil.getRootNode(controller.getNetwork()));
                                        controller.addNodeToSlimSet(controller.getRootNode());
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
                    else if (src == exportSlimSetOBOFileButton) {
                        if (exportFile.exists()) {
					    	if (!exportFile.delete()) {
						    	JOptionPane.showMessageDialog(this, "Failed to overwrite selected export file '" + exportFile.getName() + "'.");
						    }
					    }
					    Map<String, Map<String, Set<String>>> ontRelationshipRemap = new HashMap<String, Map<String, Set<String>>>();
					    for(GOSlimmerController controller: controllers) {

                            try {
							    try {
                                    // this will throw an exception if the root node is not selected
                                    ontRelationshipRemap.putAll(GOSlimmerUtil.createOBORemapReliationships(controller));
                                    
                                } catch (RootNodeNotSelectedException e) {
								    //TODO
								    int rv = JOptionPane.showConfirmDialog(this, "Root node of GO namespace " + controller.getNamespace().getName() + " must be included in slim set for export.  Include root node and continue?", "Warning:  root term not selected", JOptionPane.YES_NO_OPTION);//, arg1)Dialog(this,"Failed to remap terms due to exception: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
								    if (rv == JOptionPane.YES_OPTION) {
								    	//add the root node to the slimset and try the remapping again
                                        //controller.addNodeToSlimSet(GOSlimmerUtil.getRootNode(controller.getNetwork()));
								    	controller.addNodeToSlimSet(controller.getRootNode());
                                        ontRelationshipRemap.putAll(GOSlimmerUtil.createOBORemapReliationships(controller));
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
                            createRemappedOntologyFile(exportFile, ontRelationshipRemap);
					    } catch (IOException e) {
						    JOptionPane.showMessageDialog(this,"Failed to create remapped OBO ontology file due to IO Error: "+ e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
						    return;
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

    /**
     * Parse the slim set obo file imported by the user
     *
     * @param slimSetFile the .obo file specified by the user which contains the slim set
     * @return a collection of GO term IDs parsed from the file
     * @throws IOException
     */
    private Collection<String> readOBOFile(File slimSetFile) throws IOException {

        Collection<String> GOIds = new HashSet<String>();
        BufferedReader bufRd = new BufferedReader(new FileReader(slimSetFile));

        String line;

		while ((line = bufRd.readLine()) != null) {
			// Read header
			if (line.startsWith(TERM_TAG)) {
				String goId = readEntry(bufRd);
                if (goId != null) {
                    GOIds.add(goId);
                }
			}
		}

		try {
            bufRd.close();

		}
        catch (IOException ioe) {
		}
        finally {
			bufRd = null;
		}

        return GOIds;
    }






	/**
	 * Read one Ontology Term
	 *
	 * @param rd
	 * @throws IOException
     * @return id of this ontology entry
     */

	private String readEntry(final BufferedReader rd) throws IOException {
		String id = "";
		String line = null;

		while (true) {
			line = rd.readLine().trim();

			if (line.length() == 0)
				break;

			final int colonInx = line.indexOf(':');
			final String key = line.substring(0, colonInx).trim();
			final String val = line.substring(colonInx + 1).trim();
			Node source = null;

			if (key.equals(ID.toString())) {
				// There's only one id.
				return val;
			}

		}
        return null;
    }

    /**Create a modified version of the imported ontology file containing only those GO terms in the slim set
	 * @param remapFile the file to write the remapped annotation data to
	 * @param ontRelationshipRemap the map of selected GO term ids to a map of their selected parents and corresponding relationships
     * @throws IOException
	 */
	private void createRemappedOntologyFile(File remapFile,Map<String, Map<String, Set<String>>> ontRelationshipRemap) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(remapFile));

        GOOBOWriter oboWriter = new GOOBOWriter(w);

        // get header information from advanced OBO options
        String user = OBOSettingsPanel.getUserName();
        String subsetdefCode = OBOSettingsPanel.getSubsetdefCode();
        String subsetdefDefinition = OBOSettingsPanel.getSubsetdefDefinition();
        String subsetdefName = OBOSettingsPanel.getSubsetdefName();

        // Create and write header
        GOOBOHeader header = new GOOBOHeader("1.2");
        header.setDate(new Date());
        header.setAuto_generated_by("GO Slimmer Version 1.0");
        header.setSaved_by(user);
        header.addSubsetdef(subsetdefCode + " \"" + subsetdefDefinition + "\"");
        header.setSynonymtypedef("systematic_synonym \"Systematic synonym\" EXACT");
        header.setDefault_namespace("gene_ontology");
        header.addRemark("GO_slim_name: " + subsetdefName);
        header.addRemark("GO_slim_author: " + user);

        oboWriter.writeHeader(header);

        // Write the entries for the selected nodes

        // get list of selected nodes
        Set<Node> slimSetNodes = new HashSet<Node>();
        for (GOSlimmerController controller:controllers) {
            slimSetNodes.addAll(controller.getStatBean().getSlimGoNodes());
        }

        Set<String> relationshipTypes = new HashSet<String>();

        for (Node slimSetNode: slimSetNodes) {

            // get String attributes
            String id = slimSetNode.getIdentifier();
            String termName = nodeAtt.getStringAttribute(id, "ontology." + NAME.toString());
            String termNamespace = nodeAtt.getStringAttribute(id, "ontology." + NAMESPACE.toString());

            String termDef = nodeAtt.getStringAttribute(id, "ontology." + DEF.toString());
            String termComment = nodeAtt.getStringAttribute(id, "ontology." + COMMENT.toString());


            // get attributes that may be lists
            List<String> termDefOri = new ArrayList<String>();
            if (nodeAtt.getType("ontology." + DEF_ORIGIN.toString()) == CyAttributes.TYPE_SIMPLE_LIST) {
                termDefOri.addAll(nodeAtt.getListAttribute(id, "ontology." + DEF_ORIGIN.toString()));
            }
            else {
                termDefOri.add(nodeAtt.getStringAttribute(id, "ontology." + DEF_ORIGIN.toString()));
            }

            List<String> termAltIds = new ArrayList<String>();
            if (nodeAtt.getType("ontology." + ALT_ID.toString()) == CyAttributes.TYPE_SIMPLE_LIST) {
                termAltIds.addAll(nodeAtt.getListAttribute(id, "ontology." + ALT_ID.toString()));
            }
            else {
                termAltIds.add(nodeAtt.getStringAttribute(id, "ontology." + ALT_ID.toString()));
            }

            List<String> termSubsets = new ArrayList<String>();
            if (nodeAtt.getType("ontology." + SUBSET.toString()) == CyAttributes.TYPE_SIMPLE_LIST) {
                termSubsets.addAll(nodeAtt.getListAttribute(id, "ontology." + SUBSET.toString()));
            }
            else {
                termSubsets.add(nodeAtt.getStringAttribute(id, "ontology." + SUBSET.toString()));
            }
            termSubsets.add(subsetdefCode);

            List<String> termXRefs = new ArrayList<String>();
            if (nodeAtt.getType("ontology." + XREF.toString()) == CyAttributes.TYPE_SIMPLE_LIST) {
                termXRefs.addAll(nodeAtt.getListAttribute(id, "ontology." + XREF.toString()));
            }
            else {
                termXRefs.add(nodeAtt.getStringAttribute(id, "ontology." + XREF.toString()));
            }

            List<String> termDisjoint = new ArrayList<String>();
            if (nodeAtt.getType("ontology.disjoint_from") == CyAttributes.TYPE_SIMPLE_LIST) {
                termDisjoint.addAll(nodeAtt.getListAttribute(id, "ontology.disjoint_from"));
            }
            else {
                termDisjoint.add(nodeAtt.getStringAttribute(id, "ontology.disjoint_from"));
            }

            // get map attribute
            List<String> termSynonyms = new ArrayList<String>();

            if (nodeAtt.getType("ontology." + SYNONYM.toString()) == CyAttributes.TYPE_SIMPLE_MAP) {
                Map <String,Object> synonyms = nodeAtt.getMapAttribute(id, "ontology." + SYNONYM.toString());
                for (String key:synonyms.keySet()) {
                    termSynonyms.add("\"" + key + "\" " + synonyms.get(key).toString());
                }
            }

            GOTermEntry goTerm = new GOTermEntry(id, termName, termNamespace, termAltIds, termDef, termDefOri, termComment, termSubsets, termSynonyms, termXRefs, termDisjoint, null, null);

            // get the remapped parent relationships for this term
            if (ontRelationshipRemap.containsKey(id)) {
                // get map of parents and relationships
                Map<String, Set<String>> parentRelationshipMap = ontRelationshipRemap.get(id);

                for (String parent : parentRelationshipMap.keySet()) {
                    Set<String> relationships = parentRelationshipMap.get(parent);
                    for (String relationship : relationships) {
                        if (relationship.equals("is_a")) {
                            goTerm.addIs_a(parent);
                        } else {
                            goTerm.addRelationship(relationship + " " + parent);
                            relationshipTypes.add(relationship);
                        }
                    }
                }

            }




            // write this go term information to the OBO file
            try {
                oboWriter.writeGOTermEntry(goTerm);
            }
            catch (OBOFormatException e) {
            }
        }

        // Create and write typedef stanza for 'part_of' relationship (if it exists)
        if (relationshipTypes.contains("part_of")) {
            GOOBOTypeDef typedef = new GOOBOTypeDef("part_of", "part_of");
            typedef.addXref("OBO_REL:part_of");
            typedef.setIsTransitive(true);

            oboWriter.writeTypeDef(typedef);
        }
 
        w.close();
	}
}
