package org.janelia.model.domain.workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A generic node in a domain object tree. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="treeNode",label="Folder")
@SearchType(key="treeNode",label="Folder")
public class TreeNode extends AbstractDomainObject implements IsParent {

    @SearchTraversal({})
    private List<Reference> children = new ArrayList<>();

    /**
     * Return true if the given tree node has the specified domain object as a child.
     * @param domainObject
     * @return
     */
    public boolean hasChild(DomainObject domainObject) {
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
    public boolean hasChild(Reference reference) {
        return getChildren().contains(reference);
    }
    
    @JsonIgnore
    public boolean hasChildren() {
    	return !children.isEmpty();
    }

    @JsonIgnore
    public int getNumChildren() {
        return children.size();
    }

    public void addChild(Reference ref) {
        children.add(ref);
    }

    public void insertChild(int index, Reference ref) {
        children.add(index, ref);
    }

    public void removeChild(Reference ref) {
        // Remove all references, in case there are duplicates
        for (Iterator<Reference> iterator = children.iterator(); iterator.hasNext();) {
            Reference reference = iterator.next();
            if (reference.equals(ref)) {
                iterator.remove();
            }
        }
    }

    public List<Reference> getChildren() {
        return children;
    }

    public void setChildren(List<Reference> children) {
        if (children==null) throw new IllegalArgumentException("Property cannot be null");
        this.children = children;
    }
}
