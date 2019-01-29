package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.domain.ontology.OntologyTerm;

import java.util.Collection;
import java.util.List;

/**
 * Ontology data access object
 */
public interface OntologyDao extends DomainObjectDao<Ontology> {
    List<Ontology> getOntologiesAccessibleBySubjectGroups(String subjectKey, long offset, int length);

    /**
     * Add the given list of terms to the specified ontology as children of the parent term id at the specified position
     * @param subjectKey term definition owner
     * @param ontologyId ontology identifier
     * @param parentTermId parent term
     * @param terms terms to be added
     * @param pos where the terms are inserted
     * @return
     */
    Ontology addTerms(String subjectKey, Long ontologyId, Long parentTermId, List<OntologyTerm> terms, Integer pos);

    /**
     * Reorder the terms that are direct children of the specified term based on the specified order.
     * @param subjectKey
     * @param ontologyId
     * @param parentTermId
     * @param order the new term order order[i] contains the index of the term at index i.
     * @return
     */
    Ontology reorderTerms(String subjectKey, Long ontologyId, Long parentTermId, int[] order);

    /**
     * Remove the specified term from the ontology.
     *
     * @param subjectKey
     * @param ontologyId
     * @param parentTermId
     * @param termId
     * @return
     */
    Ontology removeTerm(String subjectKey, Long ontologyId, Long parentTermId, Long termId);

}
