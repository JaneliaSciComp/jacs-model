package org.janelia.it.jacs.model.domain;

import java.io.Serializable;

import org.janelia.it.jacs.model.domain.interfaces.HasIdentifier;
import org.janelia.it.jacs.model.domain.support.MongoMapped;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A subject's preference. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="preference",label="User Preference")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public class Preference implements HasIdentifier, Serializable {
    
    @MongoId
    private Long id;
    private String subjectKey;
    private String category;
    private String key;
    private Object value;

    public Preference() {
    }
    
    public Preference(String subjectKey, String category, String key, Object value) {
        this.subjectKey = subjectKey;
        this.category = category;
        this.key = key;
        this.value = value;
    }
    
    public String getCategory() {
        return category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectId(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Preference [id=" + id + ", subjectKey=" + subjectKey + ", category=" + category + ", key=" + key + ", value=" + value + "]";
    }
    
}
