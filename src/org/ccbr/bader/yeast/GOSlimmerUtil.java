package org.ccbr.bader.yeast;

import giny.model.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

public class GOSlimmerUtil {

	private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	
	public static int getNumGenesCoveredByGoNode(Node goNode,boolean includeCoverageInferredFromDescendants) {
		//Note:  this could be implemented more concisely by calling the 'getGenesCoveredByGoNode' method and then returning the 
		int numCovered;
		List<String> directlyCoveredGenes = nodeAtt.getListAttribute(goNode.getIdentifier(), GOSlimmer.directlyAnnotatedGenesAttributeName);
		
		numCovered = directlyCoveredGenes!=null?directlyCoveredGenes.size():0;
		
		if (includeCoverageInferredFromDescendants) {
			List<String> inferredCoveredGenes = nodeAtt.getListAttribute(goNode.getIdentifier(), GOSlimmer.inferredAnnotatedGenesAttributeName);
			numCovered += inferredCoveredGenes!=null?inferredCoveredGenes.size():0;
		}
		
		return numCovered;
	}
	
	public static List<String> getGenesCoveredByGoNode(Node goNode,boolean includeCoverageInferredFromDescendants) {
		int numCovered;
		List<String> coveredGenes = nodeAtt.getListAttribute(goNode.getIdentifier(), GOSlimmer.directlyAnnotatedGenesAttributeName);
		if (coveredGenes == null) coveredGenes = new ArrayList<String>();
		
		if (includeCoverageInferredFromDescendants) {
			List<String> inferredCoveredGenes = nodeAtt.getListAttribute(goNode.getIdentifier(), GOSlimmer.inferredAnnotatedGenesAttributeName);
			if (inferredCoveredGenes!=null )coveredGenes.addAll(inferredCoveredGenes);
		}
		
		return coveredGenes;
	}
	
	public static List<String> getDirectlyCoveredGenes(Node goNode) {
		int numCovered;
		List<String> coveredGenes = nodeAtt.getListAttribute(goNode.getIdentifier(), GOSlimmer.directlyAnnotatedGenesAttributeName);
		if (coveredGenes == null) coveredGenes = new ArrayList<String>();
		
		return coveredGenes;
	}
	
	public static List<String> getInferredCoveredGenes(Node goNode) {
		int numCovered;
		List<String> coveredGenes = nodeAtt.getListAttribute(goNode.getIdentifier(), GOSlimmer.inferredAnnotatedGenesAttributeName);
		if (coveredGenes == null) coveredGenes = new ArrayList<String>();
		
		return coveredGenes;
	} 
	
	/**This method will create a new attribute for the nodes of the DAG rooted at <code>node</code> which 
	 * contains a list of the genes which that node's GO term annotates by inference based on what genes 
	 * are annotated by that GO term's children.
	 * 
	 * It is implemented as a dynamic programming algorithm in that it will use the already calculated 
	 * list of inferred genes if it is already present for a node, only calculating it if it is not.
	 * 
	 * It is a recursive implementation, though it will only descend if the values have not already been 
	 * calculated.  Note that this will not scale to very deep graphs.
	 * 
	 * @param node root node of DAG which is to have parent attributions attached
	 * @param network TODO
	 * @return list of genes covered by <code>node</code>'s children (not including those covered by node itself 
	 * which are not covered by its children).
	 */
	public static Set<String> getGenesCoveredByChildren(Node node, CyNetwork network) {
		//base case 1:  node has already been annotated with inferred covered gene list, so just return that
		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
		List<String> inferredCoveredGenes = nodeAtt.getListAttribute(node.getIdentifier(), GOSlimmer.inferredAnnotatedGenesAttributeName);
		//NOTE:  cannot accept empty lists as belonging to case 1 because the getListAttribute method will return empty lists for attributes which have not be set at all (instead of the expected null)
		if (inferredCoveredGenes != null && inferredCoveredGenes.size()>0) {
			//convert to a set, then return
			return listToSet(inferredCoveredGenes);
		}
		//base case 2: inferred covered genes needs to be calculated, but there are no children
		//get the children nodes;  note that children direct edges towards parents, that's why we're getting incoming edges
		int[] childEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
		if (childEdges == null || childEdges.length==0) {
			return new HashSet<String>();
		}
		//case 3:  inferred covered genes based on children needs to be calculated
		Set<String> inferredCoveredGenesS = new HashSet<String>();
		//iterate through the children of the node, for each one retrieving the directly and inferred covered genes and adding them to the inferredCoveredGenesS collection
		for(int childEdgeI:childEdges) {
			//get the child node; note that children direct edges to parents, so we use 'getSource()' instead of 'getTarget()'
			Node child = network.getEdge(childEdgeI).getSource();
			//get the child's directly covered genes
			List<String> childsDirectlyCoveredGenes = nodeAtt.getListAttribute(child.getIdentifier(), GOSlimmer.directlyAnnotatedGenesAttributeName);
			inferredCoveredGenesS.addAll(childsDirectlyCoveredGenes);
			
			//get the childs inferred covered genes
			Set<String> childsInferredCoveredGenes = getGenesCoveredByChildren(child,network);
			//set the child node's inferred covered genes attribute
			nodeAtt.setListAttribute(child.getIdentifier(), GOSlimmer.inferredAnnotatedGenesAttributeName, setToList(childsInferredCoveredGenes));
			inferredCoveredGenesS.addAll(childsInferredCoveredGenes);
		}
		return inferredCoveredGenesS;
	}
	
	public static Set<String> listToSet(List<String> list) {
		Set<String> set = new HashSet<String>();
		set.addAll(list);
		return set;
	}
	
	public static List<String> setToList(Set<String> set) {
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		return list;
	}
	
}
