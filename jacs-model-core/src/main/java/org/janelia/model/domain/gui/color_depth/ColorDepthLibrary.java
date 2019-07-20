package org.janelia.model.domain.gui.color_depth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;
import org.janelia.model.domain.workspace.Node;

/**
 * A collection of color depth images for searching.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="colorDepthLibrary",label="Color Depth Library")
@SearchType(key="colorDepthLibrary",label="Color Depth Library")
public class ColorDepthLibrary extends AbstractDomainObject implements Node {

    @SearchAttribute(key="identifier_txt",label="Data Set Identifier")
    private String dataSetIdentifier;

    @SearchTraversal({})
    private List<Reference> children = new ArrayList<>();

    @SearchTraversal({})
    private Map<String,Integer> colorDepthCounts = new HashMap<>();

    public String getDataSetIdentifier() {
        return dataSetIdentifier;
    }

    public void setDataSetIdentifier(String dataSetIdentifier) {
        this.dataSetIdentifier = dataSetIdentifier;
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

    public Map<String, Integer> getColorDepthCounts() {
        return colorDepthCounts;
    }

    @Deprecated
    public void setColorDepthCounts(Map<String, Integer> colorDepthCounts) {
        if (colorDepthCounts==null) throw new IllegalArgumentException("Property cannot be null");
        this.colorDepthCounts = colorDepthCounts;
    }
}
