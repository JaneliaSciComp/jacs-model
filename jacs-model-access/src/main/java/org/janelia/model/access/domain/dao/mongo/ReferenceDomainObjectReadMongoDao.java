package org.janelia.model.access.domain.dao.mongo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.ReferenceDomainObjectReadDao;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;

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
    public <T extends DomainObject> T findByReference(Reference entityReference) {
        return findByReferenceAndSubjectCriteria(entityReference, null);
    }

    @Override
    public <T extends DomainObject> T findByReferenceAndSubjectKey(Reference entityReference, String subjectKey) {
        return findByReferenceAndSubjectCriteria(entityReference, permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey));
    }

    @SuppressWarnings("unchecked")
    private <T extends DomainObject> T findByReferenceAndSubjectCriteria(Reference entityReference, Bson subjectFilter) {
        Class<T> clazz = (Class<T>) DomainUtils.getObjectClassByName(entityReference.getTargetClassName());

        List<T> results = MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(entityReference.getTargetId()),
                        subjectFilter),
                null,
                0,
                -1,
                getEntityCollection(clazz),
                clazz);

        return results.stream().findFirst().orElse(null);
    }

    @Override
    public List<? extends DomainObject> findByReferences(List<Reference> entityReferences) {
        return findByReferencesAndSubjectCriteria(entityReferences, null);
    }

    @Override
    public List<? extends DomainObject> findByReferencesAndSubjectKey(List<Reference> entityReferences, String subjectKey) {
        return findByReferencesAndSubjectCriteria(entityReferences, permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey));
    }

    private List<? extends DomainObject> findByReferencesAndSubjectCriteria(List<Reference> entityReferences, Bson subjectFilter) {
        Map<Class<? extends DomainObject>, Set<Reference>> entityCollectionMapping = entityReferences.stream()
                .collect(Collectors.groupingBy(
                        ref -> DomainUtils.getObjectClassByName(ref.getTargetClassName()),
                        Collectors.toSet()));

        return entityCollectionMapping.entrySet().stream()
                .flatMap(collectionRefEntry -> MongoDaoHelper.find(
                        MongoDaoHelper.createFilterCriteria(
                                MongoDaoHelper.createFilterByIds(collectionRefEntry.getValue().stream().map(ref -> ref.getTargetId()).collect(Collectors.toSet())),
                                subjectFilter),
                        null,
                        0,
                        -1,
                        getEntityCollection(collectionRefEntry.getKey()),
                        collectionRefEntry.getKey()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends DomainObject> findByReverseReference(ReverseReference reverseEntityReference) {
        return findByReverseReferenceAndSubjectCriteria(reverseEntityReference, null);
    }

    @Override
    public List<? extends DomainObject> findByReverseReferenceAndSubjectKey(ReverseReference reverseEntityReference, String subjectKey) {
        return findByReverseReferenceAndSubjectCriteria(reverseEntityReference, permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey));
    }

    private List<? extends DomainObject> findByReverseReferenceAndSubjectCriteria(ReverseReference reverseEntityReference, Bson subjectFilter) {
        Class<? extends DomainObject> referringClass = DomainUtils.getObjectClassByName(reverseEntityReference.getReferringClassName());
        String collectionName = DomainUtils.getCollectionName(referringClass);
        Set<String> allPossibleReferringClasses = DomainUtils.getObjectClassNames(DomainUtils.getBaseClass(collectionName));

        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createAttributeFilter(reverseEntityReference.getReferenceAttr(), reverseEntityReference.getReferenceId()),
                        Filters.in("class", allPossibleReferringClasses),
                        subjectFilter),
                null,
                0,
                -1,
                getEntityCollection(referringClass),
                referringClass);
    }
}
