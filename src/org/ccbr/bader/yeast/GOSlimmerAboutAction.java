package org.ccbr.bader.yeast;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Jun 25, 2008
 * Time: 11:44:00 AM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The action to show the About dialog box
 */
public class GOSlimmerAboutAction implements ActionListener {

    /**
     * Invoked when the about action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        //display about box
        GOSlimmerAboutDialog aboutDialog = new GOSlimmerAboutDialog();
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }
}