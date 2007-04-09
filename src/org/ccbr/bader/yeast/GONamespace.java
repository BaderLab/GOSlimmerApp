/**
 * 
 */
package org.ccbr.bader.yeast;

public enum GONamespace{
	MolFun("Molecular Function"),
	BioPro("Biological Process"),
	CelCom("Cellular Component");
	
	private String name;
	
	GONamespace(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}