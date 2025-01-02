package org.janelia.model.access.domain.dao.mongo;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.*;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * {@link Annotation} Mongo DAO.
 */
public class AnnotationMongoDao extends AbstractDomainObjectMongoDao<Annotation> implements AnnotationDao {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationMongoDao.class);

    private OntologyMongoDao ontologyMongoDao;

    @Inject
    AnnotationMongoDao(MongoDatabase mongoDatabase,
                       TimebasedIdentifierGenerator idGenerator,
                       DomainPermissionsMongoHelper permissionsHelper,
                       DomainUpdateMongoHelper updateHelper,
                       OntologyMongoDao ontologyMongoDao) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
        this.ontologyMongoDao = ontologyMongoDao;
    }

    @Override
    public Annotation createAnnotation(String subjectKey, Reference target, OntologyTermReference ontologyTermReference, String value) {

        LOG.debug("createAnnotation({}, target={}, ontologyTerm={}, value={})", subjectKey, target, ontologyTermReference, value);

        Ontology ontology = ontologyMongoDao.findById(ontologyTermReference.getOntologyId());
        OntologyTerm ontologyTerm = ontology.findTerm(ontologyTermReference.getOntologyTermId());

        OntologyTerm keyTerm = ontologyTerm;
        OntologyTerm valueTerm = null;
        String keyString = keyTerm.getName();
        String valueString = value;

        if (keyTerm instanceof EnumItem) {
            keyTerm = ontologyTerm.getParent();
            valueTerm = ontologyTerm;
            keyString = keyTerm.getName();
            valueString = valueTerm.getName();
        }

        final Annotation annotation = new Annotation();
        annotation.setKey(keyString);
        annotation.setValue(valueString);
        annotation.setTarget(target);

        annotation.setKeyTerm(new OntologyTermReference(ontology, keyTerm));
        if (valueTerm != null) {
            annotation.setValueTerm(new OntologyTermReference(ontology, valueTerm));
        }

        annotation.setName(keyTerm.createAnnotationName(annotation));

        Annotation savedAnnotation = saveBySubjectKey(annotation, subjectKey);
        LOG.trace("Saved annotation as " + savedAnnotation.getId());

        // TODO: auto-share annotation based on auto-share template (this logic is currently in the client)
        return savedAnnotation;
    }

    @Override
    public Annotation updateAnnotationValue(String subjectKey, Long annotationId, String newValue) {

        Date updatedDate = new Date();
        Annotation annotation = findEntityByIdReadableBySubjectKey(annotationId, subjectKey);

        if (annotation==null) {
            throw new IllegalArgumentException("Annotation#"+annotationId+" not found");
        }

        String name = annotation.getName();
        String newName = name.substring(0, name.indexOf("=") + 2) + newValue;

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        DaoUpdateResult res = MongoDaoHelper.updateMany(mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                    MongoDaoHelper.createFilterByIds(Collections.singletonList(annotationId)),
                    permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)
                ),
                ImmutableMap.of(
                    "updatedDate", new SetFieldValueHandler<>(updatedDate),
                    "value", new SetFieldValueHandler<>(newValue),
                    "name", new SetFieldValueHandler<>(newName)
                ), updateOptions);

        if (res.getEntitiesFound()!=1) {
            throw new IllegalArgumentException("Annotation#"+annotationId+" not found");
        }

        if (res.getEntitiesAffected()!=1) {
            // No updates made
            return annotation;
        }

        annotation.setUpdatedDate(updatedDate);
        annotation.setValue(newValue);
        annotation.setName(newName);
        return annotation;
    }

    @Override
    public List<Annotation> findAnnotationsByTargets(Collection<Reference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return Collections.emptyList();
        } else {
            return find(
                    MongoDaoHelper.createFilterCriteria(
                            Filters.in("target", references.stream().filter(Objects::nonNull).map(Reference::toString).collect(Collectors.toSet()))),
                    null,
                    0,
                    -1,
                    getEntityType());
        }
    }

    @Override
    public List<Annotation> findAnnotationsByTargetsAccessibleBySubjectKey(Collection<Reference> references, String subjectKey) {
        if (CollectionUtils.isEmpty(references)) {
            return Collections.emptyList();
        } else {
            return find(
                    MongoDaoHelper.createFilterCriteria(
                            Filters.in("target", references.stream().filter(Objects::nonNull).map(Reference::toString).collect(Collectors.toSet())),
                            permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                    null,
                    0,
                    -1,
                    getEntityType());
        }
    }
}
