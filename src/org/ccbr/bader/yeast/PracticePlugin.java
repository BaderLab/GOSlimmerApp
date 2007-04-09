package org.ccbr.bader.yeast;


import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;


/**
 * Created by IntelliJ IDEA.
 * User: mikematan
 * Date: Feb 12, 2007
 * Time: 6:03:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PracticePlugin extends CytoscapePlugin 
{
    public PracticePlugin() {
        System.out.println("PracticePlugin!");
        
        //set-up menu options in plugins menu
        JMenu menu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;

        //MCODE submenu
        JMenu submenu = new JMenu("GOSlimmer");

        //MCODE panel
        item = new JMenuItem("Start GOSlimmer");
        item.addActionListener(new GOSlimPanelAction());
        submenu.add(item);
        menu.add(submenu);
    }

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		super.activate();
		//cytoscape.cruft.obo.BiologicalProcessAnnotationReader meh = new cytoscape.cruft.obo.BiologicalProcessAnnotationReader(null, meh);
		
	}
}
