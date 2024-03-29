package org.ccbr.bader.yeast.export;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

/**This is a simple representation of a OBO 1.2 file header.
 *
 * @author laetitiamorrison
 *
 */
public class GOOBOHeader {

    //required
    String format_version = "1.2";

    //optional
    String data_version;
    String date;  // date in dd:MM:yyyy HH:mm format
    String saved_by;
    String auto_generated_by; // program that generated the file
    String import_url;
    String synonymtypedef;
    String default_namespace;
    List<String> subsetdef; // each subsset def should containt a subset name, space, and a quote enclosed description
    List<String> remark;
    //more defined here: http://www.geneontology.org/GO.format.obo-1_2.shtml

    public GOOBOHeader(String format_version) {
        if (format_version!=null) {
            this.format_version = format_version;
        }
    }

    // Get methods

    /*
     * Method to get the format version of the OBO file
     * @return format version of the OBO file
     */
    public String getFormat_version() {
        return format_version;
    }

    /*
     * Method to get the data version of the OBO file
     * @return data version of the OBO file
     */
    public String getData_version() {
        return data_version;
    }

    /*
     * Method to get the date of the OBO file
     * @return date of the OBO file
     */
    public String getDate() {
        return date;
    }

    /*
     * Method to get the 'saved by' information of the OBO file
     * @return 'saved by' information of the OBO file
     */
    public String getSaved_by() {
        return saved_by;
    }

    /*
     * Method to get the 'auto generated by' information of the OBO file
     * @return 'auto generated by' information of the OBO file
     */
    public String getAuto_generated_by() {
        return auto_generated_by;
    }

    /*
     * Method to get the import url of the OBO file
     * @return import url of the OBO file
     */
    public String getImport_url() {
        return import_url;
    }

    /*
     * Method to get the synonym type definitions of the OBO file
     * @return synonym type definitions of the OBO file
     */
    public String getSynonymtypedef() {
        return synonymtypedef;
    }

    /*
     * Method to get the default namespace of the OBO file
     * @return default namespace of the OBO file
     */
    public String getDefault_namespace() {
        return default_namespace;
    }

    /*
     * Method to get the subset definitions of the OBO file
     * @return subset definitions of the OBO file
     */
    public List<String> getSubsetdef() {
        return subsetdef;
    }

    /*
     * Method to get the remarks of the OBO file
     * @return remarks of the OBO file
     */
    public List<String> getRemark() {
        return remark;
    }

    // Set methods

    /*
     * Method to set the format version of the OBO file
     * @param format_version format version of the OBO file
     */
    public void setFormat_version(String format_version) {
        if (format_version != null){
            this.format_version = format_version;
        }
    }

    /*
     * Method to set the data version of the OBO file
     * @param data_version data version of the OBO file
     */
    public void setData_version(String data_version) {
        this.data_version = data_version;
    }

    /*
     * Method to set the date of the OBO file
     * @param date date of the OBO file
     */
    public void setDate(String date) {
        this.date = date;
    }

    /*
     * Method to set the date of the OBO file
     * @param date date of the OBO file
     */
    public void setDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd:MM:yyyy HH:mm");
        this.date = formatter.format(date);
    }

    /*
     * Method to set the 'saved by' information of the OBO file
     * @param saved_by 'saved by' information of the OBO file
     */
    public void setSaved_by(String saved_by) {
        this.saved_by = saved_by;
    }

    /*
     * Method to set the 'auto generated by' information of the OBO file
     * @param auto_generated_by 'auto generated by' information of the OBO file
     */
    public void setAuto_generated_by(String auto_generated_by) {
        this.auto_generated_by = auto_generated_by;
    }

    /*
     * Method to set the import url of the OBO file
     * @param import_url import url of the OBO file
     */
    public void setImport_url(String import_url) {
        this.import_url = import_url;
    }

    /*
     * Method to set the synonym type definitions of the OBO file
     * @param synonymtypedef synonym type definitions of the OBO file
     */
    public void setSynonymtypedef(String synonymtypedef) {
        this.synonymtypedef = synonymtypedef;
    }

    /*
     * Method to set the default namespaceof the OBO file
     * @param default_namespace default namespace of the OBO file
     */
    public void setDefault_namespace(String default_namespace) {
        this.default_namespace = default_namespace;
    }

    /*
     * Method to set the subset definitions of the OBO file
     * @param subsetdef subset definitions of the OBO file
     */
    public void setSubsetdef(List<String> subsetdef) {
        this.subsetdef = subsetdef;
    }

    /*
     * Method to set the remarks of the OBO file
     * @param remark remarks of the OBO file
     */
    public void setRemark(List<String> remark) {
        this.remark = remark;
    }

    // Add methods

    /*
     * Method to add a subset definition for the OBO file to the current list
     * @param subsetdef subset definition for the OBO file to add to the current list
     */
    public void addSubsetdef(String subsetdef) {
        if (this.subsetdef == null) {
            this.subsetdef = new ArrayList<String>();
        }
        this.subsetdef.add(subsetdef);
    }

    /*
     * Method to add a remark for the OBO file to the current list
     * @param remark remark for the OBO file to add to the current list
     */
    public void addRemark(String remark) {
        if (this.remark == null) {
            this.remark = new ArrayList<String>();
        }
        this.remark.add(remark);
    }

}
