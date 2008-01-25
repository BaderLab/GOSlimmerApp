package org.ccbr.bader.yeast.AutomaticGOSetGenerator;

/**
 * Class which represents a GO term, along with the number of remaining uncovered genes that this term covers.
 * User: lmorrison
 * Date: Jan 11, 2008
 * Time: 10:16:53 AM
 * To change this template use File | Settings | File Templates.
 */

import giny.model.Node;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

public class GOSetTerm implements Comparable {
    private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
    private int maxCharacters = 30;

    private Node goNode;
    private int numRemainingGenesCovered;

    public GOSetTerm(Node goNode, int numRemainingGenesCovered) {
        this.goNode = goNode;
        this.numRemainingGenesCovered = numRemainingGenesCovered;
    }

    public Node getGONode() {
        return goNode;
    }

    public int getNumRemainingGenesCovered() {
        return numRemainingGenesCovered;
    }

    public void setNumRemainingGenesCovered(int numGenes) {
        numRemainingGenesCovered = numGenes;
    }

    public int compareTo(Object anotherGOSetTerm) throws ClassCastException {
        if (!(anotherGOSetTerm instanceof GOSetTerm)) {
            throw new ClassCastException("A GOSetTerm object expected.");
        }
        int anotherTermNumGenes = ((GOSetTerm) anotherGOSetTerm).getNumRemainingGenesCovered();
        return this.numRemainingGenesCovered - anotherTermNumGenes;
    }

    public String toString() {
        String name = nodeAtt.getStringAttribute(goNode.getIdentifier(), "ontology.name");
        if (name.length()>maxCharacters) {
            name = name.substring(0,maxCharacters) + "...";
        }
        return name + " (" + goNode.toString() + "): " + numRemainingGenesCovered;
    }

    /**
     * Method to get a string describing this GOSetTerm.
     * The string format is: <GOTerm name> (<GOTerm id>): <Number of remaining genes covered>
     * @return string describing this GOSetTerm
     */
    public String getDescriptiveString() {
        String name = nodeAtt.getStringAttribute(goNode.getIdentifier(), "ontology.name");
        if (name.length()>maxCharacters) {
            name = name.substring(0,maxCharacters) + "...";
        }
        return name + " (" + goNode.toString() + "): " + numRemainingGenesCovered;
    }
    
}
