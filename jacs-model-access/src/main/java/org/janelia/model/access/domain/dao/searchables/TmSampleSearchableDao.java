package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmSampleDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.TmSample;

import javax.inject.Inject;

/**
 * {@link TmSample} DAO.
 */
@AsyncIndex
public class TmSampleSearchableDao extends AbstractDomainSearchablDao<TmSample> implements TmSampleDao {

    private final TmSampleDao tmSampleDao;

    @Inject
    TmSampleSearchableDao(TmSampleDao tmSampleDao,
                          @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(tmSampleDao, objectIndexer);
        this.tmSampleDao = tmSampleDao;
    }

    @Override
    public TmSample createTmSample(String subjectKey, TmSample tmSample) {
        TmSample persistedTmSample = tmSampleDao.createTmSample(subjectKey, tmSample);
        domainObjectIndexer.indexDocument(persistedTmSample);
        return persistedTmSample;
    }

    @Override
    public TmSample updateTmSample(String subjectKey, TmSample tmSample) {
        TmSample updatedTmSample = tmSampleDao.updateTmSample(subjectKey, tmSample);
        domainObjectIndexer.indexDocument(updatedTmSample);
        return updatedTmSample;
    }

    @Override
    public boolean removeTmSample(String subjectKey, Long tmSampleId) {
        boolean removed = tmSampleDao.removeTmSample(subjectKey, tmSampleId);
        if (removed) {
            domainObjectIndexer.removeDocument(tmSampleId);
        }
        return removed;
    }
}
