package org.janelia.it.jacs.model.domain.ontology;

import java.io.Serializable;

public class OntologyTermReference implements Serializable {

    private Long ontologyId;
    private Long ontologyTermId;

    public static OntologyTermReference createFor(OntologyTerm term) {
        OntologyTerm currTerm = term;
        while(!(currTerm instanceof Ontology) && currTerm!=null) {
            currTerm = currTerm.getParent();
        }
        if (currTerm instanceof Ontology) {
            return new OntologyTermReference((Ontology)currTerm, term);
        }
        throw new IllegalArgumentException("Term has no ontology ancestor");
    }
    
    public OntologyTermReference() {
    }
    
    public OntologyTermReference(Long ontologyId, Long ontologyTermId) {
        this.ontologyId = ontologyId;
        this.ontologyTermId = ontologyTermId;
    }
    
    public OntologyTermReference(Ontology ontology, OntologyTerm term) {
        this.ontologyId = ontology.getId();
        this.ontologyTermId = term.getId();
    }

    public Long getOntologyId() {
        return ontologyId;
    }

    public void setOntologyId(Long ontologyId) {
        this.ontologyId = ontologyId;
    }

    public Long getOntologyTermId() {
        return ontologyTermId;
    }

    public void setOntologyTermId(Long ontologyTermId) {
        this.ontologyTermId = ontologyTermId;
    }
}
