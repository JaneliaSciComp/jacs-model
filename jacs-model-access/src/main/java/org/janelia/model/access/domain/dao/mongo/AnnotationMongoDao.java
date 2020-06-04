package org.janelia.model.access.domain.dao.mongo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;

import javax.inject.Inject;

/**
 * {@link Annotation} Mongo DAO.
 */
public class AnnotationMongoDao extends AbstractDomainObjectMongoDao<Annotation> implements AnnotationDao {
    @Inject
    AnnotationMongoDao(MongoDatabase mongoDatabase,
                       TimebasedIdentifierGenerator idGenerator,
                       DomainPermissionsMongoHelper permissionsHelper,
                       DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<Annotation> findAnnotationsByTargets(Collection<Reference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return Collections.emptyList();
        } else {
            return find(
                    MongoDaoHelper.createFilterCriteria(
                            Filters.in("target", references.stream().filter(r -> r != null).map(r -> r.toString()).collect(Collectors.toSet()))),
                    null,
                    0,
                    -1,
                    getEntityType());
        }
    }

    @Override
    public List<Annotation> findAnnotationsByTargetsAccessibleBySubjectKey(Collection<Reference> references, String subjectKey) {
        if (CollectionUtils.isEmpty(references)) {
            return Collections.emptyList();
        } else {
            return find(
                    MongoDaoHelper.createFilterCriteria(
                            Filters.in("target", references.stream().filter(r -> r != null).map(r -> r.toString()).collect(Collectors.toSet())),
                            permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                    null,
                    0,
                    -1,
                    getEntityType());
        }
    }
}
