package org.janelia.model.domain.gui.cdmip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.StringUtils;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.search.Filtering;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.gui.search.criteria.FacetCriteria;
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

    private String version;

    @SearchTraversal({})
    private Reference parentLibraryRef;

    @SearchTraversal({})
    private Map<String,Integer> colorDepthCounts = new HashMap<>();

    private List<Reference> sourceReleases;

    @JsonIgnore
    private List<Criteria> lazyCriteria;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean hasVersion() {
        return parentLibraryRef != null && StringUtils.isNotBlank(version);
    }

    public Reference getParentLibraryRef() {
        return parentLibraryRef;
    }

    public void setParentLibraryRef(Reference parentLibraryRef) {
        this.parentLibraryRef = parentLibraryRef;
    }

    public Map<String, Integer> getColorDepthCounts() {
        return colorDepthCounts;
    }

    public void setColorDepthCounts(Map<String, Integer> colorDepthCounts) {
        if (colorDepthCounts==null) throw new IllegalArgumentException("Property cannot be null");
        this.colorDepthCounts = colorDepthCounts;
    }

    public List<Reference> getSourceReleases() {
        return sourceReleases;
    }

    public void setSourceReleases(List<Reference> sourceReleases) {
        this.sourceReleases = sourceReleases;
    }

    public void addSourceRelease(Reference sourceRelease) {
        if (sourceRelease != null) {
            if (sourceReleases == null) {
                sourceReleases = new ArrayList<>();
            }
            sourceReleases.add(sourceRelease);
        }
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
            FacetCriteria libraryIdentifier = new FacetCriteria();
            libraryIdentifier.setAttributeName("libraries");
            libraryIdentifier.getValues().add(getIdentifier());
            lazyCriteria.add(libraryIdentifier);
        }
        return lazyCriteria;
    }
}
