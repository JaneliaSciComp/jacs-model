package org.janelia.it.jacs.model.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A reference to a DomainObject in a specific collection.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class Reference implements Serializable {

    private String targetClassName;
    private Long targetId;

    public Reference() {
    }
    
    private Reference(String className, Long id) {
        this.targetClassName = className;
        this.targetId = id;
    }

    @JsonIgnore
    public String getTargetClassName() {
        return targetClassName;
    }

    @JsonIgnore
    public void setTargetClassName(String className) {
        this.targetClassName = className;
    }

    @JsonIgnore
    public Long getTargetId() {
        return targetId;
    }

    @JsonIgnore
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime*result
                +((targetId==null) ? 0 : targetId.hashCode());
        result = prime*result
                +((targetClassName==null) ? 0 : targetClassName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj) {
            return true;
        }
        if (obj==null) {
            return false;
        }
        if (getClass()!=obj.getClass()) {
            return false;
        }
        Reference other = (Reference) obj;
        if (targetId==null) {
            if (other.targetId!=null) {
                return false;
            }
        }
        else if (!targetId.equals(other.targetId)) {
            return false;
        }
        if (targetClassName==null) {
            if (other.targetClassName!=null) {
                return false;
            }
        }
        else if (!targetClassName.equals(other.targetClassName)) {
            return false;
        }
        return true;
    }

    @JsonValue
    @Override
    public String toString() {
        return targetClassName + "#" + targetId;
    }
    
    public static Reference createFor(DomainObject domainObject) {
        if (domainObject==null) throw new IllegalArgumentException("Null domain object");
        return new Reference(domainObject.getClass().getSimpleName(), domainObject.getId());
    }

    public static Reference createFor(Class<?> clazz, Long id) {
        if (clazz==null) throw new IllegalArgumentException("Null domain object class");
        return new Reference(clazz.getSimpleName(), id);
    }
    
    public static Reference createFor(String className, Long id) {
        if (className==null) throw new IllegalArgumentException("Null domain object class name");
        if (id==null) throw new IllegalArgumentException("Null domain object id");
        return new Reference(className, id);
    }
    
    @JsonCreator
    public static Reference createFor(String strRef) {
        if (strRef==null) throw new IllegalArgumentException("Null string reference");
        String[] s = strRef.split("#");
        String className = s[0];
        Long id = Long.parseLong(s[1]);
        return new Reference(className, id);
    }
}
