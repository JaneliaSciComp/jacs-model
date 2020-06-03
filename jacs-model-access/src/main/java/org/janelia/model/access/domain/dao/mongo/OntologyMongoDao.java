package org.janelia.model.access.domain.dao.mongo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.OntologyDao;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.domain.ontology.OntologyTerm;
import org.janelia.model.util.SortCriteria;
import org.janelia.model.util.TimebasedIdentifierGenerator;

/**
 * {@link Ontology} Mongo DAO.
 */
public class OntologyMongoDao extends AbstractDomainObjectMongoDao<Ontology> implements OntologyDao {

    @Inject
    OntologyMongoDao(MongoDatabase mongoDatabase,
                     TimebasedIdentifierGenerator idGenerator,
                     DomainPermissionsMongoHelper permissionsHelper,
                     DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<Ontology> getOntologiesAccessibleBySubjectGroups(String subjectKey, long offset, int length) {
        if (StringUtils.isBlank(subjectKey))
            return Collections.emptyList();
        return MongoDaoHelper.find(
                permissionsHelper.createSameGroupReadPermissionFilterForSubjectKey(subjectKey),
                MongoDaoHelper.createBsonSortCriteria(
                        new SortCriteria("ownerKey"),
                        new SortCriteria("_id")),
                offset,
                length,
                mongoCollection,
                Ontology.class);
    }

    @Override
    public Ontology addTerms(String subjectKey, Long ontologyId, Long parentTermId, List<OntologyTerm> terms, Integer pos) {
        Ontology ontology = findEntityByIdReadableBySubjectKey(ontologyId, subjectKey);
        if (ontology == null) {
            // bad ID or subject does not have access to the ontology
            return null;
        }
        OntologyTerm parentTerm = ontology.findTerm(parentTermId);
        if (parentTerm == null) {
            throw new IllegalArgumentException("Term not found: " + parentTermId);
        } else if (!parentTerm.allowsChildren()) {
            throw new IllegalArgumentException("Term node " + parentTermId + " does not allow children");
        }
        if (CollectionUtils.isEmpty(terms)) {
            return ontology;
        }
        Streams.zip(
                IntStream.range(0, terms.size()).mapToObj(i -> Integer.valueOf(i)),
                terms.stream(),
                (i, t) -> ImmutablePair.of(i, t)
        ).forEach(p -> {
            OntologyTerm newTerm = p.getRight();
            newTerm.setId(createNewId());
            if (pos != null) {
                parentTerm.insertChild(pos + p.getLeft(), p.getRight());
            } else {
                parentTerm.addChild(p.getRight());
            }
        });
        ontology.setUpdatedDate(new Date());

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        DaoUpdateResult updateResult = MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(ontologyId),
                        permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)
                ),
                ImmutableMap.of(
                        "updatedDate", new SetFieldValueHandler<>(ontology.getUpdatedDate()),
                        "terms", new SetFieldValueHandler<>(ontology.getTerms())
                ),
                updateOptions);
        if (updateResult.getEntitiesFound() == 0) {
            // no entity was found for update - this usually happens if the user does not have write permissions
            return null;
        } else {
            return ontology;
        }
    }

    @Override
    public Ontology reorderTerms(String subjectKey, Long ontologyId, Long parentTermId, int[] order) {
        Ontology ontology = findEntityByIdReadableBySubjectKey(ontologyId, subjectKey);
        if (ontology == null) {
            // bad ID or subject does not have access to the ontology
            return null;
        }
        OntologyTerm parentTerm = ontology.findTerm(parentTermId);
        if (parentTerm == null) {
            throw new IllegalArgumentException("Term not found: " + parentTermId);
        }

        List<OntologyTerm> terms = parentTerm.getTerms();
        if (order == null) {
            throw new IllegalArgumentException("Invalid null term order");
        } else if (order.length > terms.size()) {
            throw new IllegalArgumentException("Order array size is greater than the number of terms " +
                    order.length + " vs " + terms.size());
        } else {
            validateTermOrder(order);
        }
        int[] newOrder;
        if (order.length < terms.size()) {
            newOrder = Arrays.copyOf(order, terms.size());
            Arrays.fill(newOrder, order.length, terms.size(), -1);
        } else {
            newOrder = order;
        }
        OntologyTerm[] reorderedTerms = new OntologyTerm[terms.size()];
        Streams.zip(
                IntStream.range(0, terms.size()).mapToObj(i -> Integer.valueOf(i)),
                terms.stream(),
                (i, t) -> newOrder[i] != -1
                        ? ImmutablePair.of(newOrder[i], t)
                        : ImmutablePair.of(i, t)
        ).forEach(p -> {
            reorderedTerms[p.getLeft()] = p.getRight();
        });
        parentTerm.getTerms().clear();
        for (OntologyTerm t : reorderedTerms) {
            parentTerm.addChild(t);
        }
        ontology.setUpdatedDate(new Date());

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(ontologyId),
                        permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)
                ),
                ImmutableMap.of(
                        "updatedDate", new SetFieldValueHandler<>(ontology.getUpdatedDate()),
                        "terms", new SetFieldValueHandler<>(ontology.getTerms())
                ),
                updateOptions);
        return ontology;
    }

    void validateTermOrder(int[] order) {
        Arrays.stream(order)
                .forEach(i -> {
                    if (i >= order.length) {
                        throw new IllegalArgumentException("Index value " + i + " greater than array length " + order.length
                                + " in term order array " + Arrays.toString(order));
                    }
                });
    }

    @Override
    public Ontology removeTerm(String subjectKey, Long ontologyId, Long parentTermId, Long termId) {
        Preconditions.checkArgument(termId != null,
                "The ID of the term to be removed cannot be null");
        Ontology ontology = findEntityByIdReadableBySubjectKey(ontologyId, subjectKey);
        if (ontology == null) {
            // bad ID or subject does not have access to the ontology
            return null;
        }
        OntologyTerm parentTerm = ontology.findTerm(parentTermId);
        if (parentTerm == null) {
            throw new IllegalArgumentException("Term not found: " + parentTermId);
        }
        parentTerm.getTerms().stream()
                .filter(t -> termId.equals(t.getId()))
                .findFirst()
                .ifPresent(t -> parentTerm.removeChild(t));
        ontology.setUpdatedDate(new Date());

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(ontologyId),
                        permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)
                ),
                ImmutableMap.of(
                        "updatedDate", new SetFieldValueHandler<>(ontology.getUpdatedDate()),
                        "terms", new SetFieldValueHandler<>(ontology.getTerms())
                ),
                updateOptions);
        return ontology;
    }
}
