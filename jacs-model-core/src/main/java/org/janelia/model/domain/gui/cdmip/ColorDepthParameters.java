package org.janelia.model.domain.gui.cdmip;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.janelia.model.domain.Reference;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthParameters {

    private List<String> libraries = new ArrayList<>();

    private List<Reference> masks = new ArrayList<>();

    /** Background threshold for data (0-255) */
    private Integer dataThreshold;

    /** % of Positive PX Threshold (0-100%) */
    private Double pctPositivePixels;

    /** Pix Color Fluctuation, 1.18 per slice */
    private Double pixColorFluctuation;

    /** Number of pixels to try shifting in XY plane */
    private Integer xyShift;

    /** Should the mask be mirrored across the Y axis? */
    private Boolean mirrorMask;

    private Integer negativeRadius;

    /** Maxmimum number of results to persist per mask */
    private Integer maxResultsPerMask;

    private Boolean useSegmentation;

    private Boolean useGradientScores;

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public void addCDSTarget(String cdsTarget) {
        if (StringUtils.isNotBlank(cdsTarget)) {
            if (this.libraries == null) {
                this.libraries = new ArrayList<>();
            }
            this.libraries.add(cdsTarget);
        }
    }

    public void removeCDSTarget(String cdsTarget) {
        libraries.remove(cdsTarget);
    }

    public void clearCDSTargets() {
        libraries.clear();
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

    public Integer getXyShift() {
        return xyShift;
    }

    public void setXyShift(Integer xyShift) {
        this.xyShift = xyShift;
    }

    public Boolean getMirrorMask() {
        return mirrorMask;
    }

    public void setMirrorMask(Boolean mirrorMask) {
        this.mirrorMask = mirrorMask;
    }

    public Integer getNegativeRadius() {
        return negativeRadius;
    }

    public void setNegativeRadius(Integer negativeRadius) {
        this.negativeRadius = negativeRadius;
    }

    public Integer getMaxResultsPerMask() {
        return maxResultsPerMask;
    }

    public void setMaxResultsPerMask(Integer maxResultsPerMask) {
        this.maxResultsPerMask = maxResultsPerMask;
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

    public Boolean getUseSegmentation() {
        return useSegmentation;
    }

    public void setUseSegmentation(Boolean useSegmentation) {
        this.useSegmentation = useSegmentation;
    }

    public Boolean getUseGradientScores() {
        return useGradientScores;
    }

    public void setUseGradientScores(Boolean useGradientScores) {
        this.useGradientScores = useGradientScores;
    }
}
