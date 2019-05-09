package org.janelia.model.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.domain.ontology.Annotation;

import javax.inject.Inject;

/**
 * {@link Annotation} Mongo DAO.
 */
public class AnnotationMongoDao extends AbstractDomainObjectMongoDao<Annotation> implements AnnotationDao {
    @Inject
    AnnotationMongoDao(MongoDatabase mongoDatabase,
                       DomainPermissionsMongoHelper permissionsHelper,
                       DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }
}
