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
 * * Description: Provides functionality for remapping a set of GO terms which are not included in a term set to ancestor terms 
 * which are in that set
 */
package org.ccbr.bader.yeast.export;

import giny.model.Node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.ccbr.bader.yeast.GOSlimmer;
import org.ccbr.bader.yeast.GOSlimmerException;
import org.ccbr.bader.yeast.GOSlimmerUtil;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**Provides functionality for remapping a set of GO terms which are not included in a term set to ancestor terms 
 * which are in that set.  This is needed when wants to regenerate an imported gene annotation file such that it only annotates 
 * genes within the user specified GO Slim Set.  The general procedure is this:
 *   <BR>Traverse tree in depth first manner
 *   <BR>On each node, if the node does not belong to the slim set, map it to a set of go terms which contains the last GO term 
 *   encountered (in the depth first traversal) which was in the slim set
 * <P> Note that terms with multiple (in slim set) ancestors along distinct ancestor paths will be mapped to each of the ancestors;  
 * this eliminates the possibility of ambiguity in the remapping due to different depth first traversals. 
 * @author mikematan
 *
 */
public class GeneAnnotationRemapWriter {

	private static final int GOID = 4;
	
	BufferedWriter w = null;
	
	Map<String, Set<String>> goTermRemap;
	
	/**Creates a remap writer which will write a remap of the given GO DAG's go terms to the specified writer parameter
	 * @param writer the writer which the remapping should be written to
	 * @param GODAG the GOSlimmer GO DAG which is to have its terms remapped.  The remapping will be performed based on the values of the attributes of the GO nodes
	 * @throws GOSlimmerException
	 */
	public GeneAnnotationRemapWriter(BufferedWriter writer, CyNetwork GODAG) throws GOSlimmerException {
		this.w = writer;
		this.goTermRemap = GOSlimmerUtil.createGoTermMultipleRemap(GODAG);
	}
	
	
	
	/**Creates a remap writer which will write the given GO term remapping Map to the specified writer parameter
	 * @param writer the writer which the remapping should be written to
	 * @param goTermRemap the map of unselected GO terms to the selected terms which they are to be mapped to
	 * @throws GOSlimmerException
	 */
	public GeneAnnotationRemapWriter(BufferedWriter writer, Map<String, Set<String>> goTermRemap) {
		this.w = writer;
		this.goTermRemap = goTermRemap;
	}



	public void write(String s) throws IOException {
		w.write(s);
	}
	
	private static final String lsep = System.getProperty("line.separator");
	
	/**Writes a remapped version of the given gene association annotation file entry based on the map which this remapper was 
	 * initialized with.  The entry(s) output will be in the standard gene association file format 
	 * @param annotationEntry  The annotationEntry array contains the fields of the annotation entry record in the standard order as seen 
	 * in the gene association file format. 
	 * @throws IOException
	 */
	public void writeRemappedEntry(String[] annotationEntry) throws IOException {
		String originalGoTerm = annotationEntry[GOID];
		Set<String> remappedGoTerms = goTermRemap.get(originalGoTerm);
		if (remappedGoTerms == null) {
			throw new RuntimeException("GO Term '" + originalGoTerm + "' does not have a term remapping mapping");
		}
		for(String remappedGoTerm:remappedGoTerms) {
			for(int i = 0;i<annotationEntry.length;i++) {
				if (i!=GOID) {
					w.write(annotationEntry[i]);
				} 
				else {
					//TODO we'll also need to remap some of the other columns which describe the GO term (or do we?)
					w.write(remappedGoTerm);
				}
				w.write("\t");
			}
			w.write(lsep);
		}
	}


	
	
	
}
