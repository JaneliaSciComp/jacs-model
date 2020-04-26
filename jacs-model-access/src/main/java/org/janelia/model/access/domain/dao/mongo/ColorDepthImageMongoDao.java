package org.janelia.model.access.domain.dao.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.AddToSetFieldValueHandler;
import org.janelia.model.access.domain.dao.ColorDepthImageDao;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;
import org.janelia.model.util.SortCriteria;

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
    public long countColorDepthMIPs(String ownerKey, String alignmentSpace,
                                    Collection<String> libraryIdentifiers,
                                    Collection<String> matchingNames,
                                    Collection<String> matchingFilepaths,
                                    Collection<String> matchingSampleRefs) {
        return mongoCollection.countDocuments(createColorDepthMIPsFilter(ownerKey, alignmentSpace, libraryIdentifiers, matchingNames, matchingFilepaths, matchingSampleRefs));
    }

    @Override
    public Map<String, Integer> countColorDepthMIPsByAlignmentSpaceForLibrary(String library) {
        List<Document> countsByAligmentSpace = new ArrayList<>();
        mongoCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("libraries", library)),
                        Aggregates.group("$alignmentSpace", Accumulators.sum("count", 1))
                ),
                Document.class
        ).into(countsByAligmentSpace);
        return countsByAligmentSpace.stream().collect(Collectors.toMap(d -> d.getString("_id"), d -> d.getInteger("count")));
    }

    private Bson createColorDepthMIPsFilter(String ownerKey, String alignmentSpace,
                                            Collection<String> libraryIdentifiers,
                                            Collection<String> matchingNames,
                                            Collection<String> matchingFilepaths,
                                            Collection<String> matchingSampleRefs) {
        ImmutableList.Builder<Bson> cdmFiltersBuilder = ImmutableList.builder();
        if (StringUtils.isNotBlank(ownerKey)) {
            cdmFiltersBuilder.add(Filters.eq("ownerKey", ownerKey));
        }
        if (StringUtils.isNotBlank(alignmentSpace)) {
            cdmFiltersBuilder.add(Filters.eq("alignmentSpace", alignmentSpace));
        }
        if (CollectionUtils.isNotEmpty(libraryIdentifiers)) {
            cdmFiltersBuilder.add(Filters.in("libraries", libraryIdentifiers));
        }
        if (CollectionUtils.isNotEmpty(matchingNames)) {
            cdmFiltersBuilder.add(Filters.in("name", matchingNames));
        }
        if (CollectionUtils.isNotEmpty(matchingFilepaths)) {
            cdmFiltersBuilder.add(Filters.in("filepath", matchingFilepaths));
        }
        if (CollectionUtils.isNotEmpty(matchingSampleRefs)) {
            cdmFiltersBuilder.add(Filters.in("sampleRef", matchingSampleRefs));
        }
        return MongoDaoHelper.createFilterCriteria(cdmFiltersBuilder.build());
    }

    @Override
    public Stream<ColorDepthImage> streamColorDepthMIPs(String ownerKey, String alignmentSpace,
                                                        Collection<String> libraryIdentifiers,
                                                        Collection<String> matchingNames,
                                                        Collection<String> matchingFilepaths,
                                                        Collection<String> matchingSampleRefs,
                                                        int offset, int length) {
        Spliterator<ColorDepthImage> iterableCursor = MongoDaoHelper.rawFind(
                createColorDepthMIPsFilter(ownerKey, alignmentSpace, libraryIdentifiers, matchingNames, matchingFilepaths, matchingSampleRefs),
                MongoDaoHelper.createBsonSortCriteria(new SortCriteria("filepath")),
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

    @Override
    public long addLibraryBySampleRefs(String libraryIdentifier, Collection<Reference> sampleRefs) {
        if (CollectionUtils.isNotEmpty(sampleRefs) && StringUtils.isNotBlank(libraryIdentifier)) {
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(false);
            DaoUpdateResult result = MongoDaoHelper.updateMany(
                    mongoCollection,
                    Filters.in("sampleRef", sampleRefs),
                    ImmutableMap.of(
                            "libraries", new AddToSetFieldValueHandler<>(libraryIdentifier)
                    ),
                    updateOptions
            );
            return result.getEntitiesAffected();
        } else {
            return 0L;
        }
    }
}
