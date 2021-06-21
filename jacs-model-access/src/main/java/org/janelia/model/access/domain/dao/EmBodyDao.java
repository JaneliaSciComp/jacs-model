package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.flyem.EMBody;
import org.janelia.model.domain.flyem.EMDataSet;

import java.util.List;
import java.util.Set;
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
     * @param offset
     * @param length
     * @return
     */
    List<EMBody> getBodiesForDataSet(EMDataSet emDataSet, long offset, int length);

    /**
     * Returns all bodies associated with the specified dataset and have a name in the selected set
     * @param emDataSet
     * @param set of names
     * @return
     */
    List<EMBody> getBodiesWithNameForDataSet(EMDataSet emDataSet, Set<String> selectedNames, long offset, int length);

    /**
     * Stream all of the bodies associated with a given data set.
     * @param emDataSet
     * @return
     */
    Stream<EMBody> streamBodiesForDataSet(EMDataSet emDataSet);
}
