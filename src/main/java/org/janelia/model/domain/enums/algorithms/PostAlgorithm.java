package org.janelia.model.domain.enums.algorithms;

import org.janelia.model.domain.enums.NamedEnum;

public enum PostAlgorithm implements NamedEnum {

	BASIC("Basic Post-Processing"),
	ASO("Aso Post-Processing");

	private String name;

	private PostAlgorithm(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
