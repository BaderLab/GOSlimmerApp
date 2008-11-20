package org.ccbr.bader.yeast.export;

/**
 * Filter to show only directories and files with the extension '.obo'
 * User: lmorrison
 * Date: Aug 19, 2008
 * Time: 2:50:09 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.File;
import javax.swing.filechooser.*;

public class OBOExtensionFileFilter extends FileFilter {

    /*
     * Method to determine what should be accepted by this filter.
     * Accept all directories and all '.obo.' files
     * @param f file to accept or reject
     * @return true if file is accepted, false otherwise
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        return (extension != null && extension.equals("obo"));
    }

    /*
     * Method to return the description of this filter
     * @return description of the filter
     */
    public String getDescription() {
        return "Just .obo files.";
    }

    /*
     * Method to return the extension of the file
     * @param f file for which to return the extension
     * @return extension of the file
     */
    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}
