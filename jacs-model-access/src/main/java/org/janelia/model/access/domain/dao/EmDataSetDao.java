package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.flyem.EMDataSet;

import java.util.List;

/**
 * Interface for accessing EM information synchronized from FlyEM neuPrint.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface EmDataSetDao extends ReadDao<EMDataSet, Long>, WriteDao<EMDataSet, Long> {

    /**
     * Returns all versions of the data set with the given name.
     * @param name data set name
     * @return all data sets with the given name
     */
    List<EMDataSet> getDataSetVersions(String name);

    /**
     * Returns the specified version of a data set.
     * @param name data set name
     * @param version data set version
     * @return matching data set, or null if not found
     */
    EMDataSet getDataSetByNameAndVersion(String name, String version);

}
