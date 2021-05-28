package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ColorDepthImageQuery;
import org.janelia.model.access.domain.dao.EmBodyDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.flyem.EMBody;
import org.janelia.model.domain.flyem.EMDataSet;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;
import org.janelia.model.util.SortCriteria;

import javax.inject.Inject;
import java.util.List;
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
    public List<EMBody> getBodiesForDataSet(EMDataSet emDataSet) {
        return MongoDaoHelper.find(
                Filters.eq("dataSetRef", Reference.createFor(emDataSet)),
                null,
                0,
                -1,
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
