package org.janelia.model.domain.interfaces;

import java.util.List;

import org.janelia.model.domain.sample.PipelineResult;

public interface HasResults {

    List<PipelineResult> getResults();

    Long getDiskSpaceUsage();
}
