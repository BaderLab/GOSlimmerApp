package org.ccbr.bader.yeast;

import java.util.HashMap;
import java.util.Map;

import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;

import cytoscape.CyNetwork;

public class GOSlimmer {

	public static final String directlyAnnotatedGenesAttributeName = "GENE_ASSOC.DIRECTLY_ANNOTATED_GENES";
	public static final String inferredAnnotatedGenesAttributeName = "GENE_ASSOC.INFERRED_ANNOTATED_GENES";
	public static final String goNodeInSlimSetAttributeName = "GOSLIM.INSET";
	
	public static final Map<CyNetwork,GOSlimmerCoverageStatBean> networkToCoverageStatMap = new HashMap<CyNetwork, GOSlimmerCoverageStatBean>();
	
	/*This will record the name of the ontology which GOSlimmer is working on, as derived from the name of the network being worked on*/
	public static String ontologyName;
}
