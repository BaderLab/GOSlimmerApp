package org.ccbr.bader.yeast;

import giny.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
	
	public static Node getRootNode(CyNetwork network) {
		Iterator<Node> nodeI = network.nodesIterator();
		//pick a node arbitrarily TODO ensure that nodeI is not biased towards nodes away from the root
		if (!nodeI.hasNext()) return null;
		Node node = nodeI.next();
		
		//follow the node's ancestors until we get to the ancestor with no ancestors, which must be the root; note that children direct 'is_a' edges to parents
		int[] ancestorEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, false, true);
		while (ancestorEdges!=null && ancestorEdges.length>0) {
			//get the parent node
			node = network.getNode(network.getEdgeTargetIndex(ancestorEdges[0]));
			ancestorEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, false, true);
		}
		return node;
	}
	
	public static  Map<String, String> createGoTermRemap(CyNetwork godag) {
		Map<String, String> goTermRemap = new HashMap<String, String>();
		Node rootNode = GOSlimmerUtil.getRootNode(godag);
		/*
		 * pseudo code for remapping:
		 * descent the tree depth first, keeping track of the last selected node;  
		 * 	we'll probably need to keep a stack of said selected nodes, popping the stack as we ascend above it again
		 * 	TODO figure out how to deal with the nodes with multiple possible parents:  how should we remap them?  
		 * 		For now, just do it stochastically, but we'll need to find a consistent way, or at least alert users to when this is done.
		 * on finding a node which is not selected, map it to the last selected node which was encountered.  Nodes which are selected for 
		 * go slim set inclusion either need not be mapped or can be mapped to themselves (for consistencies sake, this is probably the better 
		 * option, for then we can use the null mapping case as an exceptional one)
		 * 
		 */
		//this stack will keep track of the last selected term as we descend the list;  upon going above the term, we can pop the stack
		Stack<String> lastSelectedTermId = new Stack<String>();
//		Node curNode = rootNode;
		if (isSelected(rootNode)) {
			lastSelectedTermId.push(rootNode.getIdentifier());
			goTermRemap.put(rootNode.getIdentifier(), rootNode.getIdentifier());
		}
		else {
			throw new RuntimeException("Cannot remap because root node is not selected; some annotations would be lost.");
		}
		int[] childEdges = godag.getAdjacentEdgeIndicesArray(rootNode.getRootGraphIndex(), false, true, false);
		remapGoTerms(childEdges, lastSelectedTermId, godag, goTermRemap);
		//TODO insert sanity check to ensure stack only contains the root node term, as it should
		return goTermRemap;
	}
	
	private static  void remapGoTerms(int[] childEdges,Stack<String> lastSelectedTermId,CyNetwork godag,Map remap) {
		if (childEdges == null) return;
		for(int childEdge:childEdges) {
			/*
			 * pseudo code:
			 * get the child node
			 * if (child node is selected) {
			 *   push child node's GO ID onto the lastSelectedTermId stack
			 *   get child Edges for child node
			 *   recursive call to remap on child nodes
			 *   pop stack
			 * else {
			 *   peek at stack and remap child node's go term to that term
			 *   get child edges for child node
			 *   recursive call to remap on child nodes
			 * }
			 */
			Node node = godag.getNode(godag.getEdgeSourceIndex(childEdge));
			if (isSelected(node)) {
				String goTerm = node.getIdentifier();
				remap.put(goTerm,goTerm);
				lastSelectedTermId.push(goTerm);
				int[]  grandChildEdges =godag.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
				remapGoTerms(grandChildEdges, lastSelectedTermId, godag, remap);
				if (goTerm != lastSelectedTermId.pop()) {
					//sanity check failed:  pop should have returned the same go term which was pushed onto it by this method call
					throw new RuntimeException("element at top of stack is not the same one which was placed there, as is expected by algorithm logic.");
				}
			}
			else {
				String lastSelectedTerm = lastSelectedTermId.peek();
				//sanity check
				if (lastSelectedTerm ==null) throw new RuntimeException("cannot remap because no term on stack");
				//map this unselected go term to the last selected go term, so that it's annotations will be remapped to the selected term 
				remap.put(node.getIdentifier(), lastSelectedTerm);
				int[]  grandChildEdges =godag.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), false, true, false);
				remapGoTerms(grandChildEdges, lastSelectedTermId, godag, remap);
			}
		}
	}
	

	private static boolean isSelected(Node goNode) {
		Boolean isSelected =  nodeAtt.getBooleanAttribute(goNode.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName);
		if (isSelected ==null) return false;
		return isSelected;
	}
	
	
}
