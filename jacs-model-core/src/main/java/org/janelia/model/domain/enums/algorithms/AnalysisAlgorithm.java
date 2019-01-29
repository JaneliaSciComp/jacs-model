package org.janelia.model.domain.enums.algorithms;

import org.janelia.model.domain.enums.NamedEnum;

public enum AnalysisAlgorithm implements NamedEnum {

	NEURON_SEPARATOR("Gene Myers Neuron Separator"),
    CELL_COUNTING("Cell Counting");
	
	private String name;
	
	private AnalysisAlgorithm(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
