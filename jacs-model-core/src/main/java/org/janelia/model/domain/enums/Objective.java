package org.janelia.model.domain.enums;

public enum Objective implements NamedEnum {

    OBJECTIVE_10X("10x"),
	OBJECTIVE_20X("20x"),
	OBJECTIVE_25X("25x"),
	OBJECTIVE_40X("40x"),
	OBJECTIVE_63X("63x");
	
	private String name;
	
	private Objective(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
