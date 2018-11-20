package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.OntologyDao;
import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.util.SortCriteria;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * {@link Ontology} Mongo DAO.
 */
public class OntologyMongoDao extends AbstractDomainObjectMongoDao<Ontology> implements OntologyDao {

    @Inject
    OntologyMongoDao(MongoDatabase mongoDatabase,
                     DomainPermissionsMongoHelper permissionsHelper,
                     DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }

    @Override
    public List<Ontology> getAllOntologiesByAccessibleBySubjectKey(String subjectKey, long offset, int length) {
        if (StringUtils.isBlank(subjectKey))
            return Collections.emptyList();
        return MongoDaoHelper.find(
                permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey),
                MongoDaoHelper.createBsonSortCriteria(new SortCriteria("_id")),
                offset,
                length,
                mongoCollection,
                Ontology.class);
    }

}
