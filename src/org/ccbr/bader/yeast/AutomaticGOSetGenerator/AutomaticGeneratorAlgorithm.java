package org.ccbr.bader.yeast.AutomaticGOSetGenerator;

/**
 * Class to automatically generate a set of go terms with complete coverage of the gene set (covering set), or to
 * to generate the list of GO terms with the best gene coverage.
 * User: lmorrison
 * Date: Dec 20, 2007
 * Time: 3:55:54 PM
 */


import cytoscape.Cytoscape;
import cytoscape.CyNetwork;

import org.ccbr.bader.yeast.GOSlimmerUtil;
import org.ccbr.bader.yeast.GOSlimmerSession;

import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

import giny.model.Node;
import giny.view.EdgeView;
import ding.view.DGraphView;

public class AutomaticGeneratorAlgorithm {

    private GOSlimmerSession session;

    public AutomaticGeneratorAlgorithm(GOSlimmerSession session) {
        this.session = session;
    }

    /**
     * Returns a set of GO term nodes which provides 100% coverage of the associated genes (if possible).
     * If a user gene set has been imported, than the gene set considered for coverage is the user gene set.
     * Otherwise, the returned set of nodes covers the complete gene set.
     * @param selectedGONodes the set of GO term nodes which have already been selected
     * @return the set of GO term nodes which provide a covering set for the associated genes
     */
    public Set<Node> getCoveringSet(Set<Node> selectedGONodes) {

        Set<Node> coveringSet = new HashSet<Node>(selectedGONodes);

        // Determine the gene set for which the covering set should be determined
        Set<String> geneIds = null;
        boolean userGenesImported = session.isUserGeneSetImported();
        if (userGenesImported) {  // user gene set was imported, so use user gene set
            geneIds = new HashSet<String>(session.getUserGeneSet());
        }
        else { // no user gene set, use all genes
            geneIds = session.getGaru().getGeneIds();
        }

        // Loop until all the genes are covered, or there is no other node that can increase the current gene coverage.
        Node nextBestNode = getNextBestTerm(coveringSet, geneIds, userGenesImported);
        while (nextBestNode != null) {
            coveringSet.add(nextBestNode);
            nextBestNode = getNextBestTerm(coveringSet, geneIds, userGenesImported);
        }
        
        return coveringSet;
    }

    /**
     * Returns an array of strings representing the GO terms with the best direct coverage of the remaining uncovered
     * genes. Each string in the array is in the format <GOTerm name> (<GOTerm id>): <Number of remaining genes covered>.
     * If a user gene set has been imported, than the gene set considered for coverage is the user gene set.
     * Otherwise, the returned strings represent the nodes with the best coverage of the complete gene set.
     * @param selectedGONodes the set of GO term nodes which have already been selected
     * @param numTerms the number of terms to return in the array
     * @return an array of strings representing the GO terms with the best direct coverage of the remaining uncovered genes.
     * The size of the array will be the minimum of numTerms and the number of GOTerms with a gene coverage greater than 0.
     */
    public String[] getTopTermsStringsDirect(Set<Node> selectedGONodes, int numTerms) {
        CyNetwork network = Cytoscape.getCurrentNetwork();

        Set<String> geneIds = null;

        // Determine the gene set for which coverage of Go terms should be determined
        boolean userGenesImported = session.isUserGeneSetImported();
        if (userGenesImported) {  // user gene set was imported, so use user gene set
            geneIds = new HashSet<String>(session.getUserGeneSet());
        }
        else { // no user gene set, use all genes
            geneIds = session.getGaru().getGeneIds();
        }

        ArrayList<GOSetTerm> goTermList = new ArrayList<GOSetTerm>();

        // Get list of covered genes
        Set<String> coveredGenes = new HashSet<String>();
        for (Node node : selectedGONodes) {
            coveredGenes.addAll(GOSlimmerUtil.listToSet(GOSlimmerUtil.getGenesCoveredByGoNode(node, false, userGenesImported)));
        }

        // Get list of uncovered genes from covered genes and all genes
        Set<String> uncoveredGenes = new HashSet<String>();
        for (String gene:geneIds) {
            if (!coveredGenes.contains(gene)) {
                uncoveredGenes.add(gene);
            }
        }
        int numUncoveredGenes = uncoveredGenes.size();

        // Sort nodes by coverage for remaining genes.

        // Iterate through all nodes in network, and determine the coverage of the remaining uncovered genes.
        Iterator nodes_i = network.nodesIterator();
        while(nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();
            if (!selectedGONodes.contains(node)) {

                Set<String> nodeCoveredGenes = new HashSet<String>(GOSlimmerUtil.getGenesCoveredByGoNode(node, false, userGenesImported));

                Set<String> newCoveredGenes = new HashSet<String>();

                // Select set to iterate through depending on size to cut down on execution time
                if (nodeCoveredGenes.size() < numUncoveredGenes) {
                    for (String gene:nodeCoveredGenes) {
                        if (uncoveredGenes.contains(gene)) {
                            newCoveredGenes.add(gene);
                        }
                    }
                }
                else {
                    for (String gene:uncoveredGenes) {
                        if (nodeCoveredGenes.contains(gene)) {
                            newCoveredGenes.add(gene);
                        }
                    }
                }

                // Create GOSetTerm and add to list if it covers at least one gene
                if (newCoveredGenes.size()>0) {
                    GOSetTerm goSetTerm = new GOSetTerm(node, newCoveredGenes.size());

                    goTermList.add(goSetTerm);
                }

            }
        }

        // Sort list based on number of remaining genes covered
        Collections.sort(goTermList);

        // Build string array to return
        int numSortedTerms = goTermList.size();
        int actualNumTerms = Math.min(numTerms, numSortedTerms);
        String[] goTermsStr = new String[actualNumTerms];
        for (int i=0; i<actualNumTerms; i++) {
            goTermsStr[i] = (goTermList.get(numSortedTerms - 1 - i)).getDescriptiveString();
        }

        return goTermsStr;
    }

    /**
     * Returns an array of strings representing the GO terms with the inferred best coverage of the remaining uncovered
     * genes. Each string in the array is in the format <GOTerm name> (<GOTerm id>): <Number of remaining genes covered>.
     * If a user gene set has been imported, than the gene set considered for coverage is the user gene set.
     * Otherwise, the returned strings represent the nodes with the best coverage of the complete gene set.
     * @param selectedGONodes the set of GO term nodes which have already been selected
     * @param numTerms the number of terms to return in the array
     * @return an array of strings representing the GO terms with the best inferred coverage of the remaining uncovered genes.
     * The size of the array will be the minimum of numTerms and the number of GOTerms with a gene coverage greater than 0.
     */
    public String[] getTopTermsStringsInferred(Set<Node> selectedGONodes, int numTerms) {
        CyNetwork network = Cytoscape.getCurrentNetwork();

        Set<String> geneIds = null;

        // Determine the gene set for which coverage of Go terms should be determined
        boolean userGenesImported = session.isUserGeneSetImported();
        if (userGenesImported) {  // user gene set was imported, so use user gene set
            geneIds = new HashSet<String>(session.getUserGeneSet());
        }
        else { // no user gene set, use all genes
            geneIds = session.getGaru().getGeneIds();
        }

        ArrayList<GOSetTerm> goTermList = new ArrayList<GOSetTerm>();

        // Get list of covered genes
        Set<String> coveredGenes = new HashSet<String>();
        for (Node node : selectedGONodes) {
            coveredGenes.addAll(GOSlimmerUtil.listToSet(GOSlimmerUtil.getGenesCoveredByGoNode(node, true, userGenesImported)));
        }

        // Get list of uncovered genes from covered genes and all genes
        Set<String> uncoveredGenes = new HashSet<String>();
        for (String gene:geneIds) {
            if (!coveredGenes.contains(gene)) {
                uncoveredGenes.add(gene);
            }
        }
        int numUncoveredGenes = uncoveredGenes.size();

        // Sort nodes by coverage for remaining genes.

        // Iterate through all nodes in network, and determine the coverage of the remaining uncovered genes.
        Iterator nodes_i = network.nodesIterator();
        while(nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();
            if (!selectedGONodes.contains(node)) {

                Set<String> nodeCoveredGenes = new HashSet<String>(GOSlimmerUtil.getGenesCoveredByGoNode(node, true, userGenesImported));

                Set<String> newCoveredGenes = new HashSet<String>();

                // Select set to iterate through depending on size to cut down on execution time
                if (nodeCoveredGenes.size() < numUncoveredGenes) {
                    for (String gene:nodeCoveredGenes) {
                        if (uncoveredGenes.contains(gene)) {
                            newCoveredGenes.add(gene);
                        }
                    }
                }
                else {
                    for (String gene:uncoveredGenes) {
                        if (nodeCoveredGenes.contains(gene)) {
                            newCoveredGenes.add(gene);
                        }
                    }
                }

                // Create GOSetTerm and add to list if it covers at least one gene
                if (newCoveredGenes.size()>0) {
                    GOSetTerm goSetTerm = new GOSetTerm(node, newCoveredGenes.size());

                    goTermList.add(goSetTerm);
                }

            }
        }

        // Sort list based on number of remaining genes covered
        Collections.sort(goTermList);

        // Build string array to return
        int numSortedTerms = goTermList.size();
        int actualNumTerms = Math.min(numTerms, numSortedTerms);
        String[] goTermsStr = new String[actualNumTerms];
        for (int i=0; i<actualNumTerms; i++) {
            goTermsStr[i] = (goTermList.get(numSortedTerms - 1 - i)).getDescriptiveString();
        }

        return goTermsStr;
    }

    /**
     * Method to get the GO term node with the best coverage of the genes specified in 'geneIds'.
     * @param selectedGONodes the set of GO term nodes which have already been selected
     * @param geneIds the set of geneIds for which to determine the best coverage
     * @param userGenesImported true if a user gene set has been imported, false otherwise.
     * @return GO term node with the highest coverage of genes specified in 'geneIds'. Ties are broken randomly.
     */
    public Node getNextBestTerm(Set<Node> selectedGONodes, Set<String> geneIds, boolean userGenesImported) {
        //return getNextBestTermInferred(selectedGONodes, geneIds, userGenesImported);
        return getNextBestTermDirect(selectedGONodes, geneIds, userGenesImported);
    }

    /**
     * Method to get the GO term node with the best direct coverage of the genes specified in 'geneIds'.
     * @param selectedGONodes the set of GO term nodes which have already been selected
     * @param geneIds the set of geneIds for which to determine the best direct coverage
     * @param userGenesImported true if a user gene set has been imported, false otherwise.
     * @return GO term node with the highest direct coverage of genes specified in 'geneIds'. Ties are broken randomly.
     */
    public Node getNextBestTermDirect(Set<Node> selectedGONodes, Set<String> geneIds, boolean userGenesImported) {

        CyNetwork network = Cytoscape.getCurrentNetwork();

        // Get list of covered genes
        Set<String> coveredGenes = new HashSet<String>();
        for (Node node : selectedGONodes) {
            coveredGenes.addAll(GOSlimmerUtil.listToSet(GOSlimmerUtil.getGenesCoveredByGoNode(node, false, userGenesImported)));
        }

        // Get list of uncovered genes from covered genes and all genes
        Set<String> uncoveredGenes = new HashSet<String>();
        for (String gene:geneIds) {
            if (!coveredGenes.contains(gene)) {
                uncoveredGenes.add(gene);
            }
        }
        int numUncoveredGenes = uncoveredGenes.size();

        // Rank nodes by coverage for remaining nodes.

        int topRank = -1;
        Node topRankedNode = null;

        Iterator nodes_i = network.nodesIterator();
        while(nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();
            if (!selectedGONodes.contains(node)) {

                Set<String> nodeCoveredGenes = new HashSet<String>(GOSlimmerUtil.getGenesCoveredByGoNode(node, false, userGenesImported));

                Set<String> newCoveredGenes = new HashSet<String>();

                // Select set to iterate through depending on size to cut down on execution time
                if (nodeCoveredGenes.size() < numUncoveredGenes) {
                    for (String gene : nodeCoveredGenes) {
                        if (uncoveredGenes.contains(gene)) {
                            newCoveredGenes.add(gene);
                        }
                    }
                }
                else {
                    for (String gene : uncoveredGenes) {
                        if (nodeCoveredGenes.contains(gene)) {
                            newCoveredGenes.add(gene);
                        }
                    }
                }

                // Check if this node covers the most uncovered genes so far - if so, mark as top ranked node
                if (newCoveredGenes.size() > topRank) {
                    topRank = newCoveredGenes.size();
                    topRankedNode = node;
                }
            }
        }

        if (topRank==0) {
            return null;
        }
        return topRankedNode;
    }

    /**
     * Method to get the GO term node with the best inferred coverage of the genes specified in 'geneIds'.
     * @param selectedGONodes the set of GO term nodes which have already been selected
     * @param geneIds the set of geneIds for which to determine the best inferred coverage
     * @param userGenesImported true if a user gene set has been imported, false otherwise.
     * @return GO term node with the highest inferred coverage of genes specified in 'geneIds'. Ties are broken randomly.
     */
    public Node getNextBestTermInferred(Set<Node> selectedGONodes, Set<String> geneIds, boolean userGenesImported)  {

         CyNetwork network = Cytoscape.getCurrentNetwork();

        // Get list of covered genes
        Set<String> coveredGenes = new HashSet<String>();
        for (Node node : selectedGONodes) {
            coveredGenes.addAll(GOSlimmerUtil.listToSet(GOSlimmerUtil.getGenesCoveredByGoNode(node, true, userGenesImported)));
        }

        // Get list of uncovered genes from covered genes and all genes
        Set<String> uncoveredGenes = new HashSet<String>();
        for (String gene:geneIds) {
            if (!coveredGenes.contains(gene)) {
                uncoveredGenes.add(gene);
            }
        }
        int numUncoveredGenes = uncoveredGenes.size();

        // Rank nodes by coverage for remaining nodes.  If node is a parent of selected nodes, then rank is 0

        int topRank = -1;
        Node topRankedNode = null;

        Iterator nodes_i = network.nodesIterator();
        while(nodes_i.hasNext()) {

            Node node = (Node) nodes_i.next();
            if (!selectedGONodes.contains(node)) {

                // Check and see if it is a parent of a select node

                if (!isAncestorOfNodeInSet(node, selectedGONodes)) {

                    Set<String> nodeCoveredGenes = new HashSet<String>(GOSlimmerUtil.getGenesCoveredByGoNode(node, true, userGenesImported));

                    Set<String> newCoveredGenes = new HashSet<String>();

                    // Select set to iterate through depending on size to cut down on execution time
                    if (nodeCoveredGenes.size() < numUncoveredGenes) {
                        for (String gene : nodeCoveredGenes) {
                            if (uncoveredGenes.contains(gene)) {
                                newCoveredGenes.add(gene);
                            }
                        }
                    }
                    else {
                        for (String gene : uncoveredGenes) {
                            if (nodeCoveredGenes.contains(gene)) {
                                newCoveredGenes.add(gene);
                            }
                        }
                    }

                    // Check if this node covers the most uncovered genes so far - if so, mark as top ranked node
                    if (newCoveredGenes.size() > topRank) {
                        topRank = newCoveredGenes.size();
                        topRankedNode = node;
                    }
                }
            }
        }

        System.out.println("top ranked node: " + topRankedNode + " with a rank of " + topRank);

        if (topRank==0) {
            return null;
        }
        return topRankedNode;

    }  

    /**
     * Method to determine whether a node is an ancestor of any node in a set.
     * @param node GO term node which may or may not be an ancestor of a node in 'otherNodes'.
     * @param otherNodes set of GO term nodes thay may or may not contain a child of 'node'
     * @return true if 'node' is an ancestor of a node in 'otherNodes', false otherwise.
     */
    private boolean isAncestorOfNodeInSet(Node node, Set<Node> otherNodes) {

        for (Node eachNode:otherNodes) {
            if (isAncestorNode(node, eachNode)) {
                return true;
            }
        }
        return false;
   }

    /**
     * Method to determine whether a node is an ancestor of another node.
     * @param ancestor GO term node which may or may not be an ancestor of 'child'.
     * @param child GO term node with may or may not be a child of 'ancestor'.
     * @return true if 'ancestor' is an ancestor of 'child', false otherwise.
     */
   private boolean isAncestorNode(Node ancestor, Node child) {

       DGraphView view = (DGraphView) Cytoscape.getCurrentNetworkView();

       // Loop through the outgoing edges of child node (to parent nodes), and check if 'target' is the parent node.
       // Then recursively call method on each of the parent nodes.
       int[] outgoingEdges = Cytoscape.getCurrentNetwork().getAdjacentEdgeIndicesArray(child.getRootGraphIndex(), false, false, true);
       for (int outgoingEdge : outgoingEdges) {
           EdgeView ev = view.getEdgeView(outgoingEdge);
           Node parentNode = ev.getEdge().getTarget();
           if (ancestor.equals(parentNode)) {
               return true;
           }
           if (isAncestorNode(ancestor, parentNode)) {
               return true;
           }
       }
       return false;
   }

}
