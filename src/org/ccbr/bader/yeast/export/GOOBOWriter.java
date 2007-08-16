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

/**This is a simple, incomplete implementation of a GO OBO 1.2 writer.  It is meant to handle only those aspects 
 * of GO OBO writing required by the GOSlimmer plugin.  While it should be capable of generating valid OBO 1.2 format files, 
 * it is not meant to be a general purpose OBO or GO OBO writer, and does not validate input in a comprehansive manner.  
 * For full details of the OBO 1.2 specification, see here:  http://www.geneontology.org/GO.format.obo-1_2.shtml
 * 
 * @author mikematan
 *
 */
public class GOOBOWriter {

	private static final String lsep = System.getProperty("line.separator");
	
	private BufferedWriter w;
	boolean headerIsWriten = false;

	public GOOBOWriter(BufferedWriter writer) {
		this.w = writer;
	}
	
	public void writeGOTermEntry(GOTermEntry entry) throws OBOFormatException, GOFormatException, IOException{
		GOTermEntry e = entry;
		if (entry.getId()==null) {
			throw new OBOFormatException("ID cannot be null");
		}
		if (entry.getNamespace()==null) {
			throw new GOFormatException("Namespace cannot be null");
		}
		//TODO verify if we want to enforce this restriction, or leave it up to the calling code
		if ((entry.getIs_a()==null || entry.getIs_a().size()==0) && (entry.getPart_of_relationship()==null || entry.getPart_of_relationship().size()==0)) {
			//entry has no parents;  this is only allowable for the root node
			if (!entry.getId().equals(entry.getNamespace().getRootTermId())) throw new GOFormatException("Term '" + entry.getId() + "' has no parents, but is not equal to the root term for it's namespace '" + entry.getNamespace().getName() + "'");
		}
		
		//verified entry is valid
		//output entry to underlying writer
		
		w.write("[TERM]"); w.write(lsep);
		w.write("id: " + e.id + lsep);
		if (e.id_is_anonymous) w.write("is_anonymous: true" + lsep);
		if (e.name !=null) w.write("name: " + e.name + lsep);
		w.write("namespace: " + e.ns + lsep);
		if (e.alt_id!=null) {
			for(String alt_id:e.alt_id) {
				w.write("alt_id: " + alt_id + lsep);
			}
		}
		if (e.def!=null) w.write("def: " + e.def + lsep);
		//TODO implement e.comment writing
		if (e.subset!=null) w.write("subset: " + e.subset + lsep);
		if (e.synonym!=null) w.write("synonym: " + e.synonym + lsep);
		writeTag("xref",e.xref);
		writeTag("is_a",e.is_a);
		if (e.is_obsolete) writeTag("is_obsolete","true");
		if (e.part_of_relationship!=null) {
			for(String part_of: e.part_of_relationship) {
				w.write("relationship: part_of " + part_of + lsep);
			}
		}
		
	}
	
	private void writeTag(String termName,String termValue) throws IOException {
		if (termValue!=null) {
			StringBuffer buf = new StringBuffer(termName);
			buf.append(":");
			buf.append(termValue);
			buf.append(lsep);
			w.write(buf.toString());			
			
		}
	}

	private void writeTag(String termName,List<String> termValues) throws IOException {
		if (termValues!=null) {
			for(String termValue:termValues) {
				w.write(termName + ":" + termValue + lsep);
			}
						
		}
	}

	public class GOTermEntry{
		//required
		private String name;
		String id;
		GONamespace ns;
		
		//optional tag following the id tag
		boolean id_is_anonymous = false;
		List<String> alt_id = new ArrayList<String>();
		String def;
		List<String> is_a= new ArrayList<String>();
		String synonym;  //synonymtypedef must be defined in the header, if a synonymtype is used
		String subset;  //must be defined by a subsetdef entry in the header;  that'll complicate writing as we go
		List<String> xref = new ArrayList<String>();
		
		/*note that this is not captured in CyAttributes by TableImport, but rather represented in the graph structure in 'part_of' relationships.
		 * These 'part_of' edges should be transcribed into lines like "relationship: part_of GO:0042274"
		 */
		List<String> part_of_relationship= new ArrayList<String>(); 
		boolean is_obsolete;
//		Ontology x = new Ontology();
//		{
//			
//		}
		public List<String> getAlt_id() {
			return alt_id;
		}
		public void setAlt_id(List<String> alt_id) {
			this.alt_id = alt_id;
		}
		
		public void addAlt_id(String alt_id) {
			this.alt_id.add(alt_id);
		}
		
		public String getDef() {
			return def;
		}
		public void setDef(String def) {
			this.def = def;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public boolean isId_is_anonymous() {
			return id_is_anonymous;
		}
		public void setId_is_anonymous(boolean id_is_anonymous) {
			this.id_is_anonymous = id_is_anonymous;
		}
		public List<String> getIs_a() {
			return is_a;
		}
		public void setIs_a(List<String> is_a) {
			this.is_a = is_a;
		}
		
		public void addIs_a(String is_a_id) {
			this.is_a.add(is_a_id);
		}
		
		public boolean isIs_obsolete() {
			return is_obsolete;
		}
		public void setIs_obsolete(boolean is_obsolete) {
			this.is_obsolete = is_obsolete;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public GONamespace getNamespace() {
			return ns;
		}
		public void setNamespace(GONamespace ns) {
			this.ns = ns;
		}

		public String getSubset() {
			return subset;
		}
		public void setSubset(String subset) {
			this.subset = subset;
		}
		public String getSynonym() {
			return synonym;
		}
		public void setSynonym(String synonym) {
			this.synonym = synonym;
		}

		public List<String> getXref() {
			return xref;
		}
		public void setXref(List<String> xref) {
			this.xref = xref;
		}
		
		public void addXref(String xref) {
			this.xref.add(xref);
		}
		
		public List<String> getPart_of_relationship() {
			return part_of_relationship;
		}
		public void setPart_of_relationship(List<String> part_of_relationship) {
			this.part_of_relationship = part_of_relationship;
		}

		public void addPart_of_relationship(String part_of_relation_id) {
			this.part_of_relationship.add(part_of_relation_id);
		}
		
	}
	
	public class GOOBOHeader{
		//required
		String format_version;
		
		//optional
		String data_version;
		String date;
		String saved_by;
		String auto_generated_by;
		String subsetdef;
		String import_url;
		String synonymtypedef;
		//more defined here: http://www.geneontology.org/GO.format.obo-1_2.shtml
	}
	
}
