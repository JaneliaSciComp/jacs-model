package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.domain.workspace.TreeNode;

import javax.inject.Inject;

/**
 * {@link Annotation} Mongo DAO.
 */
public class AnnotationMongoDao<T extends Annotation> extends AbstractPermissionAwareDomainMongoDao<T> implements AnnotationDao<T> {
    @Inject
    AnnotationMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper) {
        super(mongoDatabase, objectMapper);
    }
}
