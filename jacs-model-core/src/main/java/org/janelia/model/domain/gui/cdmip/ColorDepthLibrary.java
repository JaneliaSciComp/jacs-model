package org.janelia.model.domain.gui.cdmip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.gui.search.Filtering;
import org.janelia.model.domain.gui.search.criteria.AttributeValueCriteria;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

/**
 * A collection of color depth images for searching.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="cdmipLibrary",label="Color Depth Library")
@SearchType(key="cdmipLibrary",label="Color Depth Library")
public class ColorDepthLibrary extends AbstractDomainObject implements Filtering {

    @SearchAttribute(key="identifier_txt",label="Library Identifier")
    private String identifier;

    @SearchTraversal({})
    private Map<String,Integer> colorDepthCounts = new HashMap<>();

    @JsonIgnore
    private List<Criteria> lazyCriteria;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, Integer> getColorDepthCounts() {
        return colorDepthCounts;
    }

    @Deprecated
    public void setColorDepthCounts(Map<String, Integer> colorDepthCounts) {
        if (colorDepthCounts==null) throw new IllegalArgumentException("Property cannot be null");
        this.colorDepthCounts = colorDepthCounts;
    }

    /* implement Filtering interface */

    @JsonIgnore
    @Override
    public String getSearchClass() {
        return ColorDepthImage.class.getName();
    }

    @JsonIgnore
    @Override
    public boolean hasCriteria() {
        return true;
    }

    @JsonIgnore
    @Override
    public String getSearchString() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<Criteria> getCriteriaList() {
        if (lazyCriteria==null) {

            lazyCriteria = new ArrayList<>();

            // Search for images in this library
            AttributeValueCriteria libraryIdentifier = new AttributeValueCriteria();
            libraryIdentifier.setAttributeName("identifier");
            libraryIdentifier.setValue(getIdentifier());
            lazyCriteria.add(libraryIdentifier);

            if (!colorDepthCounts.isEmpty()) {
                Optional<String> first = colorDepthCounts.keySet().stream().sorted().findFirst();
                if (first.isPresent()) {
                    // Default to first available alignment space. User can change it.
                    String firstAlignmentSpace = first.get();
                    AttributeValueCriteria alignmentSpace = new AttributeValueCriteria();
                    alignmentSpace.setAttributeName("alignmentSpace");
                    alignmentSpace.setValue(firstAlignmentSpace);
                    lazyCriteria.add(alignmentSpace);
                }
            }

        }
        return lazyCriteria;
    }
}
