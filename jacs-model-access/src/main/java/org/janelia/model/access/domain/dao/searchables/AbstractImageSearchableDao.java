package org.janelia.model.access.domain.dao.searchables;

import jakarta.inject.Inject;

import org.janelia.model.access.domain.dao.ImageDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.sample.Image;

/**
 * {@link Image} DAO.
 */
public class AbstractImageSearchableDao<T extends Image> extends AbstractDomainSearchableDao<T> implements ImageDao<T> {

    @Inject
    AbstractImageSearchableDao(ImageDao<T> imageDao,
                               DomainObjectIndexer objectIndexer) {
        super(imageDao, objectIndexer);
    }

}
