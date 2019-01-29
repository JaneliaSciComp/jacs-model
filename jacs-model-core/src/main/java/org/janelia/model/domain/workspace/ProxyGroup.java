package org.janelia.model.domain.workspace;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

/**
 * A group of items in a GroupedFolder. The children are always ProxyGroup objects.
 *
 * The proxy object is an optional object which represents the group. If not null,
 * its name and image will be used to display the group for selection.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="groupedFolder",label="Group")
public class ProxyGroup extends GroupedFolder {

    @SearchTraversal({})
    private Reference proxyObject;

    public Reference getProxyObject() {
        return proxyObject;
    }

    public void setProxyObject(Reference proxyObject) {
        this.proxyObject = proxyObject;
    }
}
