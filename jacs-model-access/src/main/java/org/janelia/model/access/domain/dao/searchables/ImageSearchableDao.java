package org.janelia.model.access.domain.dao.searchables;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.ImageDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.sample.Image;

/**
 * {@link Image} DAO.
 */
@AsyncIndex
public class ImageSearchableDao<T extends Image> extends AbstractDomainSearchableDao<T> implements ImageDao<T> {

    @Inject
    ImageSearchableDao(ImageDao<T> imageDao,
                       @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(imageDao, objectIndexer);
    }

}
