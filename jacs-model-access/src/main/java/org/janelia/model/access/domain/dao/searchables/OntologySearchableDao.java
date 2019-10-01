package org.janelia.model.access.domain.dao.searchables;

import java.util.List;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.OntologyDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.domain.ontology.OntologyTerm;

/**
 * {@link Ontology} DAO.
 */
@AsyncIndex
public class OntologySearchableDao extends AbstractDomainSearchablDao<Ontology> implements OntologyDao {

    private final OntologyDao ontologyDao;

    @Inject
    OntologySearchableDao(OntologyDao ontologyDao,
                          @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(ontologyDao, objectIndexer);
        this.ontologyDao = ontologyDao;
    }

    @Override
    public List<Ontology> getOntologiesAccessibleBySubjectGroups(String subjectKey, long offset, int length) {
        return ontologyDao.getOntologiesAccessibleBySubjectGroups(subjectKey, offset, length);
    }

    @Override
    public Ontology addTerms(String subjectKey, Long ontologyId, Long parentTermId, List<OntologyTerm> terms, Integer pos) {
        Ontology updatedOntology = ontologyDao.addTerms(subjectKey, ontologyId, parentTermId, terms, pos);
        domainObjectIndexer.indexDocument(updatedOntology);
        return updatedOntology;
    }

    @Override
    public Ontology reorderTerms(String subjectKey, Long ontologyId, Long parentTermId, int[] order) {
        return ontologyDao.reorderTerms(subjectKey, ontologyId, parentTermId, order);
    }

    @Override
    public Ontology removeTerm(String subjectKey, Long ontologyId, Long parentTermId, Long termId) {
        Ontology updatedOntology = ontologyDao.removeTerm(subjectKey, ontologyId, parentTermId, termId);
        domainObjectIndexer.indexDocument(updatedOntology);
        return updatedOntology;
    }
}
