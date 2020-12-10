package org.janelia.model.access.domain.dao.searchables;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.LSMImageDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.sample.LSMImage;

/**
 * {@link LSMImage} DAO.
 */
@AsyncIndex
public class LSMImageSearchableDao extends ImageSearchableDao<LSMImage> implements LSMImageDao {

    @Inject
    LSMImageSearchableDao(LSMImageDao lsmImageDao,
                          @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(lsmImageDao, objectIndexer);
    }

}
