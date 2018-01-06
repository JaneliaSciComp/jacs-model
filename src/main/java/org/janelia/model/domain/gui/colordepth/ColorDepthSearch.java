package org.janelia.model.domain.gui.colordepth;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
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

    private List<String> dataSets = new ArrayList<>();

    private List<Reference> masks = new ArrayList<>();

    /** Background threshold for data (0-255) */
    @SearchAttribute(key="threshold_i",label="Threshold for Data")
    private Integer dataThreshold;

    /** % of Positive PX Threshold (0-100%) */
    @SearchAttribute(key="pct_positive_d",label="% of Positive PX Threshold")
    private Double pctPositivePixels;

    /** Pix Color Fluctuation, 1.18 per slice */
    @SearchAttribute(key="fluctuation_d",label="Pix Color Fluctuation")
    private Double pixColorFluctuation;

    /** List of results, one for each run */
    private List<Reference> results = new ArrayList<>();

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    public List<String> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<String> dataSets) {
        this.dataSets = dataSets;
    }

    public Integer getDataThreshold() {
        return dataThreshold;
    }

    public void setDataThreshold(Integer dataThreshold) {
        this.dataThreshold = dataThreshold;
    }

    public Double getPctPositivePixels() {
        return pctPositivePixels;
    }

    public void setPctPositivePixels(Double pctPositivePixels) {
        this.pctPositivePixels = pctPositivePixels;
    }

    public Double getPixColorFluctuation() {
        return pixColorFluctuation;
    }

    public void setPixColorFluctuation(Double pixColorFluctuation) {
        this.pixColorFluctuation = pixColorFluctuation;
    }

    public List<Reference> getMasks() {
        return masks;
    }

    public void setMasks(List<Reference> masks) {
        if (masks==null) throw new IllegalArgumentException("Property cannot be null");
        this.masks = masks;
    }

    public void addMask(Reference mask) {
        masks.add(mask);
    }

    public List<Reference> getResults() {
        return results;
    }

    public void setResults(List<Reference> results) {
        if (results==null) throw new IllegalArgumentException("Property cannot be null");
        this.results = results;
    }
}
