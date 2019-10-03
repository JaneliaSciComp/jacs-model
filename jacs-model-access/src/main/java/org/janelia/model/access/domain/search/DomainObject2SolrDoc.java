package org.janelia.model.access.domain.search;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.solr.common.SolrInputDocument;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainObjectGetter;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;
import org.janelia.model.domain.ontology.DomainAnnotationGetter;
import org.janelia.model.domain.ontology.SimpleDomainAnnotation;
import org.janelia.model.domain.searchable.SearchableDocType;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.workspace.AllNodeAncestorsGetter;
import org.janelia.model.domain.workspace.NodeAncestorsGetter;
import org.janelia.model.security.util.SubjectUtils;
import org.janelia.model.util.ReflectionHelper;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DomainObject2SolrDoc {

    private static final Logger LOG = LoggerFactory.getLogger(DomainObject2SolrDoc.class);
    private static final String JANELIA_MODEL_PACKAGE = "org.janelia.model.domain";

    private final NodeAncestorsGetter nodeAncestorsGetter;
    private final DomainAnnotationGetter nodeAnnotationGetter;
    private final DomainObjectGetter objectGetter;

    private static class FullTextIndexableValues {
        Map<Reference, Set<SimpleDomainAnnotation>> refAnnotationsMap = new HashMap<>();
        Multimap<String, String> fulltextIndexedFields = HashMultimap.create();

        void addFullTextIndexedField(String fieldName, String fieldValue) {
            if (StringUtils.isNotBlank(fieldValue))
                fulltextIndexedFields.put(fieldName, fieldValue);
        }

        void addReferenceAnnotations(Reference ref, Set<SimpleDomainAnnotation> refAnnotations) {
            if (CollectionUtils.isNotEmpty(refAnnotations))
                refAnnotationsMap.put(ref, refAnnotations);
        }
    }

    DomainObject2SolrDoc(NodeAncestorsGetter nodeAncestorsGetter,
                         DomainAnnotationGetter nodeAnnotationGetter,
                         DomainObjectGetter objectGetter) {
        this.nodeAncestorsGetter = nodeAncestorsGetter;
        this.nodeAnnotationGetter = nodeAnnotationGetter;
        this.objectGetter = objectGetter;
    }

    SolrInputDocument domainObjectToSolrDocument(DomainObject domainObject) {
        Set<Reference> ancestorReferences = nodeAncestorsGetter.getNodeAncestors(Reference.createFor(domainObject));
        Set<Long> ancestorIds;
        if (CollectionUtils.isNotEmpty(ancestorReferences)) {
            ancestorIds = ancestorReferences.stream().map(ref -> ref.getTargetId()).collect(Collectors.toSet());
        } else {
            ancestorIds = Collections.emptySet();
        }
        return createSolrDoc(domainObject, ancestorIds);
    }

    @SuppressWarnings("unchecked")
    private SolrInputDocument createSolrDoc(DomainObject domainObject, Set<Long> ancestorIds) {
        SolrInputDocument solrDoc = new SolrInputDocument();
        solrDoc.setField("doc_type", SearchableDocType.DOCUMENT.name(), 1.0f);
        solrDoc.setField("class", domainObject.getClass().getName(), 1.0f);
        solrDoc.setField("collection", DomainUtils.getCollectionName(domainObject), 1.0f);
        solrDoc.setField("ancestor_ids", new ArrayList<>(ancestorIds), 0.2f);

        Map<String, Object> attrs = new HashMap<>();

        BiConsumer<SearchAttribute, Object> searchFieldHandler = (searchAttribute, fieldValue) -> {
            if (fieldValue != null && !(fieldValue instanceof String && StringUtils.isBlank((String) fieldValue))) {
                attrs.put(searchAttribute.key(), fieldValue);
                if (StringUtils.isNotEmpty(searchAttribute.facet())) {
                    attrs.put(searchAttribute.facet(), fieldValue);
                }
            }
        };

        Set<Field> searchableFields = ReflectionUtils.getAllFields(domainObject.getClass(), ReflectionUtils.withAnnotation(SearchAttribute.class));
        for (Field field : searchableFields) {
            try {
                SearchAttribute searchAttributeAnnot = field.getAnnotation(SearchAttribute.class);
                Object value = ReflectionHelper.getFieldValue(domainObject, field.getName());
                searchFieldHandler.accept(searchAttributeAnnot, value);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No such field " + field.getName() + " on object " + domainObject, e);
            }
        }

        Set<Method> searchableProperties = ReflectionUtils.getAllMethods(domainObject.getClass(), ReflectionUtils.withAnnotation(SearchAttribute.class));
        for (Method propertyMethod : searchableProperties) {
            try {
                SearchAttribute searchAttributeAnnot = propertyMethod.getAnnotation(SearchAttribute.class);
                Object value = propertyMethod.invoke(domainObject);
                searchFieldHandler.accept(searchAttributeAnnot, value);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalArgumentException("Problem executing " + propertyMethod.getName() + " on object " + domainObject, e);
            }
        }

        attrs.forEach((k, v) -> {
            if (v != null) {
                solrDoc.addField(k, v, 1.0f);
            }
        });

        FullTextIndexableValues fullTextIndexableValues = getFullTextIndexedValues(domainObject);
        solrDoc.setField("fulltext_mt", ImmutableSet.copyOf(fullTextIndexableValues.fulltextIndexedFields.values()), 0.8f);

        fullTextIndexableValues.refAnnotationsMap.values().stream()
                .flatMap(refAnnotations -> refAnnotations.stream())
                .flatMap(sda -> sda.getSubjects().stream().map(s -> ImmutablePair.of(SubjectUtils.getSubjectName(s), sda.getTag())))
                .forEach(subjectTagPair -> {
                    solrDoc.addField(subjectTagPair.getLeft() + "_annotations", subjectTagPair.getRight(), 1.0f);
                });

        return solrDoc;
    }

    private FullTextIndexableValues getFullTextIndexedValues(DomainObject domainObject) {
        Set<Reference> visited = new HashSet<>();
        FullTextIndexableValues fullTextIndexableValues = new FullTextIndexableValues();
        traverseDomainObjectFieldsForFullTextIndexedValues(
                domainObject,
                domainObject,
                true, // for the root object ignore searcheable fields because they are indexed as SOLR fields already
                visited,
                fullTextIndexableValues);
        return fullTextIndexableValues;
    }

    private void traverseDomainObjectFieldsForFullTextIndexedValues(DomainObject rootObject, DomainObject currentObject, boolean ignoreSearchableFields, Set<Reference> visited, FullTextIndexableValues fullTextIndexableValue) {
        Reference currentObjectReference = Reference.createFor(currentObject);
        visited.add(currentObjectReference);
        fullTextIndexableValue.addReferenceAnnotations(currentObjectReference, nodeAnnotationGetter.getAnnotations(currentObjectReference));
        @SuppressWarnings("unchecked")
        Set<Field> currentObjectFields = ReflectionUtils.getAllFields(currentObject.getClass());
        currentObjectFields.stream()
                .filter(f -> !ignoreSearchableFields || f.getAnnotation(SearchAttribute.class) == null)
                .forEach(field -> traverseObjectFieldForFullTextIndexedValues(rootObject, currentObject, field, visited, fullTextIndexableValue));
    }


    private void traverseObjectFieldForFullTextIndexedValues(DomainObject rootObject, Object currentObject, Field field, Set<Reference> visited, FullTextIndexableValues fullTextIndexableValue) {
        if (currentObject == null) {
            return;
        }
        if (Modifier.isTransient(field.getModifiers())) {
            return; // skip transient fields
        }
        if (!isTraversable(field, rootObject)) {
            return; // the field is not traversable - rootObject's type is not in the allowed values specified in the SearchTraversal annotation
        }
        if (currentObject instanceof String) {
            fullTextIndexableValue.addFullTextIndexedField(field.getName(), (String) currentObject);
            return;
        }
        Object fieldValue = ReflectionHelper.getFieldValue(currentObject, field);
        if (fieldValue == null) {
            return;
        }
        Class<?> fieldValueClass = fieldValue.getClass();
        if (fieldValueClass.isEnum()) {
            fullTextIndexableValue.addFullTextIndexedField(field.getName(), fieldValue.toString());
        } else if (fieldValue instanceof String) {
            fullTextIndexableValue.addFullTextIndexedField(field.getName(), (String) fieldValue);
        } else if (fieldValue instanceof Map) {
            Map<?, ?> mapValue = (Map<?, ?>) fieldValue;
            traverseCollectionFieldForFullTextIndexedValues(rootObject, mapValue.values(), field, visited, fullTextIndexableValue);
        } else if (fieldValue instanceof Collection) {
            Collection<?> listValue = (Collection<?>) fieldValue;
            traverseCollectionFieldForFullTextIndexedValues(rootObject, listValue, field, visited, fullTextIndexableValue);
        } else if (fieldValue instanceof Reference) {
            Reference ref = (Reference) fieldValue;
            // Don't fetch objects which we've already visited
            if (visited.contains(ref)) {
                return;
            }
            DomainObject refObj = objectGetter.getDomainObjectByReference(ref);
            if (refObj == null) {
                LOG.warn("No domain object found for field {} of {} with value {}", field, currentObject, ref);
            } else {
                traverseDomainObjectFieldsForFullTextIndexedValues(rootObject, refObj, false, visited, fullTextIndexableValue);
            }
        } else if (fieldValue instanceof ReverseReference) {
            ReverseReference reverseRef = (ReverseReference) fieldValue;
            List<? extends DomainObject> refObjs = objectGetter.getDomainObjectsReferencedBy(reverseRef);
            refObjs.forEach(refObj -> traverseDomainObjectFieldsForFullTextIndexedValues(rootObject, refObj, false, visited, fullTextIndexableValue));
        } else if (fieldValueClass.getName().startsWith(JANELIA_MODEL_PACKAGE)) {
            DomainObject fieldValueAsDomainObject = (DomainObject) fieldValue;
            traverseDomainObjectFieldsForFullTextIndexedValues(rootObject, fieldValueAsDomainObject, false, visited, fullTextIndexableValue);
        }
        // Ignore everything else
    }

    /**
     * Check if the field is traversable. A non annotated field is always traversable, otherwise if it is annotated with SearchTraversal is
     * only traversable if the given object's class is in the list of values specified in the instance of the SearchTraversal
     * @param field
     * @param object
     * @return
     */
    private boolean isTraversable(Field field, Object object) {
        SearchTraversal searchTraversal = field.getAnnotation(SearchTraversal.class);
        // Traverse every non-annotated field
        if (searchTraversal == null) {
            return true;
        }
        for (Class<?> allowedClass : searchTraversal.value()) {
            if (allowedClass.equals(object.getClass())) {
                return true; // field is traversable when started from an object that is of a type in the allowed values
            }
        }
        // even though the field is marked as traversable it was not reached from an object of an allowed type
        return false;
    }

    /**
     * Traverse the members of the collection to add the to the indexed fields.
     *
     * @param rootObject
     * @param currentCollection
     * @param field
     * @param visited
     * @param fullTextIndexableValue
     */
    private void traverseCollectionFieldForFullTextIndexedValues(DomainObject rootObject,
                                                                 Collection<?> currentCollection,
                                                                 Field field,
                                                                 Set<Reference> visited,
                                                                 FullTextIndexableValues fullTextIndexableValue) {
        currentCollection.stream()
                .filter(Objects::nonNull)
                .forEach(member -> {
                    Class<?> memberClass = member.getClass();
                    if (member instanceof String) {
                        fullTextIndexableValue.addFullTextIndexedField(field.getName(), (String) member);
                    } else if (member instanceof Reference) {
                        Reference ref = (Reference) member;
                        // Don't fetch objects which we've already visited
                        if (visited.contains(ref)) {
                            return;
                        }
                        DomainObject refObj = objectGetter.getDomainObjectByReference(ref);
                        if (refObj == null) {
                            LOG.warn("No domain object found for collection member of field {} value {}", field, ref);
                        } else {
                            traverseDomainObjectFieldsForFullTextIndexedValues(rootObject, refObj, false, visited, fullTextIndexableValue);
                        }
                    } else if (memberClass.getName().startsWith(JANELIA_MODEL_PACKAGE)) {
                        DomainObject memberAsDomainObject = (DomainObject) member;
                        traverseDomainObjectFieldsForFullTextIndexedValues(rootObject, memberAsDomainObject, false, visited, fullTextIndexableValue);
                    }
                });
    }
}
