package org.janelia.model.access.domain.dao.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.janelia.model.access.domain.dao.ColorDepthImageQuery;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.RemoveFromSetFieldValueHandler;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;
import org.janelia.model.util.SortCriteria;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;

/**
 * {@link ColorDepthImage} Mongo DAO.
 */
public class ColorDepthImageMongoDao extends AbstractDomainObjectMongoDao<ColorDepthImage> implements ColorDepthImageDao {
    @Inject
    ColorDepthImageMongoDao(MongoDatabase mongoDatabase,
                            TimebasedIdentifierGenerator idGenerator,
                            DomainPermissionsMongoHelper permissionsHelper,
                            DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public long countColorDepthMIPs(ColorDepthImageQuery cdmQuery) {
        return mongoCollection.countDocuments(createColorDepthMIPsFilter(cdmQuery));
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

    private Bson createColorDepthMIPsFilter(ColorDepthImageQuery cdmQuery) {
        ImmutableList.Builder<Bson> cdmFiltersBuilder = ImmutableList.builder();
        if (StringUtils.isNotBlank(cdmQuery.getOwner())) {
            cdmFiltersBuilder.add(Filters.eq("ownerKey", cdmQuery.getOwner()));
        }
        if (StringUtils.isNotBlank(cdmQuery.getAlignmentSpace())) {
            cdmFiltersBuilder.add(Filters.eq("alignmentSpace", cdmQuery.getAlignmentSpace()));
        }
        if (CollectionUtils.isNotEmpty(cdmQuery.getLibraryIdentifiers())) {
            cdmFiltersBuilder.add(Filters.in("libraries", cdmQuery.getLibraryIdentifiers()));
        }
        if (CollectionUtils.isNotEmpty(cdmQuery.getExactNames())) {
            cdmFiltersBuilder.add(Filters.in("name", cdmQuery.getExactNames()));
        }
        if (CollectionUtils.isNotEmpty(cdmQuery.getFuzzyNames())) {
            List<Bson> fuzzyNamesFilters = cdmQuery.getFuzzyNames().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(fuzzyName -> Filters.regex("name", fuzzyName))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(fuzzyNamesFilters)) {
                cdmFiltersBuilder.add(Filters.or(fuzzyNamesFilters));
            }
        }
        if (CollectionUtils.isNotEmpty(cdmQuery.getExactFilepaths())) {
            cdmFiltersBuilder.add(Filters.in("filepath", cdmQuery.getExactFilepaths()));
        }
        if (CollectionUtils.isNotEmpty(cdmQuery.getFuzzyFilepaths())) {
            List<Bson> fuzzyPathsFilters = cdmQuery.getFuzzyFilepaths().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(fuzzyPath -> Filters.regex("filepath", fuzzyPath))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(fuzzyPathsFilters)) {
                cdmFiltersBuilder.add(Filters.or(fuzzyPathsFilters));
            }
        }
        if (CollectionUtils.isNotEmpty(cdmQuery.getSampleRefs())) {
            cdmFiltersBuilder.add(Filters.in("sampleRef", cdmQuery.getSampleRefs()));
        }
        return MongoDaoHelper.createFilterCriteria(cdmFiltersBuilder.build());
    }

    @Override
    public Optional<ColorDepthImage> findColorDepthImageByPath(String imagePath) {
        return MongoDaoHelper.find(
                createColorDepthMIPsFilter(new ColorDepthImageQuery().withExactFilepaths(Collections.singleton(imagePath))),
                null,
                0,
                -1,
                mongoCollection,
                ColorDepthImage.class)
                .stream()
                .findFirst();
    }

    @Override
    public Stream<ColorDepthImage> streamColorDepthMIPs(ColorDepthImageQuery cdmQuery) {
        Spliterator<ColorDepthImage> iterableCursor = MongoDaoHelper.rawFind(
                createColorDepthMIPsFilter(cdmQuery),
                MongoDaoHelper.createBsonSortCriteria(new SortCriteria("filepath")),
                cdmQuery.getOffset(),
                cdmQuery.getLength(),
                mongoCollection,
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
    public long addLibraryBySampleRefs(String libraryIdentifier, String objective, Collection<Reference> sampleRefs, boolean includeDerivedImages) {
        if (CollectionUtils.isNotEmpty(sampleRefs) && StringUtils.isNotBlank(libraryIdentifier)) {
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(false);
            // create filter for update
            ImmutableList.Builder<Bson> filterBuilder = ImmutableList.<Bson>builder()
                    .add(Filters.in("sampleRef", sampleRefs));
            if (StringUtils.isNotBlank(objective)) {
                filterBuilder.add(Filters.eq("objective", objective));
            }
            if (!includeDerivedImages) {
                filterBuilder.add(Filters.or(
                        Filters.exists("sourceImageRef", false),
                        Filters.eq("sourceImageRef", null)
                ));
            }
            DaoUpdateResult result = MongoDaoHelper.updateMany(
                    mongoCollection,
                    MongoDaoHelper.createFilterCriteria(filterBuilder.build()),
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

    @Override
    public long removeAllMipsFromLibrary(String libraryIdentifier) {
        if (StringUtils.isNotBlank(libraryIdentifier)) {
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(false);
            DaoUpdateResult result = MongoDaoHelper.updateMany(
                    mongoCollection,
                    Filters.eq("libraries", libraryIdentifier),
                    ImmutableMap.of(
                            "libraries", new RemoveFromSetFieldValueHandler<>(libraryIdentifier)
                    ),
                    updateOptions
            );
            return result.getEntitiesAffected();
        } else {
            return 0L;
        }
    }
}
