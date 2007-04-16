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

	Set<Node> slimGoNodes = new HashSet<Node>();
	
	Set<String> inferredCoveredGeneIds = new HashSet<String>();
	Set<String> directlyCoveredGeneIds = new HashSet<String>();
	
	
	public void addToSlimSet(Node goNode) {
		String goid = goNode.getIdentifier();
		if (slimGoNodes.contains(goNode)) return; // set already contains this go id, so no need to re-add it
		
		/* Update the covered gene set */
		
		//get the count of genes covered by this go term, both directly and by descendant inference
		List<String> directlyCoveredGenes = GOSlimmerUtil.getDirectlyCoveredGenes(goNode);
		List<String> inferredCoveredGenes = GOSlimmerUtil.getInferredCoveredGenes(goNode);
		
		inferredCoveredGeneIds.addAll(directlyCoveredGenes);
		inferredCoveredGeneIds.addAll(inferredCoveredGenes);
		
		directlyCoveredGeneIds.addAll(directlyCoveredGenes);
		
		/* Update the fraction covered statistic */
		updateFractionCovered();
		
		/* Update the GO Slim set */
		slimGoNodes.add(goNode);
		System.out.println(fractionInferredCovered);
	}
	
	public void removeFromSlimSet(Node goNode) {
		String goid = goNode.getIdentifier();
		if (!slimGoNodes.contains(goNode)) return; // not in set, so no need to re-add it
		
		/* Update the covered gene set */
		
		//brute force implementation:  iterate through each gene covered by this node, and see if it should be removed
		List<String> coveredGenes = GOSlimmerUtil.getGenesCoveredByGoNode(goNode, true);
		for(String coveredGene:coveredGenes) {
			boolean isStillInferredCovered = false;
			boolean isStillDirectlyCovered = false;
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
//				if (slimInferredCoveredGenes.contains(coveredGene) || slimDirectlyCoveredGenes.contains(coveredGene)) {
//					isStillInferredCovered = true;
//					break; //no need to continue iterating, we've found the gene of interest
//				}
				
				if (slimDirectlyCoveredGenes.contains(coveredGene)) {
					isStillDirectlyCovered = true;
					isStillInferredCovered = true;
				}
				else if (slimInferredCoveredGenes.contains(coveredGene)) {
					isStillInferredCovered = true;
				}
				
				if (isStillDirectlyCovered && isStillInferredCovered) break;
				
				
			}
			if (!isStillInferredCovered) inferredCoveredGeneIds.remove(coveredGene);
			if (!isStillDirectlyCovered) directlyCoveredGeneIds.remove(coveredGene);
		}
		
		/* Update the fraction covered statistic */
		updateFractionCovered();
		
		slimGoNodes.remove(goNode);
		
		System.out.println(fractionInferredCovered);
		
	}
	
	
	private void updateFractionCovered() {
		fractionInferredCovered = ((double) inferredCoveredGeneIds.size())/((double) numGenesTotal);
		fractionDirectlyCovered = ((double) directlyCoveredGeneIds.size())/((double) numGenesTotal);
	}
	
	
	/**
	 * The fraction of genes covered by the term directly and by inference from it's descendant terms' respective coverages
	 */
	private double fractionInferredCovered;
	private double fractionDirectlyCovered;
	
	public double fractionInferredCovered() {
		return fractionInferredCovered;
	}
	
	public double fractionDirectlyCovered() {
		return fractionDirectlyCovered;
	}
	
	//TODO perhaps implement listener interface so that gui can be updated upon change, though this might be better handled by a view or controller component.
	
}
