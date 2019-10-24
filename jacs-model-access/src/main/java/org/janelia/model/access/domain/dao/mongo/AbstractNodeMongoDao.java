package org.janelia.model.access.domain.dao.mongo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

import org.janelia.model.access.domain.dao.NodeDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;

/**
 * {@link Node} Mongo DAO.
 */
public abstract class AbstractNodeMongoDao<T extends Node> extends AbstractDomainObjectMongoDao<T> implements NodeDao<T> {
    AbstractNodeMongoDao(MongoDatabase mongoDatabase,
                         DomainPermissionsMongoHelper permissionsHelper,
                         DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }

    @Override
    public List<T> getNodeDirectAncestors(Reference nodeReference) {
        return MongoDaoHelper.find(
                Filters.all("children", Arrays.asList(nodeReference)),
                null,
                0,
                -1,
                mongoCollection,
                getEntityType());
    }

    @Override
    public List<T> getNodesByParentNameAndOwnerKey(Long parentNodeId, String name, String ownerKey) {
        T parentNode = findById(parentNodeId);
        if (!parentNode.hasChildren()) {
            return Collections.emptyList();
        } else {
            return MongoDaoHelper.findPipeline(
                    ImmutableList.of(
                            Aggregates.match(
                                    Filters.and(
                                            Filters.eq("name", name),
                                            Filters.eq("ownerKey", ownerKey),
                                            Filters.in("_id", parentNode.getChildren().stream().map(r -> r.getTargetId()).collect(Collectors.toList()))
                                    )
                            )
                    ),
                    null,
                    0,
                    -1,
                    mongoCollection,
                    getEntityType()
            );
        }
    }

}
