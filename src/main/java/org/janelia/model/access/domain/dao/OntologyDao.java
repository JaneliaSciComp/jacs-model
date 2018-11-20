package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.ontology.Ontology;

import java.util.List;

/**
 * Ontology data access object
 */
public interface OntologyDao extends DomainObjectDao<Ontology> {
    List<Ontology> getOntologiesAccessibleBySubjectGroups(String subjectKey, long offset, int length);
}
