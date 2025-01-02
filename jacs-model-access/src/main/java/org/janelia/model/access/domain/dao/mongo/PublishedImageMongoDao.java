package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.PublishedImageDao;
import org.janelia.model.domain.sample.PublishedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PublishedImageMongoDao extends AbstractDomainObjectMongoDao<PublishedImage> implements PublishedImageDao {

    private static final List<String> gal4LexAReleases = ImmutableList.of("Gen1 GAL4", "Gen1 LexA");

    @Inject
    PublishedImageMongoDao(MongoDatabase mongoDatabase,
                           TimebasedIdentifierGenerator idGenerator,
                           DomainPermissionsMongoHelper permissionsHelper,
                           DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<PublishedImage> getImages(String alignmentSpace, Collection<String> slideCodes, String objective) {
        if (CollectionUtils.isEmpty(slideCodes)) {
            return Collections.emptyList();
        } else {
            return MongoDaoHelper.find(
                    MongoDaoHelper.createFilterCriteria(
                            Filters.eq("alignmentSpace", alignmentSpace),
                            Filters.in("slideCode", slideCodes),
                            Filters.eq("objective", objective)
                    ),
                    null,
                    0,
                    -1,
                    mongoCollection,
                    PublishedImage.class
            );
        }
    }

    @Override
    public List<PublishedImage> getGen1Gal4LexAImages(String anatomicalArea, Collection<String> lines) {
        if (CollectionUtils.isEmpty(lines)) {
            return Collections.emptyList();
        } else {
            return MongoDaoHelper.find(
                    // these images must come from one of two releases, but we don't know
                    //  which a priori
                    MongoDaoHelper.createFilterCriteria(
                            Filters.eq("area", anatomicalArea),
                            Filters.in("originalLine", lines),
                            Filters.in("releaseName", gal4LexAReleases)
                    ),
                    null,
                    0,
                    -1,
                    mongoCollection,
                    PublishedImage.class
            );
        }
    }
}
