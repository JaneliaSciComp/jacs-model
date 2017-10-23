package org.janelia.it.jacs.model.domain.interfaces;

import java.util.List;

import org.janelia.it.jacs.model.domain.sample.PipelineResult;

public interface HasResults {

    List<PipelineResult> getResults();

    Long getDiskSpaceUsage();
}
