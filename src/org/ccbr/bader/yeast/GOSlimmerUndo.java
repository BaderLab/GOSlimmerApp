package org.ccbr.bader.yeast;

import cytoscape.util.undo.CyUndo;


/**
 * Undo utility for GOSlimmer.  Provides methods to get/set the depth limit of undo.
 *
 * @author laetitiamorrison
 *
 */
public class GOSlimmerUndo extends CyUndo {

    public GOSlimmerUndo() {

    }



    public int getLimit() {
        return undoManager.getLimit();
    }

    public void setLimit(int limit) {
        undoManager.setLimit(limit);
    } 


}
