package org.janelia.model.domain.ontology;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.janelia.model.domain.DomainObject;
import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;

/**
 * An ontology is a hierarchy of terms that can be applied to objects as annotations.
 * 
 * Every node in the ontology tree is an OntologyTerm, including the root, which is this class. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="ontology",label="Ontology")
public class Ontology extends OntologyTerm implements DomainObject {

    private String ownerKey;
    private Set<String> readers = new HashSet<>();
    private Set<String> writers = new HashSet<>();

    @SearchAttribute(key="creation_date",label="Creation Date")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date creationDate;

    @SearchAttribute(key="updated_date",label="Updated Date")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date updatedDate;
    
    @Override
    public boolean allowsChildren() {
        return true;
    }

    @SearchAttribute(key="type_label",label="Type")
    @Override
    @JsonIgnore
    public String getType() {
        return "Ontology"; // this must match the MongoMapped.label above
    }
    
    @SearchAttribute(key="search_type",label="Search Type",display=false)
    @JsonIgnore
    public String getSearchType() {
        return getType();
    }
    
    @Override
    @JsonIgnore
    public String getTypeName() {
        return getType();
    }
    
    @SearchAttribute(key="owner",label="Owner",facet="owner_s")
    @JsonIgnore
    public String getOwnerName() {
        return DomainUtils.getNameFromSubjectKey(ownerKey);
    }

    /**
     * For use by Solr. 
     * @see AbstractDomainObject.getSubjectNames
     */
    @SearchAttribute(key="subjects",label="Subjects",display=false)
    @JsonIgnore
    public Set<String> getSubjectNames() {
        Set<String> names = new HashSet<>();
        for(String subjectKey : readers) {
            if (subjectKey==null) continue;
            names.add(DomainUtils.getNameFromSubjectKey(subjectKey));
        }
        return names;
    }

    @SearchAttribute(key="id",label="GUID")
    public Long getId() {
        return super.getId();
    }

    @SearchAttribute(key="name",label="Name")
    public String getName() {
        return super.getName();
    }
    
    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public Set<String> getReaders() {
        return readers;
    }

    public void setReaders(Set<String> readers) {
        if (readers==null) throw new IllegalArgumentException("Property cannot be null");
        this.readers = readers;
    }

    public Set<String> getWriters() {
        return writers;
    }

    public void setWriters(Set<String> writers) {
        if (writers==null) throw new IllegalArgumentException("Property cannot be null");
        this.writers = writers;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
