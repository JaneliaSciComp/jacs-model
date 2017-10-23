package org.janelia.it.jacs.model.domain.ontology;

import org.janelia.it.jacs.model.domain.AbstractDomainObject;
import org.janelia.it.jacs.model.domain.Reference;
import org.janelia.it.jacs.model.domain.support.MongoMapped;

/**
 * An annotation on a single domain object, using some Ontology.  
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="annotation",label="Annotation")
public class Annotation extends AbstractDomainObject {
    
    private Reference target;
    private OntologyTermReference keyTerm;
    private OntologyTermReference valueTerm;
    private String key;
    private String value;
    private boolean computational = false;

    public Annotation() {
    }

    public Annotation(Annotation baseAnnotation) {
        setName(baseAnnotation.getName());
        setTarget(baseAnnotation.getTarget());
        setOwnerKey(baseAnnotation.getOwnerKey());
        setKey(baseAnnotation.getKey());
        setKeyTerm(baseAnnotation.getKeyTerm());
        setValue(baseAnnotation.getValue());
        setValueTerm(baseAnnotation.getValueTerm());
        setReaders(baseAnnotation.getReaders());
        setWriters(baseAnnotation.getWriters());
        setComputational(baseAnnotation.isComputational());
    }
    
    public Reference getTarget() {
        return target;
    }

    public void setTarget(Reference target) {
        this.target = target;
    }

    public OntologyTermReference getKeyTerm() {
        return keyTerm;
    }

    public void setKeyTerm(OntologyTermReference keyTerm) {
        this.keyTerm = keyTerm;
    }

    public OntologyTermReference getValueTerm() {
        return valueTerm;
    }

    public void setValueTerm(OntologyTermReference valueTerm) {
        this.valueTerm = valueTerm;
    }

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    public boolean isComputational() {
        return computational;
    }

    public void setComputational(boolean computational) {
        this.computational = computational;
    }

    @Override
    public String toString() {
        return "Annotation[" +
                "id=" + getId() +
                ", name=" + getName() +
                ", target='" + target + '\'' +
                ']';
    }
    
}
