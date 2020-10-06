package org.janelia.model.domain.gui.cdmip;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

/**
 * A color depth search is batched so that searches use the Spark cluster efficiently. Therefore, each
 * search runs against several masks in the same alignment space.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="cdmipSearch",label="Color Depth Search")
@SearchType(key="cdmipSearch",label="Color Depth Search")
public class ColorDepthSearch extends AbstractDomainObject implements IsParent {

    @SearchAttribute(key="alignment_space_txt",label="Alignment Space",facet="alignment_space_s")
    private String alignmentSpace;

    private ColorDepthParameters parameters = new ColorDepthParameters();

    /** List of results, one for each run */
    private List<Reference> results = new ArrayList<>();

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    public ColorDepthParameters getParameters() {
        return parameters;
    }

    public void setParameters(ColorDepthParameters parameters) {
        if (parameters==null) throw new IllegalArgumentException("Property cannot be null");
        this.parameters = parameters;
    }

    public List<Reference> getResults() {
        return results;
    }

    public void setResults(List<Reference> results) {
        if (results==null) throw new IllegalArgumentException("Property cannot be null");
        this.results = results;
    }

    @JsonIgnore
    public List<String> getCDSTargets() {
        return parameters.getLibraries();
    }

    public void addCDSTarget(String cdsTarget) {
        parameters.addCDSTarget(cdsTarget);
    }

    public void removeCDSTarget(String cdsTarget) {
        parameters.removeCDSTarget(cdsTarget);
    }

    public void clearAllCDSTargets() {
        parameters.clearCDSTargets();
    }

    public boolean useSegmentation() {
        return parameters.getUseSegmentation() != null && parameters.getUseSegmentation();
    }

    public boolean useGradientScores() {
        return parameters.getUseGradientScores() != null && parameters.getUseGradientScores();
    }

    @JsonIgnore
    @SearchAttribute(key="threshold_i",label="Threshold for Data")
    public Integer getDataThreshold() {
        return parameters.getDataThreshold();
    }

    @JsonIgnore
    @SearchAttribute(key="pct_positive_d",label="% of Positive PX Threshold")
    public Double getPctPositivePixels() {
        return parameters.getPctPositivePixels();
    }

    @JsonIgnore
    @SearchAttribute(key="fluctuation_d",label="Pix Color Fluctuation")
    public Double getPixColorFluctuation() {
        return parameters.getPixColorFluctuation();
    }
    
    @JsonIgnore
    @SearchAttribute(key="xyshift_d",label="XY Shift")
    public Integer getXyShift() {
        return parameters.getXyShift();
    }

    @JsonIgnore
    @SearchAttribute(key="mirror_b",label="Mirror Mask?")
    public Boolean getMirrorMask() {
        return parameters.getMirrorMask();
    }

    @JsonIgnore
    public List<Reference> getMasks() {
        return parameters.getMasks();
    }

}
