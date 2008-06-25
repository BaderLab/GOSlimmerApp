package org.ccbr.bader.yeast;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Jun 25, 2008
 * Time: 11:45:59 AM
 * To change this template use File | Settings | File Templates.
 */

import cytoscape.Cytoscape;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.net.URL;

/**
 * An about dialog box for GOSlimmer
 */
public class GOSlimmerAboutDialog extends JDialog {
    public GOSlimmerAboutDialog() {
        super(Cytoscape.getDesktop(), "About GOSlimmer", false);
        setResizable(false);

        // main panel for dialog box
        JEditorPane editorPane = new JEditorPane();
        editorPane.setMargin(new Insets(10,10,10,10));
        editorPane.setEditable(false);
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.addHyperlinkListener(new HyperlinkAction(editorPane));

        editorPane.setText(
                "<html><body><P align=center><b>GOSlimmer v1.1 (June 2008)</b><BR>" +
                "A Cytoscape PlugIn<BR><BR>" +

                "Version 1.1 by Laetitia Morrison (<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>" +
                "Version 1.0 by Mike Matan (<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>");

        setContentPane(editorPane);

    }

    private class HyperlinkAction implements HyperlinkListener {
        JEditorPane pane;

        public HyperlinkAction(JEditorPane pane) {
            this.pane = pane;
        }

        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                cytoscape.util.OpenBrowser.openURL(event.getURL().toString());
            }
        }
    }
}