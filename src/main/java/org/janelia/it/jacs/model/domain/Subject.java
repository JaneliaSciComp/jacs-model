package org.janelia.it.jacs.model.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.janelia.it.jacs.model.domain.interfaces.HasIdentifier;
import org.janelia.it.jacs.model.domain.support.MongoMapped;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A user or group in the Workstation system. Part of the permission model that is 
 * used to allow fine-grained or bulk access to individual pieces of data. 
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="subject",label="Subject")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class Subject implements HasIdentifier, Serializable {
    
    public static final String ADMIN_KEY = "group:admin";
    public static final String USERS_KEY = "group:workstation_users";
    
    @MongoId
    private Long id;
    private String key;
    private String name;
    private String fullName;

    private Set<String> groups = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
