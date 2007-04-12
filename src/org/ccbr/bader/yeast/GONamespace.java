/**
 * 
 */
package org.ccbr.bader.yeast;

public enum GONamespace{
	MolFun("Molecular Function","GO:0003674"),
	BioPro("Biological Process","GO:0008150"),
	CelCom("Cellular Component","GO:0005575");
	
	private String name;
	private String rootTermId;
	
	GONamespace(String name,String rootTermId) {
		this.name = name;
		this.rootTermId = rootTermId;
	}

	public String getName() {
		return name;
	}

	public String getRootTermId() {
		return rootTermId;
	}

}