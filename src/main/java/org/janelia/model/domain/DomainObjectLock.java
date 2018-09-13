package org.janelia.model.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.janelia.model.domain.support.MongoMapped;
import org.jongo.marshall.jackson.oid.MongoId;

import java.util.Date;

/**
 * Database lock for writing to a domain object. Lock the object before fetching it.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="objectLock",label="Domain Object Lock")
public class DomainObjectLock {
    
    @MongoId
    private String id;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date creationDate;
    
    private String ownerKey;
    
    private Long taskId;
    
    private Reference objectRef;
    
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Reference getObjectRef() {
        return objectRef;
    }

    public void setObjectRef(Reference objectRef) {
        this.objectRef = objectRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SampleLock [");
        if (id != null) {
            builder.append("id=");
            builder.append(id);
            builder.append(", ");
        }
        if (creationDate != null) {
            builder.append("creationDate=");
            builder.append(creationDate);
            builder.append(", ");
        }
        if (ownerKey != null) {
            builder.append("ownerKey=");
            builder.append(ownerKey);
            builder.append(", ");
        }
        if (taskId != null) {
            builder.append("taskId=");
            builder.append(taskId);
            builder.append(", ");
        }
        if (objectRef != null) {
            builder.append("objectRef=");
            builder.append(objectRef);
            builder.append(", ");
        }
        if (description != null) {
            builder.append("description=");
            builder.append(description);
        }
        builder.append("]");
        return builder.toString();
    }
}
