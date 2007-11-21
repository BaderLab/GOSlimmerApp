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
 * * Description: Defines the visual style for GOSlimmer
 */

package org.ccbr.bader.yeast;

import java.awt.Color;
import java.awt.Shape;
import java.util.List;

import org.ccbr.bader.yeast.view.gui.GOSlimmerGUIViewSettings;

import giny.model.Node;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.ShapeNodeRealizer;
import cytoscape.visual.VisualStyle;

/**Defines the visual style used in GOSlimmer's GO Namespace subgraphs.  In particular, it defines the distinct colours for
 * nodes in the slim set and out of the slim set, and sizes nodes according to how many annotation file or user gene set genes 
 * are annotated by the associated GO term.
 * 
 * @author mikematan
 *
 */
public class GOSlimmerVisualStyle extends VisualStyle {

	public GOSlimmerVisualStyle(String name) {
		super(name);
		this.setNodeAppearanceCalculator(new GoSlimmerNodeAppearanceCalculator());
		// TODO Auto-generated constructor stub
	}

	public GOSlimmerVisualStyle(VisualStyle toCopy) {
		super(toCopy);
		this.setNodeAppearanceCalculator(new GoSlimmerNodeAppearanceCalculator());
		// TODO Auto-generated constructor stub
	}

	public GOSlimmerVisualStyle(VisualStyle toCopy, String newName) {
		super(toCopy, newName);
		this.setNodeAppearanceCalculator(new GoSlimmerNodeAppearanceCalculator());
		// TODO Auto-generated constructor stub
	}

	public GOSlimmerVisualStyle(String name, NodeAppearanceCalculator nac,
			EdgeAppearanceCalculator eac, GlobalAppearanceCalculator gac) {
		super(name, nac, eac, gac);
		this.setNodeAppearanceCalculator(new GoSlimmerNodeAppearanceCalculator());
		// TODO Auto-generated constructor stub
	}

	/**Calculates the node appearance 
	 * 
	 * @author mikematan
	 *
	 */
	public class GoSlimmerNodeAppearanceCalculator extends NodeAppearanceCalculator {

		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
		@Override
		public NodeAppearance calculateNodeAppearance(Node node, CyNetwork network) {

			NodeAppearance nodeAppearance = new NodeAppearance();
			modifyNodeAppearance(nodeAppearance, node, network);
			
			return nodeAppearance;
			//return new GoSlimmerNodeAppearance(numDirectlyCoveredGenes,numDirectlyCoveredGenes);

		}
		


		
		private static final int minNodeSize = 2;
		
		/**
		 * The colour which will be applied to nodes which are selected for inclusion in the slim set
		 */
		private final Color selectedNodeColor = Color.CYAN;
		/**
		 * The colour which will be applied to nodes which are not selected for inclusion in the slim set
		 */
		private final Color unselectedNodeColor = new Color(255,150,150);
		
		@Override
		public void calculateNodeAppearance(NodeAppearance appr, Node node, CyNetwork network) {
			
			modifyNodeAppearance(appr, node, network);
			
			appr.applyBypass(node);

		}
		
		private static final int maxNodeLabelLength = 25;
		
		private boolean isSelectedForSlimSet(Node node) {
			Boolean isSelected = nodeAtt.getBooleanAttribute(node.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName);
			if (isSelected == null) return false;
			return isSelected;
		}
		
		/**Resizes nodes according to how many genes from either the user gene set or the full annotation file gene set (depending on user's advanced view 
		 * settings) are annotated by the GO term represented by the node.
		 * @param appr the node appearance object associated with the node
		 * @param node the node who's appearance is to be altered
		 * @param network the network which the node belongs to
		 */
		private void modifyNodeAppearance(NodeAppearance appr, Node node, CyNetwork network) {
			
			final int numDirectlyCoveredGenes = GOSlimmerUtil.getNumGenesCoveredByGoNode(node, GOSlimmerGUIViewSettings.includeDescendantInferredCoveredGenesInNodeSizeCalculations,GOSlimmerGUIViewSettings.sizeNodesBasedOnUserGeneAnnotation);
			
			double nodeDim = numDirectlyCoveredGenes>0?numDirectlyCoveredGenes*minNodeSize:minNodeSize;
			nodeDim +=1; //to ensure we don't get any negative values when we calculate the logarithm
			if (nodeDim >1) nodeDim = Math.log(nodeDim);
			nodeDim *= 10;

			appr.setLabel(node.getIdentifier());
			appr.setHeight(nodeDim);
			appr.setWidth(nodeDim);
			appr.setShape(ShapeNodeRealizer.ELLIPSE);
			
			
			if (isSelectedForSlimSet(node)) {
				appr.setFillColor(selectedNodeColor);
			}
			else {
				//appr.setFillColor(Color.RED);
				appr.setFillColor(unselectedNodeColor);
			}
			
			
			if (GOSlimmerGUIViewSettings.labelNodesWithOntologyName) {
				String ontname = nodeAtt.getStringAttribute(node.getIdentifier(), "ontology.name");
				//only use the first maxNodeLabelLength characters of ontname for the label;  TODO comment out when node tooltips are properly implemented
//				appr.setLabel(ontname.length()<maxNodeLabelLength?ontname:ontname.substring(0, maxNodeLabelLength));
//				appr.setToolTip(ontname);
				appr.setLabel(ontname);
			}
			else {
				appr.setLabel(node.getIdentifier());
			}

            if (GOSlimmerGUIViewSettings.showGODefinitionAsToolTip) {
                String defn = nodeAtt.getStringAttribute(node.getIdentifier(), "ontology.def");
                //System.out.println(node.getIdentifier() + ":" + defn);

                if (defn!= null) {

                    int curLength = 0;
                    int index = 0;
                    boolean prevWhiteSpace = true;
                    String newDefn = "";
                    String curWord = "";
                    int maxSize = GOSlimmerGUIViewSettings.showGODefinitionAsToolTipSize;


                    while (index < defn.length()) {
                        char c = defn.charAt(index);
                        index = index + 1;

                        boolean curWhiteSpace = Character.isWhitespace(c);

                        if (!curWhiteSpace) {
                            curWord = curWord + c;
                        } else if (!prevWhiteSpace) {
                            int tempLength = curWord.length();
                            if ((curLength + tempLength) > maxSize) {
                                newDefn = newDefn + "\n" + curWord + c;
                                curLength = tempLength + 1;
                            } else if ((curLength + tempLength) == maxSize) {
                                newDefn = newDefn + curWord + "\n";
                                curLength = 0;
                            } else {
                                newDefn = newDefn + curWord + c;
                                curLength = curLength + tempLength + 1;
                            }
                            prevWhiteSpace = true;
                            curWord = "";

                        }
                        prevWhiteSpace = curWhiteSpace;

                    }
                    // handle what's in last word
                    int tempLength = curWord.length();
                    if ((curLength + tempLength) > maxSize) {
                        newDefn = newDefn + "\n" + curWord;
                    }
                    else {
                        newDefn = newDefn + curWord;
                    }
                                
                    appr.setToolTip(newDefn);
                }
                else {
                    appr.setToolTip("");
                }
              
            }
            else {
                appr.setToolTip("");

            }

        }
		
	}

}
