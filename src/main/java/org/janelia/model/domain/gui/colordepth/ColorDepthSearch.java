package org.janelia.model.domain.gui.colordepth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * A color depth search is batched so that searches use the Spark cluster efficiently. Therefore, each
 * search runs against several masks in the same alignment space.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="colorDepthSearch",label="Color Depth Search")
@SearchType(key="colorDepthSearch",label="Color Depth Search")
public class ColorDepthSearch extends AbstractDomainObject implements IsParent {

    @SearchAttribute(key="alignment_space_txt",label="Alignment Space",facet="alignment_space_s")
    private String alignmentSpace;

    @SearchAttribute(key="complete_b",label="Search Complete",facet="complete_b")
    private Boolean complete = false;

    @SearchTraversal({})
    private List<ColorDepthMask> masks = new ArrayList<>();

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    public Boolean isComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    @JsonIgnore
    public boolean isSearchComplete() {
        return complete!=null && complete;
    }

    public List<ColorDepthMask> getMasks() {
        return masks;
    }

    public void setMasks(List<ColorDepthMask> masks) {
        if (masks==null) throw new IllegalArgumentException("Property cannot be null");
        this.masks = masks;
    }

    public void addMask(ColorDepthMask mask) {
        masks.add(mask);
    }

}
