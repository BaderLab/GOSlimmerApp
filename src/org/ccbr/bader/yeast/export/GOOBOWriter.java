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
 * * Description: Class for exporting a GO OBO file.  Prototype class, currently unused, untested and incomplete.
 */
package org.ccbr.bader.yeast.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ccbr.bader.yeast.GONamespace;

import cytoscape.data.annotation.Ontology;

/**This is a simple implementation of a GO OBO 1.2 writer.  It is meant to handle only those aspects
 * of GO OBO writing required by the GOSlimmer plugin.  While it should be capable of generating valid OBO 1.2 format files, 
 * it is not meant to be a general purpose OBO or GO OBO writer, and does not validate input in a comprehansive manner.  
 * For full details of the OBO 1.2 specification, see here:  http://www.geneontology.org/GO.format.obo-1_2.shtml
 * 
 * @author mikematan,
 * @author laetitiamorrison
 *
 */
public class GOOBOWriter {

	private static final String lsep = System.getProperty("line.separator");
	
	private BufferedWriter w;

	public GOOBOWriter(BufferedWriter writer) {
		this.w = writer;
	}

    /*
     * Method to write the OBO header to the specified writer
     * @param header the header to be written to the file
     * @throws IOException
     */
    public void writeHeader(GOOBOHeader header) throws IOException {

        // get header information
        String format_version = header.getFormat_version();
        String data_version = header.getData_version();
        String date = header.getDate();
        String saved_by = header.getSaved_by();
        String auto_generated_by = header.getAuto_generated_by();
        String import_url = header.getImport_url();
        String synonymtypedef = header.getSynonymtypedef();
        String default_namespace = header.getDefault_namespace();
        List<String> subsetdef = header.getSubsetdef();
        List<String> remark = header.getRemark();

        // write header information
        writeTag("format-version", format_version);
        writeTag("data-version", data_version);
        writeTag("date", date);
        writeTag("saved-by", saved_by);
        writeTag("auto-generated-by", auto_generated_by);
        writeTag("import", import_url);
        writeTag("subsetdef", subsetdef);
        writeTag("synonymtypedef", synonymtypedef);
        writeTag("default-namespace", default_namespace);
        writeTag("remark", remark);
    }

    /*
     * Method to write the GO term entry in OBO format to the specified writer
     * @param entry The GO term entry to be written
     * @throws OBOFormatException
     * @throws IOException
     */
    public void writeGOTermEntry(GOTermEntry entry) throws OBOFormatException, IOException{

        // perform minimal validation of GO term entry
        if (entry.getId()==null) {
			throw new OBOFormatException("ID cannot be null");
		}
		if (entry.getNamespace()==null) {
			throw new GOFormatException("Namespace cannot be null");
		}

		// get GO term information
        String id = entry.getId();
        String name = entry.getName();
        String namespace = entry.getNamespace();
        List<String> alt_id = entry.getAlt_id();
        String def = entry.getDef();
        List<String> def_origin = entry.getDef_origin();
        String comment = entry.getComment();
        List<String> subset = entry.getSubset();
        List<String> synonym = entry.getSynonym();
        List<String> xref = entry.getXref();
        List<String> disjoint_from = entry.getDisjoint_from();
        List<String> is_a = entry.getIs_a();
        List<String> relationship = entry.getRelationship();

        // write GO term information
        w.write(lsep + "[Term]" + lsep);
        writeTag("id", id);
        writeTag("name", name);
        writeTag("namespace", namespace);
        writeTag("alt_id",alt_id);

        String definition = "";
        if (def != null && !def.equals("")) {
            definition = "\"" + def + "\"";
            if (def_origin != null) {
                definition = definition + " " + def_origin.toString();
            }
            writeTag("def", definition);
        }

        writeTag("comment", comment);
        writeTag("subset", subset);
        writeTag("synonym", synonym);
        writeTag("xref", xref);
        writeTag("disjoint_from", disjoint_from);
        writeTag("is_a", is_a);
        writeTag("relationship", relationship);
	}

    /*
     * Method to write tag information
     * @param tagName Name of the tag to be written
     * @param tagValue Value of the tag to be written
     * @throws IOException
     */
    private void writeTag(String tagName,String tagValue) throws IOException {
		if (tagValue!=null && !tagValue.equals("")) {
            w.write(tagName + ": " + tagValue + lsep);
		}
	}

    /*
     * Method to write tag information
     * @param tagName Name of the tag to be written
     * @param tagValues List of values for the tag to be written
     * @throws IOException
     */
    private void writeTag(String tagName,List<String> tagValues) throws IOException {
		if (tagValues!=null) {
			for(String tagValue:tagValues) {
                if (tagValue != null) {
                    w.write(tagName + ": " + tagValue + lsep);
                }
			}
						
		}
	}
	
}
