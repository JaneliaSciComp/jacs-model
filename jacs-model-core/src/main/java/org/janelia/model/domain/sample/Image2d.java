package org.janelia.model.domain.sample;

import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchType;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="image",label="2D Image")
@SearchType(key="image",label="2D Image")
public class Image2d extends Image {



}
