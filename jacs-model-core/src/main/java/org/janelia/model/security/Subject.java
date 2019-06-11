package org.janelia.model.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.janelia.model.domain.interfaces.HasIdentifier;
import org.janelia.model.domain.interfaces.HasName;
import org.janelia.model.domain.support.MongoMapped;
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
public abstract class Subject implements HasIdentifier, HasName, Serializable {
    
    public static final String ADMIN_KEY = "group:admin";
    public static final String USERS_KEY = "group:workstation_users";

    @JsonIgnore
    private final Set<GroupRole> roles = new HashSet<>();

    @MongoId
    @JsonProperty(value="_id")
    private Long id;
    private String key;
    private String name;
    private String fullName;

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

    /**
     * Full name of the user or group.
     */
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void addRoles(GroupRole ...roles) {
        for (GroupRole r : roles) {
            if (r != null) {
                this.roles.add(r);
            }
        }
    }

    public boolean hasReadPrivilege() {
        return hasPrivilege(roles.stream(), GroupRole::isRead);
    }

    public boolean hasWritePrivilege() {
        return hasPrivilege(roles.stream(), GroupRole::isWrite);
    }

    protected boolean hasPrivilege(Stream<GroupRole> roleStream, Predicate<GroupRole> privilegeFilter) {
        return roleStream.filter(privilegeFilter).findFirst().map(r -> true).orElse(false);
    }

}
