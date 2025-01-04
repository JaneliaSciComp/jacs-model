package org.janelia.model.access.domain.dao.searchables;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.LSMImageDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.sample.LSMImage;

/**
 * {@link LSMImage} DAO.
 */
@AsyncIndex
@Dependent
public class LSMImageSearchableDao extends AbstractImageSearchableDao<LSMImage> implements LSMImageDao {

    @Inject
    LSMImageSearchableDao(LSMImageDao lsmImageDao,
                          @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(lsmImageDao, objectIndexer);
    }

}
