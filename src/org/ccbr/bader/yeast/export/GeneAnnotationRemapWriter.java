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

public class GeneAnnotationRemapWriter {

	private static final int GOID = 4;
	
	BufferedWriter w = null;
	
	Map<String, Set<String>> goTermRemap;
	
	public GeneAnnotationRemapWriter(BufferedWriter writer, CyNetwork GODAG) throws GOSlimmerException {
		this.w = writer;
		this.goTermRemap = GOSlimmerUtil.createGoTermMultipleRemap(GODAG);
	}
	
	
	
	public GeneAnnotationRemapWriter(BufferedWriter writer, Map<String, Set<String>> goTermRemap) {
		this.w = writer;
		this.goTermRemap = goTermRemap;
	}



	public void write(String s) throws IOException {
		w.write(s);
	}
	
	private static final String lsep = System.getProperty("line.separator");
	
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
