package org.ccbr.bader.yeast.export;

import java.util.List;
import java.util.ArrayList;

/**This is a simple representation of a OBO 1.2 GO term entry.
 *
 * @author laetitiamorrison
 *
 */
public class GOTermEntry {

    private static final String lsep = System.getProperty("line.separator");

    //required
    private String id;
    private String name;
    private String namespace;

    //optional tag following the id tag
    private List<String> alt_id;
    private String def;
    private List<String> def_origin;
    private String comment;
    private List<String> subset;  //must be defined by a subsetdef entry in the header;  that'll complicate writing as we go
    private List<String> synonym;  //synonymtypedef must be defined in the header, if a synonymtype is used
    private List<String> xref;
    private List<String> disjoint_from;
    private List<String> is_a;
    private List<String> relationship;

    /*note that this is not captured in CyAttributes by TableImport, but rather represented in the graph structure in 'part_of' relationships.
          * These 'part_of' edges should be transcribed into lines like "relationship: part_of GO:0042274"
          */

    public GOTermEntry(String id, String name, String namespace) {
        this(id, name, namespace, null, "", null, "", null, null,  null, null, null, null);
    }

    public GOTermEntry(String id, String name, String namespace, List<String> alt_id, String def, List<String> def_origin, String comment, List<String> subsets, List<String> synonym, List<String> xref, List<String> disjoint_from, List<String> is_a, List<String> relationships) {
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.alt_id = alt_id;
        this.def = def;
        this.def_origin = def_origin;
        this.comment = comment;
        this.subset = subsets;
        this.synonym = synonym;
        this.xref = xref;
        this.disjoint_from = disjoint_from;
        this.is_a = is_a;
        this.relationship = relationships;

        // if any lists are null, create new lists
        if (this.alt_id == null) {
            this.alt_id = new ArrayList<String>();
        }
        if (this.def_origin == null) {
            this.def_origin = new ArrayList<String>();
        }
        if (this.subset == null) {
            this.subset = new ArrayList<String>();
        }
        if (this.synonym == null) {
            this.synonym = new ArrayList<String>();
        }
        if (this.xref == null) {
            this.xref = new ArrayList<String>();
        }
        if (this.disjoint_from == null) {
            this.disjoint_from = new ArrayList<String>();
        }
        if (this.is_a == null) {
            this.is_a = new ArrayList<String>();
        }
        if (this.relationship == null) {
            this.relationship = new ArrayList<String>();
        }
    }


    // Get methods

    /*
     * Method to return the GO term id
     * @return id of the GO term
     */
    public String getId() {
        return id;
    }

    /*
     * Method to return the GO term name
     * @return name of the GO term
     */
    public String getName() {
        return name;
    }

    /*
     * Method to return the GO term namespace
     * @return namespace of the GO term
     */
    public String getNamespace() {
        return namespace;
    }

    /*
     * Method to return the GO term alt ids
     * @return a list of the alt ids of the GO term
     */
    public List<String> getAlt_id() {
        return alt_id;
    }

    /*
     * Method to return the GO term definition
     * @return definition of the GO term
     */
    public String getDef() {
        return def;
    }

    /*
     * Method to return the origin of the GO term definition
     * @return list of origins of the definition of the GO term
     */
    public List<String> getDef_origin() {
        return def_origin;
    }

    /*
     * Method to return the GO term comment
     * @return comment of the GO term
     */
    public String getComment() {
        return comment;
    }

    /*
     * Method to return the subset of the GO term
     * @return subset of the GO term
     */
    public List<String> getSubset() {
         return subset;
     }

    /*
     * Method to return the GO term synonyms
     * @return list of the GO term synonyms
     */
    public List<String> getSynonym() {
        return synonym;
    }

    /*
     * Method to return the GO term Xref
     * @return Xref of the GO term
     */
    public List<String> getXref() {
        return xref;
    }

    /*
     * Method to return the list of terms this GO term is disjoint from
     * @return list of terms this GO term is disjoint from
     */
    public List<String> getDisjoint_from() {
        return disjoint_from;
    }

    /*
     * Method to return the GO term ids for which this GO term satisfies the 'is_a' relationship
     * @return list of GO term ids for which this GO term satisfies the 'is_a' relationship
     */
    public List<String> getIs_a() {
        return is_a;
    }

    /*
     * Method to return the relationships for this GO term other than the 'is_a' relationships
     * @return the list of relationships for this GO term other than the 'is_a' relationships
     */
    public List<String> getRelationship() {
        return relationship;
    }

    // Set Methods

    /*
     * Method to set the GO term id
     * @param id id of the GO term
     */
    public void setId(String id) {
        this.id = id;
    }

    /*
     * Method to set the GO term name
     * @param name name of the GO term
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * Method to set the GO term namespace
     * @param ns namespace of the GO term
     */
    public void setNamespace(String ns) {
        this.namespace = ns;
    }

    /*
     * Method to set the GO term alt ids
     * @param alt_id list of alternate ids for this GO term
     */
    public void setAlt_id(List<String> alt_id) {
        this.alt_id = alt_id;
    }

    /*
     * Method to set the GO term definition
     * @param def definition of the GO term
     */
    public void setDef(String def) {
        this.def = def;
    }

    /*
     * Method to set the origin of the defintion of this GO term
     * @param def_origin list of origins of the definition of this GO term
     */
    public void setDef_origin(List<String> def_origin) {
        this.def_origin = def_origin;
    }

    /*
     * Method to set the GO term comment
     * @param comment comment of the GO term
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /*
     * Method to set the GO term subset
     * @param subset subset of this GO term
     */
    public void setSubset(List<String> subset) {
        this.subset = subset;
    }

    /*
     * Method to set the GO term synonyms
     * @param synonym list of synonyms for this GO term
     */
    public void setSynonym(List<String> synonym) {
        this.synonym = synonym;
    }

    /*
     * Method to set the GO term xref
     * @param xref xref of the GO term
     */
    public void setXref(List<String> xref) {
        this.xref = xref;
    }

    /*
     * Method to set the terms this GO term is disjoint from
     * @param disjoint_from list of terms this GO term is disjoint from
     */
    public void setDisjoint_from(List<String> disjoint_from) {
        this.disjoint_from = disjoint_from;
    }

    /*
     * Method to set the GO term ids for which this GO term satisfies the 'is_a' relationship
     * @param is_a list of GO term ids for which this GO term satisfies the 'is_a' relationship
     */
    public void setIs_a(List<String> is_a) {
        this.is_a = is_a;
    }

    /*
     * Method to set the relationships for this GO term other than the 'is_a' relationships
     * @param relationship list of relationships for this GO term other than the 'is_a' relationships
     */
    public void setRelationship(List<String> relationship) {
        this.relationship = relationship;
    }

    // Add methods

    /*
     * Method to add an alt id of this GO term to the current list
     * @param alt_id alt id of this GO term to add to the list
     */
    public void addAlt_id(String alt_id) {
        this.alt_id.add(alt_id);
    }

    /*
     * Method to add a definition origin for this GO term to the current list
     * @param def_origin definition origin for this GO term to add to the list
     */
    public void addDef_origin(String def_origin) {
        this.def_origin.add(def_origin);
    }

    /*
     * Method to add a subset of this GO term to the current list
     * @param subset subset of this GO term to add to the list
     */
    public void addSubset(String subset) {
        this.subset.add(subset);
    }

    /*
     * Method to add a synonym of this GO term to the current list
     * @param synonym synonym of this GO term to add to the list
     */
    public void addSynonym(String synonym) {
        this.synonym.add(synonym);
    }

    /*
     * Method to add an xref of this GO term to the current list
     * @param xref xref of this GO term to add to the list
     */
    public void addXref(String xref) {
        this.xref.add(xref);
    }

    /*
     * Method to add a term that this GO term is disjoint from to the current list
     * @param disjoint_from term that this GO term is disjoint from to add to the list
     */
    public void addDisjoint_from(String disjoint_from) {
        this.disjoint_from.add(disjoint_from);
    }

    /*
     * Method to add a GO term id with which this GO term satisfies the 'is_a' relationship to the current list
     * @param is_a_id id of a GO term with which this GO term satisfies the 'is_a' relationship to add to the list
    */
    public void addIs_a(String is_a_id) {
        this.is_a.add(is_a_id);
    }

    /*
     * Method to add a relationship for this GO term (other than the 'is_a' relationships) to the current list
     * @param relationship relationship for this GO term (other than the 'is_a' relationships) to add to the list
     */
    public void addRelationship(String relationship) {
        this.relationship.add(relationship);
    }

    /*
     * Method to print the GO term information in OBO format to the standard output.
     * This method is used for testing purposes.
     */
    public void print() {
         
        System.out.print(lsep + "[Term]" + lsep);
        System.out.print("id: " + id + lsep);
        System.out.print("name: " + name + lsep);
        System.out.print("namespace: " + namespace + lsep);

        if (alt_id != null) {
            for (String termAltId : alt_id) {
                if (termAltId !=null) {
                    System.out.print("alt_id: " + termAltId + lsep);
                }
            }
        }

        String definition = "";
        if (def != null && !def.equals("")) {
            definition = "def: " + def;
            if (def_origin != null) {
                definition = definition + " " + def_origin.toString();
            }
            System.out.print(definition + lsep);
        }

        if (comment != null && !comment.equals("")) {
            System.out.print("comment: " + comment + lsep);
        }

        if (subset != null) {
            for (String termSubset : subset) {
                if (termSubset != null) {
                    System.out.print("subset: " + termSubset + lsep);
                }
            }
        }

        if (synonym != null) {
            for (String termSynonym : synonym) {
                if (termSynonym != null) {
                    System.out.print("synonym: " + termSynonym + lsep);
                }
            }
        }

        if (xref != null) {
            for (String termXRef : xref) {
                if (termXRef != null) {
                    System.out.print("xref: " + termXRef + lsep);
                }
            }
        }

        if (disjoint_from != null) {
            for (String termDisjointFrom: disjoint_from) {
                if (termDisjointFrom != null) {
                    System.out.println("disjoint_from: " + termDisjointFrom + lsep);
                }
            }
        }
        
        if (is_a != null) {
            for (String termIs_a: is_a) {
                if (termIs_a != null) {
                    System.out.print("is_a: " + termIs_a + lsep);
                }
            }
        }

        if (relationship != null) {
            for (String termRelationship : relationship) {
                if (termRelationship != null) {
                    System.out.print("relationship: " + termRelationship + lsep);
                }
            }
        }

    }

}
