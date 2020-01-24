package org.janelia.model.access.domain.dao.mongo;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.ColorDepthImageDao;
import org.janelia.model.access.domain.dao.EntityUtils;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;
import org.janelia.model.domain.sample.Sample;

/**
 * {@link ColorDepthImage} Mongo DAO.
 */
public class ColorDepthImageMongoDao extends AbstractDomainObjectMongoDao<ColorDepthImage> implements ColorDepthImageDao {
    @Inject
    ColorDepthImageMongoDao(MongoDatabase mongoDatabase,
                            DomainPermissionsMongoHelper permissionsHelper,
                            DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }

    @Override
    public long countColorDepthMIPs(String ownerKey, String alignmentSpace, List<String> libraryNames, List<String> matchingNames, List<String> matchingFilepaths) {
        return mongoCollection.countDocuments(createColorDepthMIPsFilter(ownerKey, alignmentSpace, libraryNames, matchingNames, matchingFilepaths));
    }

    private Bson createColorDepthMIPsFilter(String ownerKey, String alignmentSpace, List<String> libraryNames, List<String> matchingNames, List<String> matchingFilepaths) {
        ImmutableList.Builder<Bson> cdmFiltersBuilder = ImmutableList.builder();
        if (StringUtils.isNotBlank(ownerKey)) {
            cdmFiltersBuilder.add(Filters.eq("ownerKey", ownerKey));
        }
        if (StringUtils.isNotBlank(alignmentSpace)) {
            cdmFiltersBuilder.add(Filters.eq("alignmentSpace", alignmentSpace));
        }
        if (CollectionUtils.isNotEmpty(libraryNames)) {
            cdmFiltersBuilder.add(Filters.in("libraries", libraryNames));
        }
        if (CollectionUtils.isNotEmpty(matchingNames)) {
            cdmFiltersBuilder.add(Filters.in("name", matchingNames));
        }
        if (CollectionUtils.isNotEmpty(matchingFilepaths)) {
            cdmFiltersBuilder.add(Filters.in("filepath", matchingFilepaths));
        }
        return MongoDaoHelper.createFilterCriteria(cdmFiltersBuilder.build());
    }

    @Override
    public Stream<ColorDepthImage> streamColorDepthMIPs(String ownerKey, String alignmentSpace, List<String> libraryNames, List<String> matchingNames, List<String> matchingFilepaths,
                                                        int offset, int length) {
        Spliterator<ColorDepthImage> iterableCursor = MongoDaoHelper.rawFind(
                createColorDepthMIPsFilter(ownerKey, alignmentSpace, libraryNames, matchingNames, matchingFilepaths),
                offset,
                length,
                this.mongoCollection,
                ColorDepthImage.class)
                .noCursorTimeout(true)
                .spliterator();
        return StreamSupport.stream(iterableCursor, false);
    }

    @Override
    public void updatePublicUrls(ColorDepthImage cdmi) {
        MongoDaoHelper.update(mongoCollection, cdmi.getId(),
                ImmutableMap.of(
                        "publicImageUrl", new SetFieldValueHandler<>(cdmi.getPublicImageUrl()),
                        "publicThumbnailUrl", new SetFieldValueHandler<>(cdmi.getPublicThumbnailUrl())
                ));
    }
}
