package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.TmSampleDao;
import org.janelia.model.domain.DomainConstants;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.TmSample;
import org.janelia.model.domain.workspace.TreeNode;

import javax.inject.Inject;
import java.util.Collections;

/**
 * {@link TmSample} Mongo DAO.
 */
public class TmSampleMongoDao extends AbstractDomainObjectMongoDao<TmSample> implements TmSampleDao {

    private final DomainDAO domainDao;

    @Inject
    TmSampleMongoDao(MongoDatabase mongoDatabase,
                     TimebasedIdentifierGenerator idGenerator,
                     DomainPermissionsMongoHelper permissionsHelper,
                     DomainUpdateMongoHelper updateHelper,
                     DomainDAO domainDao) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
        this.domainDao = domainDao;
    }

    @Override
    public TmSample createTmSample(String subjectKey, TmSample tmSample) {
        try {
            TmSample sample = domainDao.save(subjectKey, tmSample);
            TreeNode folder = domainDao.getOrCreateDefaultTreeNodeFolder(subjectKey, DomainConstants.NAME_TM_SAMPLE_FOLDER);
            domainDao.addChildren(subjectKey, folder, Collections.singletonList(Reference.createFor(sample)));
            return sample;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmSample updateTmSample(String subjectKey, TmSample tmSample) {
        try {
            return domainDao.save(subjectKey, tmSample);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean removeTmSample(String subjectKey, Long tmSampleId) {
        Reference ref = Reference.createFor(TmSample.class, tmSampleId);
        DomainObject domainObj = domainDao.getDomainObject(subjectKey, ref);
        if (domainObj != null) {
            try {
                return domainDao.remove(subjectKey, domainObj);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            return false;
        }
    }

}
