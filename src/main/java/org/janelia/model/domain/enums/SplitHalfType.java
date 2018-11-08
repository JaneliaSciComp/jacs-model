package org.janelia.model.domain.enums;

/**
 * Type of split half in a Split-GAL4 system.
 */
public enum SplitHalfType implements NamedEnum {

    AD("AD"),
	DBD("DBD");

	private String name;

	SplitHalfType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
