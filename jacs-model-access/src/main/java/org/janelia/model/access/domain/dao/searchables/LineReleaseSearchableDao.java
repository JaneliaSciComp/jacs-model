package org.janelia.model.access.domain.dao.searchables;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.LineReleaseDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.sample.LineRelease;

/**
 * {@link LineRelease} DAO.
 */
@AsyncIndex
public class LineReleaseSearchableDao extends AbstractDomainSearchablDao<LineRelease> implements LineReleaseDao {

    private final LineReleaseDao lineReleaseDao;

    @Inject
    LineReleaseSearchableDao(LineReleaseDao lineReleaseDao,
                             @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(lineReleaseDao, objectIndexer);
        this.lineReleaseDao = lineReleaseDao;
    }
}
