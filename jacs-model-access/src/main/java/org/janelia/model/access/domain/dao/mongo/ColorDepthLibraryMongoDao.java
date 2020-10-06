package org.janelia.model.access.domain.dao.mongo;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ColorDepthLibraryDao;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibrary;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibraryUtils;

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

}
