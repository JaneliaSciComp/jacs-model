package org.janelia.model.access.domain.dao.searchables;

import java.util.List;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.SampleQuery;
import org.janelia.model.access.domain.dao.SampleDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.sample.Sample;

/**
 * {@link Sample} DAO.
 */
@AsyncIndex
public class SampleSearchableDao extends AbstractDomainSearchableDao<Sample> implements SampleDao {

    private final SampleDao sampleDao;

    @Inject
    SampleSearchableDao(SampleDao sampleDao,
                        @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(sampleDao, objectIndexer);
        this.sampleDao = sampleDao;
    }

    @Override
    public List<Sample> findMatchingSamples(SampleQuery sampleQuery) {
        return sampleDao.findMatchingSamples(sampleQuery);
    }
}
