package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.EmDataSetDao;
import org.janelia.model.domain.flyem.EMDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.List;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class EmDataSetMongoDao extends AbstractDomainObjectMongoDao<EMDataSet> implements EmDataSetDao {

    private static final Logger LOG = LoggerFactory.getLogger(EmDataSetMongoDao.class);

    @Inject
    EmDataSetMongoDao(MongoDatabase mongoDatabase,
                       TimebasedIdentifierGenerator idGenerator,
                       DomainPermissionsMongoHelper permissionsHelper,
                       DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<EMDataSet> getDataSetVersions(String name) {
        return MongoDaoHelper.find(
                Filters.eq("name", name),
                null,
                0,
                -1,
                mongoCollection,
                EMDataSet.class
        );
    }

    @Override
    public EMDataSet getDataSetByNameAndVersion(String name, String version) {
        List<EMDataSet> emDataSets = MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("name", name),
                        Filters.eq("version", version)
                ),
                null,
                0,
                -1,
                mongoCollection,
                EMDataSet.class
        );
        if (emDataSets.size()>1) {
            LOG.warn("More than one EMDataSet detected for name="+name+" and version="+version);
        }
        else if (emDataSets.size()==1) {
            return emDataSets.get(0);
        }
        return null;
    }
}
