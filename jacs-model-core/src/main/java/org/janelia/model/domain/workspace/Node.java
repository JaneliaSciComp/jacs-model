package org.janelia.model.domain.workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.interfaces.IsParent;

import java.util.Iterator;
import java.util.List;

/**
 * A mix-in which makes an object into a node in a tree which has child references.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface Node extends DomainObject, IsParent {

    public List<Reference> getChildren();

    public void setChildren(List<Reference> children);

    /**
     * Return true if the given tree node has the specified domain object as a child.
     * @param domainObject
     * @return
     */
    @JsonIgnore
    default boolean hasChild(DomainObject domainObject) {
        for(Reference ref : getChildren()) {
            if (ref.getTargetId().equals(domainObject.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the given tree node has the specified domain object as a child.
     * @param reference
     * @return
     */
    @JsonIgnore
    default boolean hasChild(Reference reference) {
        return getChildren().contains(reference);
    }

    @JsonIgnore
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    @JsonIgnore
    default int getNumChildren() {
        return getChildren().size();
    }

    default void addChild(Reference ref) {
        getChildren().add(ref);
    }

    default void insertChild(int index, Reference ref) {
        getChildren().add(index, ref);
    }

    default void removeChild(Reference ref) {
        // Remove all references, in case there are duplicates
        for (Iterator<Reference> iterator = getChildren().iterator(); iterator.hasNext();) {
            Reference reference = iterator.next();
            if (reference.equals(ref)) {
                iterator.remove();
            }
        }
    }

}
