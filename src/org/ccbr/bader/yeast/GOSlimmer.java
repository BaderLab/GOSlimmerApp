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
 * * Description: Defines static fields used globally by GOSlimmer
 */

package org.ccbr.bader.yeast;

import java.util.HashMap;
import java.util.Map;

import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;

import cytoscape.CyNetwork;

public class GOSlimmer {

	/**
	 * The name of the cytoscape attribute for the list of gene ids which are directly annotated by a go term node
	 */
	public static final String directlyAnnotatedGenesAttributeName = "GOSlimmer.DIRECTLY_ANNOTATED_GENES";
	/**
	 * The name of the cytoscape attribute for the list of gene ids which are annotated by a go term node by inference from those 
	 * directly annotated by its descendant terms
	 */
	public static final String inferredAnnotatedGenesAttributeName = "GOSlimmer.INFERRED_ANNOTATED_GENES";
	
	/**
	 * The name of the cytoscape attribute for the list of gene synonyms which are directly annotated by a go term node
	 */
	public static final String directlyAnnotatedGenesSynonymAttributeName = "GOSlimmer.DIRECTLY_ANNOTATED_GENE_SYNONYMS";
	/**
	 * The name of the cytoscape attribute for the list of gene synonyms which are annotated by a go term node by inference from those 
	 * directly annotated by its descendant terms
	 */
	public static final String inferredAnnotatedGenesSynonymAttributeName = "GOSlimmer.INFERRED_ANNOTATED_GENE_SYNONYMS";
	
	/**
	 * The name of the boolean cytoscape attribute which indicates whether or not a given node is included in the user defined 
	 * slim set
	 */
	public static final String goNodeInSlimSetAttributeName = "GOSlimmer.INSET";
	
	/**
	 * Maps the GO term networks managed by GOSlimmer to the coverage stat beans which maintain information on which genes 
	 * the network's slim sets cover
	 */
	public static final Map<CyNetwork,GOSlimmerCoverageStatBean> networkToCoverageStatMap = new HashMap<CyNetwork, GOSlimmerCoverageStatBean>();
	
	/**
	 * The name of the cytoscape attribute for the list of user gene ids which are directly annotated by a go term node
	 */
	public static final String directlyAnnotatedUserGenesAttributeName = "GOSlimmer.DIRECTLY_ANNOTATED_USER_GENES";
	
	/**
	 * The name of the cytoscape attribute for the list of user gene ids which are annotated by a go term node by inference from those 
	 * directly annotated by its descendant terms
	 */
	public static final String inferredAnnotatedUserGenesAttributeName = "GOSlimmer.INFERRED_ANNOTATED_USER_GENES";

    /**
     * The name of the cytoscape attribute for the number of gene ids which are directly annotated by a go term node
     */
    public static final String directlyAnnotatedGeneNumberAttributeName = "GOSlimmer.NUMBER_DIRECTLY_ANNOTATED_GENES";
    /**
     * The name of the cytoscape attribute for the number of gene ids which are annotated by a go term node by inference from those
     * directly annotated by its descendant terms
     */
    public static final String inferredAnnotatedGeneNumberAttributeName = "GOSlimmer.NUMBER_INFERRED_ANNOTATED_GENES";

//	/*This will record the name of the ontology which GOSlimmer is working on, as derived from the name of the network being worked on*/
//	public static String ontologyName;
//	
//	public static GeneAssociationReaderUtil geneAssociationReader;

    /**
     * The name of the cytoscape attribute for the formatted go term name (which has been wrapped to multiple lines
     * according to the maximum label length.
     */
    public static final String formattedOntologyNameAttributeName = "GOSlimmer.FORMATTED_NAME";
	
}
