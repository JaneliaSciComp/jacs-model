package org.janelia.model.domain;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;
import org.janelia.model.util.ModelStringUtil;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Every top-level "domain object" we store in MongoDB has a core set of attributes
 * which allow for identification (id/name) and permissions (owner/readers/writers)
 * as well as safe-updates with updatedDate.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AbstractDomainObject implements DomainObject, Serializable {
    
    @MongoId
    @JsonProperty(value="_id")
    @SearchAttribute(key="id",label="GUID")
    private Long id;

    @SearchAttribute(key="name",label="Name")
    private String name;
    
    private String ownerKey;
    private Set<String> readers = new HashSet<>();
    private Set<String> writers = new HashSet<>();

    @SearchAttribute(key="creation_date",label="Creation Date")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date creationDate;

    @SearchAttribute(key="updated_date",label="Updated Date")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date updatedDate;

    @SearchAttribute(key="owner",label="Owner",facet="owner_s")
    @JsonIgnore
    public String getOwnerName() {
        return DomainUtils.getNameFromSubjectKey(ownerKey);
    }
    
    /**
     * Returns the names of all the readers (i.e. drops the "user:" or "group:" prefixes). This is used by 
     * the Solr indexing system for regulating search access. 
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

    /**
     * Attempts to find and return a type name for the current class. First, it checks the class
     * hierarchy for a SearchType.label, if none is found, it checks for a MongoMapped.label. The 
     * purpose of this method is to provide a nice user-readable label.
     */
    @SearchAttribute(key="type_label",label="Type")
    @JsonIgnore
    public String getType() {
        Class<?> clazz = getClass();
        SearchType searchType = clazz.getAnnotation(SearchType.class);
        if (searchType!=null) {
            return searchType.label();
        }
        MongoMapped mongoMapped = clazz.getAnnotation(MongoMapped.class);
        if (mongoMapped!=null) {
            return mongoMapped.label();
        }
        return ModelStringUtil.splitCamelCase(clazz.getSimpleName());
    }
    
    /**
     * Attempts to find and return a @SearchType.key defined for this object or any of its super types. 
     * If no @SearchType annotation is defined, this method returns null.
     * This field is indexed in SOLR for searching purposes.
     */
    @SearchAttribute(key="search_type",label="Search Type",display=false)
    @JsonIgnore
    public String getSearchType() {
        Class<?> clazz = this.getClass();
        while (clazz!=null) {
            SearchType searchType = clazz.getAnnotation(SearchType.class);
            if (searchType!=null) {
                return searchType.key();
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Return all @SearchType.key values defined for this object and its super types.
     * This field is indexed in SOLR for searching purposes.
     */
    @SearchAttribute(key="search_type_sm",label="Search Types",display=false)
    @JsonIgnore
    public Set<String> getSearchTypes() {
        Set<String> searchTypes = new HashSet<>();
        Class<?> clazz = this.getClass();
        while (clazz!=null) {
            SearchType searchType = clazz.getAnnotation(SearchType.class);
            if (searchType!=null) {
                searchTypes.add(searchType.key());
            }
            clazz = clazz.getSuperclass();
        }
        return searchTypes;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getOwnerKey() {
        return ownerKey;
    }

    @Override
    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    @Override
    public Set<String> getReaders() {
        return readers;
    }

    @Override
    public void setReaders(Set<String> readers) {
        if (readers==null) throw new IllegalArgumentException("Property cannot be null");
        this.readers = readers;
    }

    public boolean addReaders(Collection<String> readers) {
        if (CollectionUtils.isNotEmpty(readers)) {
            return this.readers.addAll(readers);
        } else {
            return false;
        }
    }

    @Override
    public Set<String> getWriters() {
        return writers;
    }

    @Override
    public void setWriters(Set<String> writers) {
        if (writers==null) throw new IllegalArgumentException("Property cannot be null");
        this.writers = writers;
    }

    public boolean addWriters(Collection<String> writers) {
        if (CollectionUtils.isNotEmpty(writers)) {
            return this.writers.addAll(writers);
        } else {
            return false;
        }
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Date getUpdatedDate() {
        return updatedDate;
    }

    @Override
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AbstractDomainObject that = (AbstractDomainObject) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(name, that.name)
                .append(ownerKey, that.ownerKey)
                .append(creationDate, that.creationDate)
                .append(updatedDate, that.updatedDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(ownerKey)
                .append(creationDate)
                .append(updatedDate)
                .toHashCode();
    }
}
