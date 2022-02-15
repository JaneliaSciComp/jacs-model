package org.janelia.model.domain.files;

import org.janelia.model.domain.support.SearchType;

/**
 * Represents a synchronized path to an n5 container.
 *
 * @see <a href="https://github.com/saalfeldlab/n5">n5 API</a>
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="n5Container",label="N5 Container")
public class N5Container extends SyncedPath {

}
