package org.janelia.model.domain.enums.algorithms;

import org.janelia.model.domain.enums.NamedEnum;

public enum AlignmentAlgorithm implements NamedEnum {

	CENTRAL("Peng Central Brain 40x Alignment"),
	OPTIC("FlyLight Optic Lobe 40x Alignment"),
	WHOLE_40X("FlyLight Whole Brain 40x Alignment"),
	WHOLE_40X_IMPROVED("FlyLight Whole Brain 40x Improved Alignment"),
	WHOLE_63X("FlyLight Whole Brain 63x Alignment"),
	CONFIGURED("Configured Script Alignment"),
	CONFIGURED_PAIR("Configured Pair Alignment"),
	CONFIGURED_BRAIN_VNC("Configured Brain/VNC Alignment"),
	CONFIGURED_VNC("Configured VNC Alignment"),
	CONFIGURED_VNC_MASK("Configured VNC Mask Search Alignment");

	private String name;
	
	AlignmentAlgorithm(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
