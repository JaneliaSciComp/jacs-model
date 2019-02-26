package org.janelia.model.domain.ontology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.janelia.model.domain.interfaces.HasIdentifier;
import org.janelia.model.domain.interfaces.HasName;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class OntologyTerm implements HasIdentifier, HasName, Serializable {
    
    @MongoId
    private Long id;
    
    private String name;
    private List<OntologyTerm> terms = new ArrayList<>();
    private transient OntologyTerm parent;

    @JsonIgnore
    public OntologyTerm getParent() {
        return parent;
    }

    @JsonIgnore
    void setParent(OntologyTerm parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public Ontology getOntology() {
        OntologyTerm curr = this;
        while(curr!=null) {
            if (curr instanceof Ontology) {
                return (Ontology)curr;
            }
            curr = curr.getParent();
        }
        return null;
    }

    /**
     * Return true if the given ontology term has the specified ontology term as a child.
     * @param childTerm child term
     * @return
     */
    @JsonIgnore
    public boolean hasChild(OntologyTerm childTerm) {
        if (childTerm==null) return false;
        for(Iterator<OntologyTerm> i = getTerms().iterator(); i.hasNext(); ) {
            OntologyTerm child = i.next();
            if (child!=null && child.getId()!=null && child.getId().equals(childTerm.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the ontology term with the given id in the specified ontology tree.
     * @param termId GUID of the term to find 
     * @return term with the given termId, or null if it cannot be found
     */
    public OntologyTerm findTerm(Long termId) {
        if (termId==null) return null;
        if (getId()!=null && getId().equals(termId)) {
            return this;
        }
        for(OntologyTerm child : getTerms()) {
            OntologyTerm found = child.findTerm(termId);
            if (found!=null) {
                return found;
            }
        }
        return null;
    }

    public OntologyTerm findTerm(String name) {
        if (name==null) return null;
        if (getName()!=null && getName().equals(name)) {
            return this;
        }
        for(OntologyTerm child : getTerms()) {
            OntologyTerm found = child.findTerm(name);
            if (found!=null) {
                return found;
            }
        }
        return null;
    }
    
    @JsonIgnore
    public boolean hasChildren() {
        return !terms.isEmpty();
    }

    @JsonIgnore
    public int getNumChildren() {
        return terms.size();
    }

    @JsonIgnore
    public void addChild(OntologyTerm term) {
        term.setParent(this);
        terms.add(term);
    }

    @JsonIgnore
    public void insertChild(int index, OntologyTerm term) {
        term.setParent(this);
        terms.add(index, term);
    }

    @JsonIgnore
    public void removeChild(OntologyTerm term) {
        if (terms.remove(term)) {
            term.setParent(null);
        }
    }

    @JsonIgnore
    public abstract boolean allowsChildren();

    @JsonIgnore
    public abstract String getTypeName();

    @JsonProperty
    public List<OntologyTerm> getTerms() {
        return terms;
    }

    @JsonProperty
    public void setTerms(List<OntologyTerm> terms) {
        if (terms==null) throw new IllegalArgumentException("Property cannot be null");
        this.terms = terms;
        for (OntologyTerm term : terms) {
            term.setParent(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
