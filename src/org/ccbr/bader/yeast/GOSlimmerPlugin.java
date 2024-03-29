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
 * * Description: Cytoscape plugin class for GOSlimmer
 */

package org.ccbr.bader.yeast;


import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;


/**
 * User: mikematan
 * Date: Feb 12, 2007
 * Time: 6:03:32 PM
 */
public class GOSlimmerPlugin extends CytoscapePlugin 
{
    public GOSlimmerPlugin() {
        System.out.println("PracticePlugin!");
        
        //set-up menu options in plugins menu
        JMenu menu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;

        //GOSlimmer submenu
        JMenu submenu = new JMenu("GOSlimmer");

        GOSlimPanelAction actionListener = new GOSlimPanelAction();
        
        //GOSlimmer panel
        //add the start goslimmer option
        item = new JMenuItem("Start GOSlimmer");
        item.addActionListener(actionListener);
        submenu.add(item);
        //add the exit goslimmer option
        item = new JMenuItem("Exit GOSlimmer");
        item.addActionListener(actionListener);
        submenu.add(item);

        //About box
        item = new JMenuItem("About");
        item.addActionListener(new GOSlimmerAboutAction());
        submenu.add(item);       

        menu.add(submenu);
    }

	@Override
	public void activate() {
		super.activate();
		
	}
}
