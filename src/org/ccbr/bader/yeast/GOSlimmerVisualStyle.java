package org.ccbr.bader.yeast;

import java.awt.Color;
import java.awt.Shape;
import java.util.List;

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

	public class GoSlimmerNodeAppearanceCalculator extends NodeAppearanceCalculator {

		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
		@Override
		public NodeAppearance calculateNodeAppearance(Node node, CyNetwork network) {
			// TODO Auto-generated method stub

			final int numDirectlyCoveredGenes = GOSlimmerUtil.getNumGenesCoveredByGoNode(node, false);
			
			double nodeDim = numDirectlyCoveredGenes>0?numDirectlyCoveredGenes*minNodeSize:minNodeSize;
			
			nodeDim +=1; //to ensure we don't get any negative values when we calculate the logarithm
			if (nodeDim >1) nodeDim = Math.log(nodeDim);
			nodeDim *= 10;
			
			NodeAppearance nodeAppearance = new NodeAppearance();
			
			nodeAppearance.setHeight(nodeDim);
			nodeAppearance.setWidth(nodeDim);
			nodeAppearance.setShape(ShapeNodeRealizer.ELLIPSE);
			
			if (isSelectedForSlimSet(node)) { 
				nodeAppearance.setFillColor(selectedNodeColor);
			}
			else {
				//nodeAppearance.setFillColor(Color.RED);
				nodeAppearance.setFillColor(unselectedNodeColor);
			}
			//TODO verify if this commented out statement is unnecessary
			//nodeAppearance.applyBypass(node);
			return nodeAppearance;
			//return new GoSlimmerNodeAppearance(numDirectlyCoveredGenes,numDirectlyCoveredGenes);

		}
		


		
		private static final int minNodeSize = 1;
		
		Color selectedNodeColor = Color.CYAN;
		Color unselectedNodeColor = new Color(255,150,150);
		
		@Override
		public void calculateNodeAppearance(NodeAppearance appr, Node node, CyNetwork network) {
			
			final int numDirectlyCoveredGenes = GOSlimmerUtil.getNumGenesCoveredByGoNode(node, false);
			
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
			appr.applyBypass(node);
			
		}
		
		private boolean isSelectedForSlimSet(Node node) {
			Boolean isSelected = nodeAtt.getBooleanAttribute(node.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName);
			if (isSelected == null) return false;
			return isSelected;
		}
		
		
	}

}
