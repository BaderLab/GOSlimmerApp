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
 * * Description: Data Model class which maintains statistics on gene set coverage by GO Slim Set selected terms for a GO namespace graph   
 */
package org.ccbr.bader.yeast.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ccbr.bader.yeast.GOSlimmerSession;
import org.ccbr.bader.yeast.GOSlimmerUtil;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.TaskMonitor;

import giny.model.Node;

/**Data Model class which maintains statistics on gene set coverage by GO Slim Set selected terms for a GO namespace graph.
 * The bean maintains information on what fraction of the user and annotation file genes are covered by the slim set.  The 
 * controller must instruct the bean when a node has been added or removed from the slim set, as well as providing information 
 * on the user and annotation genes.
 * 
 * @author mikematan
 *
 */
public class GOSlimmerCoverageStatBean {

	private CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	private int numGenesTotal;
	private int numUserGenesTotal;
	private GOSlimmerSession session;
	
	
	/**Construct a new statbean for the given session with the given total number of genes
	 * @param numGenesTotal the total number of annotation genes for the GO subgraph, used in calculating percent coverage
	 * @param session the session which this bean belongs to
	 */
	public GOSlimmerCoverageStatBean(int numGenesTotal,GOSlimmerSession session) {
		this.numGenesTotal = numGenesTotal;
		this.session = session;
	}

	/**
	 * The GO nodes which are in the slim set
	 */
	Set<Node> slimGoNodes = new HashSet<Node>();
	
	/**
	 * The IDs of genes which are covered by the slim set by inference from descendant terms
	 */
	Set<String> inferredCoveredGeneIds = new HashSet<String>();
	/**
	 * The IDs of genes which are covered by the slim set directly
	 */
	Set<String> directlyCoveredGeneIds = new HashSet<String>();
	
	/**
	 * The IDs of user genes which are covered by the slim set by inference from descendant terms
	 */
	Set<String> inferredCoveredUserGeneIds = new HashSet<String>();
	/**
	 * The IDs of user genes which are covered by the slim set terms directly
	 */
	Set<String> directlyCoveredUserGeneIds = new HashSet<String>();

    Set<String> slimGoNodeNames = new HashSet<String>();    // List of names/ids of selected nodes

    /**
	 * The fraction of annotation file genes covered by the term directly and by inference from it's descendant terms' respective coverages
	 */
	private double fractionInferredCovered;
	/**
	 * The fraction of annotation file genes covered by the term directly
	 */
	private double fractionDirectlyCovered;
	
	/**
	 * The fraction of user genes covered by the term directly and by inference from it's descendant terms' respective coverages
	 */
	private double fractionInferredCoveredUserGenes;
	/**
	 * The fraction of user genes covered by the term directly
	 */
	private double fractionDirectlyCoveredUserGenes;
	
	/**Adds the given node to the list of selected slim term nodes and updates statistics accordingly
	 * @param goNode the node being added to the slim set
	 */
	public void addToSlimSet(Node goNode) {
		String goid = goNode.getIdentifier();
		if (slimGoNodes.contains(goNode)) return; // set already contains this go id, so no need to re-add it
		
		/* Update the covered gene set */
		addCoveredGeneIdsToGeneSets(goNode);
		
		//compute for the user genes now
		if (isUserGeneStatisticsSetup) addCoveredUserGeneIdsToUserGeneSets(goNode);
			
		
		/* Update the fraction covered statistic */
		updateFractionCovered();
		
		/* Update the GO Slim set */
		slimGoNodes.add(goNode);
//		System.out.println(fractionInferredCovered);

        /*Update the GO Slim names set */
        String goTermName = nodeAtt.getStringAttribute(goid,"ontology.name");
        slimGoNodeNames.add(goTermName + " (" + goid + ")");
    }
	
	/**Adds the annotation file genes covered by this go term node to the covered annotation file gene sets 
	 * @param goNode
	 */
	private void addCoveredGeneIdsToGeneSets(Node goNode) {
		//	get the count of genes covered by this go term, both directly and by descendant inference
		List<String> directlyCoveredGenes = GOSlimmerUtil.getDirectlyCoveredGenes(goNode);
		List<String> inferredCoveredGenes = GOSlimmerUtil.getInferredCoveredGenes(goNode);
	
		inferredCoveredGeneIds.addAll(directlyCoveredGenes);
		inferredCoveredGeneIds.addAll(inferredCoveredGenes);
	
		directlyCoveredGeneIds.addAll(directlyCoveredGenes);
	}
	
	
	/**Adds the user genes covered by this go term node to the covered user gene sets 
	 * @param goNode
	 */
	private void addCoveredUserGeneIdsToUserGeneSets(Node goNode) {
		//	get the count of genes covered by this go term, both directly and by descendant inference
		List<String> directlyCoveredUserGenes = GOSlimmerUtil.getDirectlyCoveredUserGenes(goNode);
		List<String> inferredCoveredUserGenes = GOSlimmerUtil.getInferredCoveredUserGenes(goNode);
	
		inferredCoveredUserGeneIds.addAll(directlyCoveredUserGenes);
		inferredCoveredUserGeneIds.addAll(inferredCoveredUserGenes);
	
		directlyCoveredUserGeneIds.addAll(directlyCoveredUserGenes);
	}
	
	/**Removes the given node to the list of selected slim term nodes and updates statistics accordingly.
	 * Note that this is much more expensive an operation than adding a node, because it must ensure that the removed 
	 * gene is not covered by any other selected go term
	 * @param goNode the node being removed to the slim set
     * @param taskMonitor TaskMonitor to be updated with task progress, if applicable
	 */
	public void removeFromSlimSet(Node goNode, TaskMonitor taskMonitor) {

        String goid = goNode.getIdentifier();

        removeNodesGenes(goNode, taskMonitor);
		if (isUserGeneStatisticsSetup) removeNodesUserGenes(goNode, taskMonitor);
		
		/* Update the fraction covered statistics */
		updateFractionCovered();
		
		slimGoNodes.remove(goNode);

        /*Update the GO Slim names set */
        String goTermName = nodeAtt.getStringAttribute(goid,"ontology.name");
        slimGoNodeNames.remove(goTermName + " (" + goid + ")");
    }
	
	/**Removes node's associated user genes from the set of covered user genes, if they are not covered by a still selected node
	 * @param goNode the go node who's user genes are to be removed, if not still covered
     * @param taskMonitor TaskMonitor to be updated with task progress, if applicable
	 */
	private void removeNodesUserGenes(Node goNode, TaskMonitor taskMonitor) {
        if (taskMonitor!=null) {
            taskMonitor.setPercentCompleted(0);
            taskMonitor.setStatus("User genes...");
        }
		if (!slimGoNodes.contains(goNode)) return; // not in set, so no need to remove it
		
		/* Update the covered gene set */

        // Get list of user genes that are covered by other terms in go set
        Set<String> directlyCoveredUserGenes = new HashSet<String>();
        Set<String> inferredCoveredUserGenes = new HashSet<String>();

        for (Node slimNode:slimGoNodes) {
            if (!slimNode.equals(goNode)){
                directlyCoveredUserGenes.addAll(GOSlimmerUtil.getDirectlyCoveredUserGenes(slimNode));
                inferredCoveredUserGenes.addAll(GOSlimmerUtil.getInferredCoveredUserGenes(slimNode));
            }
        }

        // Get list of user genes covered by this node
        Set<String> coveredUserGenes = new HashSet<String>();
        coveredUserGenes.addAll(GOSlimmerUtil.getGenesCoveredByGoNode(goNode, true, true));

        // parameters to update task monitor
        int numGenes = coveredUserGenes.size();
        int count = 0;
        int complete = 0;
        int lastComplete = 0;

        //brute force implementation:  iterate through each gene covered by this node, and see if it should be removed
        for (String gene: coveredUserGenes) {

            // Update task monitor with progress
            if (taskMonitor != null) {
                complete = (int)(((double) count / numGenes) * 100);
                if ((complete!= lastComplete) && ((complete % 10) == 0)) {
                    lastComplete = complete;
                    taskMonitor.setPercentCompleted(complete);
                }
            }

            // determine if this gene is still covered (both directly and inferred)
            if (!directlyCoveredUserGenes.contains(gene)) {
                directlyCoveredUserGeneIds.remove(gene);

                if (!inferredCoveredUserGenes.contains(gene)) {
                    inferredCoveredUserGeneIds.remove(gene);
                }
            }
            count++;
        }
		

		
	}

	/**Removes node's associated annotation file genes from the set of covered user genes, if they are not covered by a still selected node
	 * @param goNode the go node who's annotation file genes are to be removed, if not still covered
     * @param taskMonitor TaskMonitor to be updated with task progress, if applicable
	 */
	private void removeNodesGenes(Node goNode, TaskMonitor taskMonitor) {
        if (taskMonitor!=null) {
            taskMonitor.setPercentCompleted(0);
            taskMonitor.setStatus("All genes...");
        }
		if (!slimGoNodes.contains(goNode)) return; // not in set, so no need to remove it
		
		/* Update the covered gene set */

        // Get list of genes that are covered by other terms in go set
        Set<String> directlyCoveredGenes = new HashSet<String>();
        Set<String> inferredCoveredGenes = new HashSet<String>();

        for (Node slimNode:slimGoNodes) {
            if (!slimNode.equals(goNode)){
                directlyCoveredGenes.addAll(GOSlimmerUtil.getDirectlyCoveredGenes(slimNode));
                inferredCoveredGenes.addAll(GOSlimmerUtil.getInferredCoveredGenes(slimNode));
            }
        }

        // Get list of genes covered by this node
        Set<String> coveredGenes = new HashSet<String>();
        coveredGenes.addAll(GOSlimmerUtil.getGenesCoveredByGoNode(goNode, true));

        // parameters to update task monitor
        int numGenes = coveredGenes.size();
        int count = 0;
        int complete = 0;
        int lastComplete = 0;


        //brute force implementation:  iterate through each gene covered by this node, and see if it should be removed
        for (String gene: coveredGenes) {

            // Update task monitor with progress
            if (taskMonitor != null) {
                complete = (int)(((double) count / numGenes) * 100);
                if ((complete!= lastComplete) && ((complete % 10) == 0)) {
                    lastComplete = complete;
                    taskMonitor.setPercentCompleted(complete);
                }
            }

            // determine if this gene is still covered (both directly and inferred)
            if (!directlyCoveredGenes.contains(gene)) {
                directlyCoveredGeneIds.remove(gene);

                if (!inferredCoveredGenes.contains(gene)) {
                    inferredCoveredGeneIds.remove(gene);
                }
            }
            count++;
        }



	}


	/**
	 * Updates the stats on the fraction of genes covered based on the sets of user and annotation file genes covered by the 
	 * slim set terms.  This method is not automatically called on adding/removing operations since it would impact performance.
	 */
	private void updateFractionCovered() {
		fractionInferredCovered = ((double) inferredCoveredGeneIds.size())/((double) numGenesTotal);
		fractionDirectlyCovered = ((double) directlyCoveredGeneIds.size())/((double) numGenesTotal);
		if (isUserGeneStatisticsSetup) fractionDirectlyCoveredUserGenes = ((double) directlyCoveredUserGeneIds.size())/((double) numUserGenesTotal);
		if (isUserGeneStatisticsSetup) fractionInferredCoveredUserGenes = ((double) inferredCoveredUserGeneIds.size())/((double) numUserGenesTotal);
	}
	
	

	
	public double fractionInferredCovered() {
		return fractionInferredCovered;
	}
	
	public double fractionDirectlyCovered() {
		return fractionDirectlyCovered;
	}

	public double fractionDirectlyCoveredUserGenes() {
		return fractionDirectlyCoveredUserGenes;
	}
	
	public double fractionInferredCoveredUserGenes() {
		return fractionInferredCoveredUserGenes;
	}
	
	
	private boolean isUserGeneStatisticsSetup = false;
	
	/**Initializes/reinitializes the user gene coverage statistics
	 * @param numUserGenesTotal the total number of user genes which could possibly be covered.
	 */
	public void setupUserGeneStatistics(int numUserGenesTotal) {
		isUserGeneStatisticsSetup = true;
		this.numUserGenesTotal = numUserGenesTotal;
		inferredCoveredUserGeneIds = new HashSet<String>();
		directlyCoveredUserGeneIds = new HashSet<String>();
		for(Node node:slimGoNodes) {
			this.addCoveredUserGeneIdsToUserGeneSets(node);
		}
		updateFractionCovered();
		
	}

	public Set<Node> getSlimGoNodes() {
		return slimGoNodes;
	}

    public String[] getListSelectedGONodes() {
        String[] returnArray = new String[slimGoNodeNames.size()];
        return slimGoNodeNames.toArray(returnArray);
    }

    //TODO perhaps implement listener interface so that gui can be updated upon change, though this might be better handled by a view or controller component.
	
}
