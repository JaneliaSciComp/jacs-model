package org.janelia.model.domain.gui.cdmip;

import java.util.ArrayList;
import java.util.List;

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

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
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

}