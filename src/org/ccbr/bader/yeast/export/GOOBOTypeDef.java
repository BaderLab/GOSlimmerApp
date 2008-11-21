package org.ccbr.bader.yeast.export;

import java.util.List;
import java.util.ArrayList;

/**This is a simple representation of an OBO 1.2 typedef
 *
 * @author laetitiamorrison
 *
 */
public class GOOBOTypeDef {

    //required
    private String id;
    private String name;

    //optional tag following the id tag
    private List<String> xref;
    private Boolean isTransitive;

    public GOOBOTypeDef(String id, String name) {
        this(id, name, null, null);
    }

    public GOOBOTypeDef(String id, String name, List<String> xref, Boolean isTransitive) {

        this.id = id;
        this.name = name;
        this.xref = xref;
        this.isTransitive = isTransitive;

        // if any lists are null, create new lists
        if (this.xref == null) {
            this.xref = new ArrayList<String>();
        }
    }

    // Get methods

    /*
     * Method to get id of the OBO typedef
     * @return id of the OBO typedef
     */
    public String getId() {
        return id;
    }

    /*
     * Method to get the name of the OBO typedef
     * @return name of the OBO typedef
     */
    public String getName() {
        return name;
    }

    /*
     * Method to return the typedef Xref
     * @return Xref of the typedef
     */
    public List<String> getXref() {
        return xref;
    }

    /*
     * Method to return the transitive state of the typedef.
     * @return true if this typedef is transitive, false otherwise
     */
    public Boolean getIsTransitive() {
        return isTransitive;
    }

    // Set methods

    /*
     * Method to set the id of the OBO typedef
     * @param id id of the OBO typedef
     */
    public void setId(String id) {
        this.id = id;
    }

    /*
     * Method to set the name of the OBO typedef
     * @param name name of the OBO typedef
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * Method to set the typedef Xref
     * @param xref xref of the typedef
     */
    public void setXref(List<String> xref) {
        this.xref = xref;
    }

    /*
     * Method to set the transitive state of the typedef
     * @param isTransitive true if this typedef is transitive, false otherwise
     */
    public void setIsTransitive(Boolean isTransitive) {
        this.isTransitive = isTransitive;
    }

    // Add methods

    /*
     * Method to add an xref for the typedef to the current list
     * @param xref xref of the typedef to add to the current list
     */
    public void addXref(String xref) {
        if (this.xref == null) {
            this.xref = new ArrayList<String>();
        }
        this.xref.add(xref);
    }
}
