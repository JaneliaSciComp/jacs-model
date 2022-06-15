package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.PublishedImageDao;
import org.janelia.model.domain.sample.PublishedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class PublishedImageMongoDao extends AbstractDomainObjectMongoDao<PublishedImage> implements PublishedImageDao {

    private static final Logger LOG = LoggerFactory.getLogger(PublishedImageMongoDao.class);

    private static final List<String> gal4LexAReleases = ImmutableList.of("Gen1 GAL4", "Gen1 LexA");

    @Inject
    PublishedImageMongoDao(MongoDatabase mongoDatabase,
                           TimebasedIdentifierGenerator idGenerator,
                           DomainPermissionsMongoHelper permissionsHelper,
                           DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public PublishedImage getImage(String alignmentSpace, String slideCode, String objective) {

        List<PublishedImage> publishedImages = MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("alignmentSpace", alignmentSpace),
                        Filters.eq("slideCode", slideCode),
                        Filters.eq("objective", objective)
                ),
                null,
                0,
                -1,
                mongoCollection,
                PublishedImage.class
                );
        if (publishedImages.size() > 1) {
            LOG.warn("More than one PublishedImage detected for slideCode=" + slideCode +
                    ", alignmentSpace=" + alignmentSpace + ", objective=" + objective);
        } else if (publishedImages.size() == 1) {
            return publishedImages.get(0);
        }
        return null;
    }

    @Override
    public PublishedImage getGen1Gal4LexAImage(String originalLine, String area) {
        List<PublishedImage> publishedImages = MongoDaoHelper.find(
                // these images must come from one of two releases, but we don't know
                //  which a priori
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("originalLine", originalLine),
                        Filters.eq("area", area),
                        // filter for "releaseName":{$in:["Gen1 GAL4","Gen1 LexA"]}
                        Filters.in("releaseName", gal4LexAReleases)
                ),
                null,
                0,
                -1,
                mongoCollection,
                PublishedImage.class
        );
        if (publishedImages.size() > 1) {
            LOG.warn("More than one PublishedImage detected for originalLine=" + originalLine +
                    ", area=" + area);
        } else if (publishedImages.size() == 1) {
            return publishedImages.get(0);
        }
        return null;
    }
}
