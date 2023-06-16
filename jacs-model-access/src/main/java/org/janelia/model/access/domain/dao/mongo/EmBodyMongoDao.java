package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.EmBodyDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.flyem.EMBody;
import org.janelia.model.domain.flyem.EMDataSet;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class EmBodyMongoDao extends AbstractDomainObjectMongoDao<EMBody> implements EmBodyDao {

    @Inject
    EmBodyMongoDao(MongoDatabase mongoDatabase,
                   TimebasedIdentifierGenerator idGenerator,
                   DomainPermissionsMongoHelper permissionsHelper,
                   DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<EMBody> getBodiesForDataSet(EMDataSet emDataSet, long offset, int length) {
        return MongoDaoHelper.find(
                Filters.eq("dataSetRef", Reference.createFor(emDataSet)),
                null,
                offset,
                length,
                mongoCollection,
                EMBody.class
        );
    }

    @Override
    public List<EMBody> getBodiesWithNameForDataSet(EMDataSet emDataSet, Set<String> selectedNames,
                                                    long offset, int length) {
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("dataSetRef", Reference.createFor(emDataSet)),
                        CollectionUtils.isEmpty(selectedNames) ? null : Filters.in("name", selectedNames)
                ),
                null,
                offset,
                length,
                mongoCollection,
                EMBody.class
        );
    }

    @Override
    public Stream<EMBody> streamBodiesForDataSet(EMDataSet emDataSet) {
        Spliterator<EMBody> iterableCursor = MongoDaoHelper.rawFind(
                Filters.eq("dataSetRef", Reference.createFor(emDataSet)),
                null,
                0,
                -1,
                mongoCollection,
                EMBody.class)
                .noCursorTimeout(true)
                .spliterator();
        return StreamSupport.stream(iterableCursor, false);
    }

}
