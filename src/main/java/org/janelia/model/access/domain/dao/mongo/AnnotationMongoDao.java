package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.cdi.DaoObjectMapper;
import org.janelia.model.domain.ontology.Annotation;

import javax.inject.Inject;

/**
 * {@link Annotation} Mongo DAO.
 */
public class AnnotationMongoDao<T extends Annotation> extends AbstractDomainObjectMongoDao<T> implements AnnotationDao<T> {
    @Inject
    AnnotationMongoDao(MongoDatabase mongoDatabase,
                     DomainPermissionsMongoHelper permissionsHelper,
                     DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }
}
