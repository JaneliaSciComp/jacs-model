package org.janelia.model.domain.workspace;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of items in a GroupedFolder. The children are always Group objects.
 *
 * The proxy object is an optional object which represents the group. If not null,
 * its name and image will be used to display the group for selection.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="groupedFolder",label="Group")
@SearchType(key="groupedFolder",label="Group")
public class Group extends AbstractDomainObject implements Node {

    @SearchTraversal({})
    private Reference proxyObject;

    @SearchTraversal({})
    private List<Reference> children = new ArrayList<>();

    public Reference getProxyObject() {
        return proxyObject;
    }

    public void setProxyObject(Reference proxyObject) {
        this.proxyObject = proxyObject;
    }
    
    @Override
    public List<Reference> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Reference> children) {
        if (children==null) throw new IllegalArgumentException("Property cannot be null");
        this.children = children;
    }

}
