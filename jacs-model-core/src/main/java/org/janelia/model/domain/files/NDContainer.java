package org.janelia.model.domain.files;

import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchType;

/**
 * Represents a synchronized path to an N dimensional array container. N5 and Zarr are two standards for representing
 * these types of data.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="ndContainer",label="Synchronized Path")
@MongoMapped(collectionName="ndContainer",label="Synchronized Path")
public abstract class NDContainer extends SyncedPath {

}
