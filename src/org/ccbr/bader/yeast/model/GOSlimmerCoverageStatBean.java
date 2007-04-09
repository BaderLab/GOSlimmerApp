/**
 * 
 */
package org.ccbr.bader.yeast.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ccbr.bader.yeast.GOSlimmerUtil;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

import giny.model.Node;

/**
 * @author mikematan
 *
 */
public class GOSlimmerCoverageStatBean {

	private CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	private int numGenesTotal;
	
	/**
	 * 
	 */
	public GOSlimmerCoverageStatBean(int numGenesTotal) {
		this.numGenesTotal = numGenesTotal;
	}

	public void addToSlimSet(Node goNode) {
		String goid = goNode.getIdentifier();
		if (slimGoNodes.contains(goNode)) return; // set already contains this go id, so no need to re-add it
		
		/* Update the covered gene set */
		
		//get the count of genes covered by this go term, both directly and by descendant inference
		List<String> coveredGenes = GOSlimmerUtil.getGenesCoveredByGoNode(goNode, true);
		coveredGeneIds.addAll(coveredGenes);
		
		/* Update the fraction covered statistic */
		updateFractionCovered();
		
		/* Update the GO Slim set */
		slimGoNodes.add(goNode);
		System.out.println(fractionCovered);
	}
	
	public void removeFromSlimSet(Node goNode) {
		String goid = goNode.getIdentifier();
		if (!slimGoNodes.contains(goNode)) return; // not in set, so no need to re-add it
		
		/* Update the covered gene set */
		
		//brute force implementation:  iterate through each gene covered by this node, and see if it should be removed
		List<String> coveredGenes = GOSlimmerUtil.getGenesCoveredByGoNode(goNode, true);
		for(String coveredGene:coveredGenes) {
			boolean isStillCovered = false;
			//iterate through remaining slim set goid's, and for each one see if it also contains the gene covered by the goNode being removed
			for (Node slimGoNode:slimGoNodes) {
				if (slimGoNode == goNode) continue;
				//the following commented out code block was commented out and replaced with what follows for purposes of optimization
//				List<String> slimCoveredGenes = GOSlimmerUtil.getGenesCoveredByGoNode(slimGoNode, true);
//				if (slimCoveredGenes.contains(coveredGene)) {
//					isStillCovered = true;
//					
//					break; //no need to continue iterating, we've found the gene of interest
//				}
				List<String> slimDirectlyCoveredGenes = GOSlimmerUtil.getDirectlyCoveredGenes(slimGoNode);
				List<String> slimInferredCoveredGenes = GOSlimmerUtil.getInferredCoveredGenes(slimGoNode);
				if (slimInferredCoveredGenes.contains(coveredGene) || slimDirectlyCoveredGenes.contains(coveredGene)) {
					isStillCovered = true;
					break; //no need to continue iterating, we've found the gene of interest
				}
				
				
			}
			if (!isStillCovered) coveredGeneIds.remove(coveredGene);
		}
		
		/* Update the fraction covered statistic */
		updateFractionCovered();
		
		slimGoNodes.remove(goNode);
		
		System.out.println(fractionCovered);
		
	}
	
	
	private void updateFractionCovered() {
		fractionCovered = ((double) coveredGeneIds.size())/((double) numGenesTotal);
	}
	
	Set<Node> slimGoNodes = new HashSet<Node>();
	
	Set<String> coveredGeneIds = new HashSet<String>();
	
	private double fractionCovered;
	
	public double fractionCovered() {
		return fractionCovered;
	}
	
	//TODO perhaps implement listener interface so that gui can be updated upon change, though this might be better handled by a view or controller component.
	
}
