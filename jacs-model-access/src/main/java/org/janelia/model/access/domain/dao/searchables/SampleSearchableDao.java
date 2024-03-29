package org.janelia.model.access.domain.dao.searchables;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
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
    public List<Sample> findMatchingSample(Collection<Long> ids,
                                           Collection<String> dataSetIds,
                                           Collection<String> sampleNames,
                                           Collection<String> slideCodes,
                                           long offset,
                                           int length) {
        return sampleDao.findMatchingSample(ids, dataSetIds, sampleNames, slideCodes, offset, length);
    }
}
