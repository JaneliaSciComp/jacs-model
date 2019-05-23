package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.access.domain.dao.ReferenceDomainObjectReadDao;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;

import javax.inject.Inject;
import java.util.List;

/**
 * DAO for reading entities by their references.
 */
public class ReferenceDomainObjectReadMongoDao extends AbstractMongoDao implements ReferenceDomainObjectReadDao {

    private final DomainPermissionsMongoHelper permissionsHelper;

    @Inject
    public ReferenceDomainObjectReadMongoDao(MongoDatabase mongoDatabase, DomainPermissionsMongoHelper permissionsHelper) {
        super(mongoDatabase);
        this.permissionsHelper = permissionsHelper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> T findByReferenceAndSubjectKey(Reference entityReference, String subjectKey) {
        Class<T> clazz = (Class<T>) DomainUtils.getObjectClassByName(entityReference.getTargetClassName());

        List<T> results = MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(entityReference.getTargetId()),
                        permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                null,
                0,
                -1,
                getEntityCollection(clazz),
                clazz);

        return results.stream().findFirst().orElse(null);
    }

    @Override
    public List<? extends DomainObject> findByReferencesAndSubjectKey(List<Reference> entityReferences, String subjectKey) {
        return null; // FIXME
    }

    @Override
    public List<? extends DomainObject> findByReverseReferenceAndSubjectKey(ReverseReference reverseEntityReference, String subjectKey) {
        return null; // FIXME
    }

}
