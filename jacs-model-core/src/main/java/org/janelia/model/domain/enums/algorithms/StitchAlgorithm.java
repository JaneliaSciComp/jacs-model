package org.janelia.model.domain.enums.algorithms;

import org.janelia.model.domain.enums.NamedEnum;

public enum StitchAlgorithm implements NamedEnum {

	FLYLIGHT("FlyLight RawTile Stitching");
	
	private String name;
	
	private StitchAlgorithm(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
