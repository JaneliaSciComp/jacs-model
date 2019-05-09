package org.janelia.model.mongo;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.AppendFieldValueHandler;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.RemoveFieldValueHandler;
import org.janelia.model.access.domain.dao.RemoveItemsFieldValueHandler;
import org.janelia.model.util.SortCriteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Mongo DAO helper.
 */
class MongoDaoHelper {

    static <I, T, R> R findById(I id, MongoCollection<T> mongoCollection, Class<R> documentType) {
        if (id == null) {
            return null;
        } else {
            List<R> entityDocs = find(
                    Filters.eq("_id", id),
                    null,
                    0,
                    2,
                    mongoCollection,
                    documentType
            );
            return CollectionUtils.isNotEmpty(entityDocs) ? entityDocs.get(0) : null;
        }
    }

    static <I> Bson createFilterById(I id) {
        return Filters.eq("_id", id);
    }

    static <I> Bson createFilterByIds(Collection<I> ids) {
        return Filters.in("_id", ids);
    }

    static Bson createFilterByClass(Class<?> clazz) {
        return Filters.eq("class", clazz.getName());
    }

    static <T, R> List<R> getDistinctValues(String fieldName, Bson filter, MongoCollection<T> mongoCollection, Class<R> documentType) {
        Iterable<R> results;
        if (filter == null) {
            results = mongoCollection.distinct(fieldName, documentType);
        } else {
            results = mongoCollection.distinct(fieldName, filter, documentType);
        }
        List<R> entityDocs = new ArrayList<>();
        results.forEach(entityDocs::add);
        return entityDocs;
    }

    static <I, T, R> List<R> findByIds(Collection<I> ids, MongoCollection<T> mongoCollection, Class<R> documentType) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return find(createFilterByIds(ids), null, 0, 0, mongoCollection, documentType);
        } else {
            return Collections.emptyList();
        }
    }

    static <T, R> R findFirst(Bson queryFilter, Bson sortCriteria, MongoCollection<T> mongoCollection, Class<R> resultType) {
        return find(queryFilter, sortCriteria, 0, 2, mongoCollection, resultType).stream().findFirst().orElse(null);
    }

    static <T, R> List<R> find(Bson queryFilter, Bson sortCriteria, long offset, int length, MongoCollection<T> mongoCollection, Class<R> resultType) {
        List<R> entityDocs = new ArrayList<>();
        FindIterable<R> results = mongoCollection.find(resultType);
        if (queryFilter != null) {
            results = results.filter(queryFilter);
        }
        if (offset > 0) {
            results = results.skip((int) offset);
        }
        if (length > 0) {
            results = results.limit(length);
        }
        return results
                .sort(sortCriteria)
                .into(entityDocs);
    }

    static <T, R> List<R> findPipeline(List<Bson> aggregationPipeline, Bson sortCriteria, long offset, int length, MongoCollection<T> mongoCollection, Class<R> resultType) {
        List<R> results = new ArrayList<>();
        aggregationPipelineIterator(aggregationPipeline, sortCriteria, offset, length, mongoCollection, resultType)
                .forEach(results::add);
        return results;
    }

    static <T, R> Iterable<R> aggregationPipelineIterator(List<Bson> aggregationPipeline, Bson sortCriteria, long offset, int length, MongoCollection<T> mongoCollection, Class<R> resultType) {
        ImmutableList.Builder<Bson> aggregatePipelineBuilder = ImmutableList.builder();
        if (CollectionUtils.isNotEmpty(aggregationPipeline)) {
            aggregatePipelineBuilder.addAll(aggregationPipeline);
        }
        if (sortCriteria != null) {
            aggregatePipelineBuilder.add(Aggregates.sort(sortCriteria));
        }
        if (offset > 0) {
            aggregatePipelineBuilder.add(Aggregates.skip((int) offset));
        }
        if (length > 0) {
            aggregatePipelineBuilder.add(Aggregates.limit(length));
        }
        return mongoCollection.aggregate(aggregatePipelineBuilder.build(), resultType);
    }

    static <T> Long count(Bson filter, MongoCollection<T> mongoCollection) {
        return mongoCollection.countDocuments(filter);
    }

    static Bson createFilterCriteria(Bson... filters) {
        return createFilterCriteria(Arrays.asList(filters));
    }

    static Bson createFilterCriteria(List<Bson> filters) {
        if (CollectionUtils.isNotEmpty(filters)) {
            return filters.stream()
                    .filter(f -> f != null)
                    .reduce((f1, f2) -> Filters.and(f1, f2))
                    .orElse(new Document());
        } else {
            return new Document();
        }
    }

    static Bson createBsonSortCriteria(SortCriteria... sortCriteria) {
        return createBsonSortCriteria(Arrays.asList(sortCriteria));
    }

    static Bson createBsonSortCriteria(List<SortCriteria> sortCriteria) {
        if (CollectionUtils.isNotEmpty(sortCriteria)) {
            Map<String, Object> sortCriteriaAsMap = sortCriteria.stream()
                    .filter(sc -> StringUtils.isNotBlank(sc.getField()))
                    .collect(Collectors.toMap(
                            SortCriteria::getField,
                            sc -> sc.getDirection() == SortCriteria.SortDirection.DESC ? -1 : 1,
                            (sc1, sc2) -> sc2,
                            LinkedHashMap::new));
            return new Document(sortCriteriaAsMap);
        } else {
            return null;
        }
    }

    static <T, I> long delete(MongoCollection<T> mongoCollection, I entityId) {
        if (entityId == null) {
            return 0;
        } else {
            DeleteResult result = mongoCollection.deleteOne(createFilterById(entityId));
            return result.getDeletedCount();
        }
    }

    static <T, I> long update(MongoCollection<T> mongoCollection, I entityId, Map<String, EntityFieldValueHandler<?>> fieldsToUpdate) {
        if (entityId == null) {
            return 0;
        } else {
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(false);
            DaoUpdateResult result = updateMany(mongoCollection, createFilterById(entityId), fieldsToUpdate, updateOptions);
            return result.getEntitiesAffected();
        }
    }

    static <T> long deleteMatchingRecords(MongoCollection<T> mongoCollection, Bson matchingCriteria) {
        if (matchingCriteria == null) {
            throw new IllegalArgumentException("An empty matching criteria will delete all records");
        }
        DeleteResult result = mongoCollection.deleteMany(matchingCriteria);
        return result.getDeletedCount();
    }

    static <T> DaoUpdateResult updateMany(MongoCollection<T> mongoCollection, Bson matchFilter, Map<String, EntityFieldValueHandler<?>> fieldsToUpdate, UpdateOptions updateOptions) {
        if (MapUtils.isEmpty(fieldsToUpdate)) {
            return new DaoUpdateResult(0, 0);
        } else {
            List<Bson> bsonUpdates = fieldsToUpdate.entrySet().stream()
                    .map(e -> getFieldUpdate(e.getKey(), e.getValue()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            UpdateResult result = mongoCollection.updateMany(matchFilter, Updates.combine(bsonUpdates), updateOptions);
            return new DaoUpdateResult(result.getMatchedCount(), result.getModifiedCount());
        }
    }

    @SuppressWarnings("unchecked")
    private static Bson getFieldUpdate(String fieldName, EntityFieldValueHandler<?> valueHandler) {
        if (valueHandler == null || valueHandler.getFieldValue() == null || valueHandler instanceof RemoveFieldValueHandler) {
            return Updates.unset(fieldName);
        } else if (valueHandler instanceof AppendFieldValueHandler) {
            Object value = valueHandler.getFieldValue();
            if (value instanceof Iterable) {
                if (Set.class.isAssignableFrom(value.getClass())) {
                    return Updates.addEachToSet(fieldName, ImmutableList.copyOf((Iterable) value));
                } else {
                    return Updates.pushEach(fieldName, ImmutableList.copyOf((Iterable) value));
                }
            } else {
                return Updates.push(fieldName, value);
            }
        } else if (valueHandler instanceof RemoveItemsFieldValueHandler) {
            Object value = valueHandler.getFieldValue();
            if (value instanceof Iterable) {
                return Updates.pullAll(fieldName, ImmutableList.copyOf((Iterable) value));
            } else {
                return Updates.pull(fieldName, value);
            }
        } else {
            return Updates.set(fieldName, valueHandler.getFieldValue());
        }
    }

}
