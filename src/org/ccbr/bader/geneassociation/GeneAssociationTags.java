/**
 * * Copyright (c) 2007 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * * Research, University of Toronto
 * *
 * * Code written by: Keiichiro Ono
 * * Authors: Keiichiro Ono, Michael Matan, Gary D. Bader
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
 * * Description: Defines Gene Association file columns
 */
package org.ccbr.bader.geneassociation;



/**
 * Reserved keywords for Gene Association files.<br>
 * <p>
 * For more information about GA format, please visit:<br>
 * http://www.geneontology.org/GO.annotation.shtml#file
 * </p>
 * @author kono
 *
 */
public enum GeneAssociationTags {
	DB("DB"),
	DB_OBJECT_ID("DB_Object_ID"),
	DB_OBJECT_SYMBOL("DB_Object_Symbol"),
	QUALIFIER("Qualifier"),
	GO_ID("GO ID"),
	DB_REFERENCE("DB:Reference"),
	EVIDENCE("Evidence"),
	WITH_OR_FROM("With (or) From"),
	ASPECT("Aspect"),
	DB_OBJECT_NAME("DB_Object_Name"),
	DB_OBJECT_SYNONYM("DB_Object_Synonym"),
	DB_OBJECT_TYPE("DB_Object_Type"),
	TAXON("Taxon"),
	DATE("Date"),
	ASSIGNED_BY("Assigned_by");

	private String tag;

	private GeneAssociationTags(String tag) {
		this.tag = tag;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String toString() {
		return tag;
	}

	/**
	 * Since this enum represents a column names, we can find the index of the tag
	 * by using this method.
	 * <br>
	 * @return
	 */
	public int getPosition() {
		GeneAssociationTags[] tags = values();

		for (int i = 0; i < tags.length; i++) {
			if (tags[i] == this) {
				return i;
			}
		}

		return 0;
	}
}
