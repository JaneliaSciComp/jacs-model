package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.flyem.EMBody;
import org.janelia.model.domain.flyem.EMDataSet;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for accessing EM information synchronized from FlyEM neuPrint.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface EmBodyDao extends ReadDao<EMBody, Long>, WriteDao<EMBody, Long> {

    /**
     * Returns all of the bodies associated with a given data set.
     * @param emDataSet
     * @return
     */
    List<EMBody> getBodiesForDataSet(EMDataSet emDataSet);

    /**
     * Stream all of the bodies associated with a given data set.
     * @param emDataSet
     * @return
     */
    Stream<EMBody> streamBodiesForDataSet(EMDataSet emDataSet);
}
