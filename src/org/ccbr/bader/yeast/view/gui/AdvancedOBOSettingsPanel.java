package org.ccbr.bader.yeast.view.gui;

import org.ccbr.bader.yeast.view.gui.misc.JCollapsablePanel;
import org.ccbr.bader.yeast.view.gui.misc.JLabelMod;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**GUI Widget for manipulating advanced settings for OBO export of GOSlimmer information
 *
 *
 * @author laetitiamorrison
 *
 */
public class AdvancedOBOSettingsPanel extends JCollapsablePanel implements ActionListener {

    public AdvancedOBOSettingsPanel() {
        super("Advanced OBO Export Settings");
        username = System.getProperty("user.name");
        if (username == null) {
            username = "go_slimmer_user";
        }
        initComponents();
    }

    private static final String lsep = System.getProperty("line.separator");
    private String username;
    private JLabel userNameLbl;
    private JLabel subsetdefCodeLbl;
    private JLabel subsetdefDefinitionLbl;
    private JLabel subsetdefNameLbl;
    private JTextField userNameTF;
    private JTextField subsetdefCodeTF;
    private JTextField subsetdefDefinitionTF;
    private JTextField subsetdefNameTF;


    // create userNameLbl
    private JLabel getUserNameLbl() {
        if (userNameLbl == null) {
            userNameLbl = new JLabelMod("User Name: ");
        }
        return userNameLbl;
    }

    // create subsetdefCodeLbl
    private JLabel getSubsetdefCodeLbl() {
        if (subsetdefCodeLbl == null) {
            subsetdefCodeLbl = new JLabelMod("Code to user for subsetdef tag: ");
        }
        return subsetdefCodeLbl;
    }

    // create subsetdefDefinitionLbl
    private JLabel getSubsetdefDefinitionLbl() {
        if (subsetdefDefinitionLbl == null) {
            subsetdefDefinitionLbl = new JLabelMod("Definition for subsetdef tag: ");
        }
        return subsetdefDefinitionLbl;
    }

    // create subsetdefNameLbl
    private JLabel getSubsetdefNameLbl() {
        if (subsetdefNameLbl == null) {
            subsetdefNameLbl = new JLabelMod("Name for subsetdef tag: ");
        }
        return subsetdefNameLbl;
    }

    // create userNameTF
    private JTextField getUserNameTF() {
        if (userNameTF == null) {
            userNameTF = new JTextField(10);
            userNameTF.setText(username);
        }
        return userNameTF;
    }

    // create subsetdefCodeTF
    private JTextField getSubsetdefCodeTF() {
        if (subsetdefCodeTF == null) {
            subsetdefCodeTF = new JTextField(10);
            subsetdefCodeTF.setText("goslim_" + username);
        }
        return subsetdefCodeTF;
    }

    // create subsetdefDefinitionTF
    private JTextField getSubsetdefDefinitionTF() {
        if (subsetdefDefinitionTF == null) {
            subsetdefDefinitionTF = new JTextField(10);
            subsetdefDefinitionTF.setText("Custom GO slim created by " + username);
        }
        return subsetdefDefinitionTF;
    }

    // create subsetdefNameTF
    private JTextField getSubsetdefNameTF() {
        if (subsetdefNameTF == null) {
            subsetdefNameTF = new JTextField(10);
            subsetdefNameTF.setText("Custom GO slim");
        }
        return subsetdefNameTF;
    }


    /**
     * Initializes the widget's subcomponents and layout
     */
    private void initComponents() {


        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.FIRST_LINE_START;
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        this.add(getUserNameLbl(), c);

        c.gridwidth = 1;
        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 0;
        this.add(getUserNameTF(), c);

        c.weightx = 0.3;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        this.add(getSubsetdefCodeLbl(), c);

        c.weightx = 1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        this.add(getSubsetdefCodeTF(), c);

        c.weightx = 0.3;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 2;
        this.add(getSubsetdefDefinitionLbl(), c);

        c.weightx = 1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 2;
        this.add(getSubsetdefDefinitionTF(), c);

        c.weightx = 0.3;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        this.add(getSubsetdefNameLbl(), c);

        c.weightx = 1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 3;
        this.add(getSubsetdefNameTF(), c);

    }

    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src instanceof JCheckBox) {

        } else if (src instanceof JTextField) {

        }

    }

    public String getUserName() {
        return userNameTF.getText();
    }

    public String getSubsetdefCode() {
        return subsetdefCodeTF.getText();
    }

    public String getSubsetdefDefinition() {
        return subsetdefDefinitionTF.getText();
    }

    public String getSubsetdefName() {
        return subsetdefNameTF.getText();
    }


}
