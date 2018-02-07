package org.janelia.model.domain.workspace;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * A type of node where the results are all Groups.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="groupedFolder",label="Grouped Folder")
@SearchType(key="groupedFolder",label="Grouped Folder")
public class GroupedFolder extends AbstractDomainObject implements Node {

    @SearchTraversal({})
    private List<Reference> children = new ArrayList<>();

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
