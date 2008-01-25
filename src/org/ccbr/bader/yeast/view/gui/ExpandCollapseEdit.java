package org.ccbr.bader.yeast.view.gui;

import ding.view.DGraphView;

import javax.swing.undo.AbstractUndoableEdit;
import org.ccbr.bader.yeast.controller.GOSlimmerController;


import undo.Undo;

/**
 * A Ding specific undoable edit.
 */
public class ExpandCollapseEdit extends AbstractUndoableEdit {

	private ExpandedState origState;
	private ExpandedState newState;

	private DGraphView m_view;
    private GOSlimmerController controller;

    private String m_label;

	public ExpandCollapseEdit(GOSlimmerController controller, String label) {
		super();
		this.controller = controller;
        m_label = label;

		saveOldPositions();
	}

	protected void saveOldPositions() {
		origState = new ExpandedState(controller);
	}

	protected void saveNewPositions() {
		newState = new ExpandedState(controller);
	}
    

    public void post() {
		saveNewPositions();
		if ( !origState.equals(newState) ){
            Undo.getUndoableEditSupport().postEdit( this );
        }
    }

	/**
	 * @return Not sure where this is used.
	 */
	public String getPresentationName() {
		return m_label;
	}

	/**
	 * @return Used in the edit menu.
	 */
	public String getRedoPresentationName() {
		return "Redo: " + m_label;
	}

	/**
	 * @return Used in the edit menu.
	 */
	public String getUndoPresentationName() {
		return "Undo: " + m_label;
	}

	/**
	 * Applies the new state to the view after it has been undone.
	 */
	public void redo() {
		super.redo();
		newState.apply();
	}

	/**
	 * Applies the original state to the view.
	 */
	public void undo() {
		super.undo();
		origState.apply();
	}
}
