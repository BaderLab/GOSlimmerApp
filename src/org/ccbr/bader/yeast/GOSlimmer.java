package org.ccbr.bader.yeast;

import java.util.HashMap;
import java.util.Map;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;

import cytoscape.CyNetwork;

public class GOSlimmer {

	public static final String directlyAnnotatedGenesAttributeName = "GENE_ASSOC.DIRECTLY_ANNOTATED_GENES";
	public static final String inferredAnnotatedGenesAttributeName = "GENE_ASSOC.INFERRED_ANNOTATED_GENES";
	
	public static final String directlyAnnotatedGenesSynonymAttributeName = "GENE_ASSOC.DIRECTLY_ANNOTATED_GENE_SYNONYMS";
	public static final String inferredAnnotatedGenesSynonymAttributeName = "GENE_ASSOC.INFERRED_ANNOTATED_GENE_SYNONYMS";
	
	public static final String goNodeInSlimSetAttributeName = "GOSLIM.INSET";
	
	public static final Map<CyNetwork,GOSlimmerCoverageStatBean> networkToCoverageStatMap = new HashMap<CyNetwork, GOSlimmerCoverageStatBean>();
	
	public static final String directlyAnnotatedUserGenesAttributeName = "GENE_ASSOC.DIRECTLY_ANNOTATED_USER_GENES";
	public static final String inferredAnnotatedUserGenesAttributeName = "GENE_ASSOC.INFERRED_ANNOTATED_USER_GENES";
	
	
//	/*This will record the name of the ontology which GOSlimmer is working on, as derived from the name of the network being worked on*/
//	public static String ontologyName;
//	
//	public static GeneAssociationReaderUtil geneAssociationReader; 
	
}
