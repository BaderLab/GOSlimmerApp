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
 * * Description: Defines a GONamespace enumerated type
 */

package org.ccbr.bader.yeast;

public enum GONamespace{
	MolFun("Molecular Function","GO:0003674","F"),
	BioPro("Biological Process","GO:0008150","P"),
	CelCom("Cellular Component","GO:0005575","C");
	
	/**
	 * The natural language name for the namespace
	 */
	private String name;
	/**
	 * The ID of the root term for this namespace
	 */
	private String rootTermId;
	/**
	 * Abbreviation for this namespace as used in gene association files
	 */
	private String geneAnnotationAbreviation;
	
	GONamespace(String name,String rootTermId,String geneAnnotationAbbreviation) {
		this.name = name;
		this.rootTermId = rootTermId;
		this.geneAnnotationAbreviation = geneAnnotationAbbreviation;
	}

	/**
	 * @return the abbreviation for this namespace used in gene annotation files
	 */
	public String getGeneAnnotationAbreviation() {
		return geneAnnotationAbreviation;
	}

	public String getName() {
		return name;
	}

	public String getRootTermId() {
		return rootTermId;
	}

}