package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.NodeDao;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;
import org.janelia.model.util.SortCriteria;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Projections.include;

/**
 * {@link Node} Mongo DAO.
 */
public abstract class AbstractNodeMongoDao<T extends Node> extends AbstractDomainObjectMongoDao<T> implements NodeDao<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeMongoDao.class);

    AbstractNodeMongoDao(MongoDatabase mongoDatabase,
                         TimebasedIdentifierGenerator idGenerator,
                         DomainPermissionsMongoHelper permissionsHelper,
                         DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<T> getNodeDirectAncestors(Reference nodeReference) {
        return MongoDaoHelper.find(
                Filters.all("children", Collections.singletonList(nodeReference)),
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
                                            Filters.in("_id", parentNode.getChildren()
                                                    .stream().map(Reference::getTargetId).collect(Collectors.toList()))
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

    // TODO: move this to DomainUtils in the model
    private Multimap<String, Long> getIdsGroupedByClassname(Collection<Reference> references) {
        Multimap<String, Long> referenceMap = ArrayListMultimap.create();
        for (Reference reference : references) {
            if (reference == null) {
                continue;
            }
            referenceMap.put(reference.getTargetClassName(), reference.getTargetId());
        }
        return referenceMap;
    }

    /**
     * Paginated query for node children. This method is highly complicated by the fact that children of a single node
     * can come from different collections, so it needs to do N queries and merge the results.
     * @param subjectKey subject running the query
     * @param node the node whose children are being retrieved
     * @param sortCriteriaStr sort criteria for the children: field name prefixed with plus or minus
     * @param page index of the page to return
     * @param pageSize size of the pages
     * @return domain objects on the given page
     */
    @Override
    public List<DomainObject> getChildren(String subjectKey, Node node, String sortCriteriaStr,
                                                 long page, int pageSize) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        SortCriteria sortCriteria = new SortCriteria(sortCriteriaStr);
        String sortField = sortCriteria.getField();
        String sortFieldMongo = "id".equals(sortField) ?"_id":sortField;
        boolean asc = sortCriteria.getDirection()==SortCriteria.SortDirection.ASC;
        Bson permissionFilter = permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey);

        Multimap<String, Long> referenceMap = getIdsGroupedByClassname(node.getChildren());

        if (node.getChildren().size() < pageSize && page==0) {
            // Everything fits into one page, so just fetch it.

            // In parallel, launch N queries for the N domain object types on the page
            List<DomainObject> children = referenceMap.keySet().parallelStream().flatMap(className -> {
                Collection<Long> ids = referenceMap.get(className);
                Class<? extends DomainObject> clazz = DomainUtils.getObjectClassByName(className);
                String collectionName = DomainUtils.getCollectionName(className);
                MongoCollection<? extends DomainObject> collection = mongoDatabase.getCollection(collectionName, clazz);
                Spliterator<? extends DomainObject> spliterator = collection
                        .find(clazz)
                        .filter(
                                MongoDaoHelper.createFilterCriteria(
                                        MongoDaoHelper.createFilterByIds(ids),
                                        MongoDaoHelper.createFilterByClass(clazz),
                                        permissionFilter
                                )
                        )
                        .spliterator();
                return StreamSupport.stream(spliterator, false);
            }).collect(Collectors.toList());
            DomainUtils.sortDomainObjects(children, sortCriteriaStr);
            LOG.trace("getChildren (one page version) found {} objects and took {} ms", children.size(), stopWatch.getTime());
            return children;
        }

        // In parallel, launch N queries for the N domain object types in the folder, fetching the
        // values of their sortCriteria attribute, then resort all of the results according to the
        // sortCriteria.
        List<Document> sortedDocs = referenceMap.keySet().parallelStream().flatMap(className -> {
            Collection<Long> ids = referenceMap.get(className);
            String collectionName = DomainUtils.getCollectionName(className);
            Class<? extends DomainObject> clazz = DomainUtils.getObjectClassByName(className);
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
            Spliterator<Document> spliterator = collection
                    .find()
                    .filter(
                        MongoDaoHelper.createFilterCriteria(
                                MongoDaoHelper.createFilterByIds(ids),
                                MongoDaoHelper.createFilterByClass(clazz),
                                permissionFilter
                        )
                    )
                    // We just want the id/class and sortField
                    .projection(Projections.fields(include(sortFieldMongo, "class")))
                    .spliterator();
            return StreamSupport.stream(spliterator, false);
        }).sorted((o1, o2) -> {
            Comparable<?> v1 = (Comparable<?>)o1.get(sortFieldMongo);
            Comparable<?> v2 = (Comparable<?>)o2.get(sortFieldMongo);
            if (v1==null) {
                LOG.trace("Null {} for {}#{}", sortFieldMongo, o1.get("class"), o1.get("_id"));
            }
            if (v2==null) {
                LOG.trace("Null {} for {}#{}", sortFieldMongo, o2.get("class"), o2.get("_id"));
            }
            return asc ?
                    ComparisonChain.start().compare(v1, v2, Ordering.natural().nullsLast()).result()
                    : ComparisonChain.start().compare(v2, v1, Ordering.natural().nullsLast()).result();
        }).collect(Collectors.toList());

        // Select only the references on the requested page
        int fromIndex = (int)(page * pageSize);
        int toIndex = fromIndex + pageSize;
        if (toIndex>sortedDocs.size()) {
            toIndex = sortedDocs.size();
        }

        List<Reference> pageReferences = sortedDocs.subList(fromIndex, toIndex)
                .stream()
                .map(d -> Reference.createFor(d.getString("class"),d.getLong("_id")))
                .collect(Collectors.toList());

        // In parallel, launch N queries for the N domain object types on the page
        Multimap<String, Long> pageReferenceMap = getIdsGroupedByClassname(pageReferences);
        Map<Long,DomainObject> domainObjectMap = pageReferenceMap.keySet().parallelStream().flatMap(className -> {
            Collection<Long> pageIds = pageReferenceMap.get(className);
            Class<? extends DomainObject> clazz = DomainUtils.getObjectClassByName(className);
            String collectionName = DomainUtils.getCollectionName(className);
            MongoCollection<? extends DomainObject> collection = mongoDatabase.getCollection(collectionName, clazz);
            Spliterator<? extends DomainObject> spliterator = collection
                    .find(clazz)
                    .filter(
                        MongoDaoHelper.createFilterCriteria(
                                MongoDaoHelper.createFilterByIds(pageIds),
                                MongoDaoHelper.createFilterByClass(clazz),
                                permissionFilter
                        )
                    )
                    .spliterator();
            return StreamSupport.stream(spliterator, false);
        }).collect(Collectors.toMap(DomainObject::getId, Function.identity()));

        // Order the objects according to the predetermined sort order
        List<DomainObject> children = pageReferences
                .stream()
                .map(r -> domainObjectMap.get(r.getTargetId()))
                .collect(Collectors.toList());

        stopWatch.stop();

        LOG.trace("getChildren found {} objects and took {} ms", children.size(), stopWatch.getTime());
        return children;
    }
}
