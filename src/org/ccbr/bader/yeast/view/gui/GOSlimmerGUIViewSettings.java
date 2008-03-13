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
 * * Description: aggregates static fields which hold global view settings   
 */
package org.ccbr.bader.yeast.view.gui;

/**This class will contain global settings for the gui view.  Applications and plugins will be able to programmatically 
 * modify these settings because they are stored as public static fields.
 * 
 * @author mikematan
 *
 */
public class GOSlimmerGUIViewSettings {

	/**
	 * The minimum size which a node in a GOSlimmer subgraph network is allowed to have
	 */
//	public static int minNodeSize = 0;
	
	/**
	 * Setting which determines whether nodes are labelled with their ontology name or with the GO term id
	 */
	public static boolean labelNodesWithOntologyName = true;
	
	/**
	 * Setting which determines whether inferred gene coverage is factored into calculation of node size.  If false, only 
	 * direct gene coverage by the node's GO term is used to calculate node size
	 */
	public static boolean includeDescendantInferredCoveredGenesInNodeSizeCalculations = false;
	
	/**
	 * Setting which determines whether node size is calculated based on coverage by the GO term of the genes in the gene 
	 * association GO annotation file, or if it is to be based on coverage of user specified genes only.
	 */
	public static boolean sizeNodesBasedOnUserGeneAnnotation = false;

    /**
     * Setting which determines whether GO term definition should be shown as a node tool tip.
     */
    public static boolean showGODefinitionAsToolTip = false;

    /**
     * Setting which determines the width (characters) of the GO definition tool tip
     */
    public static int showGODefinitionAsToolTipSize = 50;

    /**
     * Setting which determines what children are displayed when a node is expanded.
     * If the value is false, then all children are shown.  If the value is true, then only those children with
     * associated genes (direct or inferred) will be shown.
     */
    public static boolean expandNodesWithGenes = true;

    /**
     * Setting which determines the maximum width (in characters) of the GO term name labels.  Any labels longer than this
     * width will be wrapped onto multiple lines.
     */
    public static final int formattedOntologyNameMaxLength = 15;


}
