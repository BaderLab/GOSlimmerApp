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
 * * Description: Exception thrown when an exception occurs during file 
 */
package org.ccbr.bader.yeast.export;

import org.ccbr.bader.yeast.GOSlimmerException;

/**
 * @author mikematan
 *
 */
public class GOSlimmerExportException extends GOSlimmerException {

	/**
	 * 
	 */
	public GOSlimmerExportException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public GOSlimmerExportException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public GOSlimmerExportException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public GOSlimmerExportException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
