package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ColorDepthLibraryDao;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibrary;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibraryUtils;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link ColorDepthLibrary} Mongo DAO.
 */
public class ColorDepthLibraryMongoDao extends AbstractDomainObjectMongoDao<ColorDepthLibrary> implements ColorDepthLibraryDao {
    @Inject
    ColorDepthLibraryMongoDao(MongoDatabase mongoDatabase,
                              TimebasedIdentifierGenerator idGenerator,
                              DomainPermissionsMongoHelper permissionsHelper,
                              DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<ColorDepthLibrary> getLibraryWithVariants(String libraryIdentifier) {
        if (StringUtils.isBlank(libraryIdentifier)) {
            return Collections.emptyList();
        }
        return ColorDepthLibraryUtils.collectLibrariesWithVariants(MongoDaoHelper.find(
                Filters.regex("identifier", libraryIdentifier),
                null,
                0,
                -1,
                mongoCollection,
                ColorDepthLibrary.class)
        );
    }

    @Override
    public List<ColorDepthLibrary> getLibrariesByLibraryIdentifiers(Collection<String> libraryIdentifiers) {
        if (CollectionUtils.isEmpty(libraryIdentifiers)) {
            return Collections.emptyList();
        }
        return MongoDaoHelper.find(
                Filters.in("identifier", libraryIdentifiers),
                null,
                0,
                -1,
                mongoCollection,
                ColorDepthLibrary.class
        );
    }

    @Override
    public void updateColorDepthCounts(List<ColorDepthLibrary> libraries) {
        libraries.stream()
                .filter(l -> l.getId() != null || StringUtils.isNotBlank(l.getIdentifier()))
                .forEach(l -> {
                    UpdateOptions updateOptions = new UpdateOptions();
                    Bson cond;
                    if (l.getId() != null) {
                        cond = MongoDaoHelper.createFilterById(l.getId());
                    } else {
                        cond = Filters.eq("identifier", l.getIdentifier());
                    }
                    MongoDaoHelper.updateMany(
                            mongoCollection,
                            cond,
                            ImmutableMap.of("colorDepthCounts", new SetFieldValueHandler<>(l.getColorDepthCounts())),
                            updateOptions
                    );
        });
    }
}
