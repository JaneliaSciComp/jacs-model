package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Mongo DAO helper.
 */
class MongoDaoHelper {

    static <I, T, R> R findById(I id, MongoCollection<T> mongoCollection, Class<R> documentType) {
        List<R> entityDocs = find(Filters.eq("_id", id), null, 0, 2, mongoCollection, documentType);
        return CollectionUtils.isNotEmpty(entityDocs) ? entityDocs.get(0) : null;
    }

    static <I> Bson createFilterById(I id) {
        return Filters.eq("_id", id);
    }

    static <I, T, R> List<R> findByIds(Collection<I> ids, MongoCollection<T> mongoCollection, Class<R> documentType) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return find(Filters.in("_id", ids), null, 0, 0, mongoCollection, documentType);
        } else {
            return Collections.emptyList();
        }
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

    static Bson createFilterCriteria(List<Bson> filters) {
        return Filters.and(filters);
    }

    static <T, I> long delete(MongoCollection<T> mongoCollection, I entityId) {
        DeleteResult result = mongoCollection.deleteOne(createFilterById(entityId));
        return result.getDeletedCount();
    }

    static <T> long deleteMatchingRecords(MongoCollection<T> mongoCollection, Bson matchingCriteria) {
        if (matchingCriteria == null) {
            throw new IllegalArgumentException("An empty matching criteria will delete all records");
        }
        DeleteResult result = mongoCollection.deleteMany(matchingCriteria);
        return result.getDeletedCount();
    }

}
