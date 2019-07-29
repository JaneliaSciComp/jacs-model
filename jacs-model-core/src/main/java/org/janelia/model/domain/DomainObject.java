package org.janelia.model.domain;

import java.util.Date;
import java.util.Set;

import org.janelia.model.domain.interfaces.HasIdentifier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.janelia.model.domain.interfaces.HasName;

/**
 * A domain object is anything stored at the top level of a collection. 
 * It must have a GUID, a name, and user ownership/permissions. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface DomainObject extends HasIdentifier, HasName {

    /** Returns a Globally Unique Identifier for the object */
    @Override
    Long getId();

    void setId(Long id);

    /** Returns a user-readable, non-unique label for the object instance */
    @Override
    String getName();

    void setName(String name);
    
    /** Returns the key for the subject who knows the object instance */
    String getOwnerKey();

    void setOwnerKey(String ownerKey);

    /** Returns all the keys of subjects who have read access to the object instance */ 
    Set<String> getReaders();

    void setReaders(Set<String> readers);

    /** Returns all the keys of subjects who have write access to the object instance */
    Set<String> getWriters();

    void setWriters(Set<String> writers);

    /** Returns the date/time when the object was created */
    Date getCreationDate();

    void setCreationDate(Date creationDate);

    /** Returns the date/time when the object was last updated */
    Date getUpdatedDate();

    void setUpdatedDate(Date updatedDate);
    
    /** Returns a user-readable label for the domain object sub-type */
    String getType();
}
