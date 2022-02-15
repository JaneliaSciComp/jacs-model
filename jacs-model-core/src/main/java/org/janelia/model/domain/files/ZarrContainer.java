package org.janelia.model.domain.files;

import org.janelia.model.domain.support.SearchType;

/**
 * Represents a synchronized path to an n5 container.
 *
 * @see <a href="https://github.com/saalfeldlab/n5">n5 API</a>
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="zarrContainer",label="Zarr Container")
public class ZarrContainer extends NDContainer {

}
