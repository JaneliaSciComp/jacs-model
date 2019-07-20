package org.janelia.model.domain.gui.color_depth;

import java.util.ArrayList;
import java.util.List;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;

/**
 * The result of running a ColorDepthSearch on the cluster. Each search mask has a list of ColorDepthResults associated
 * with it.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="colorDepthResult",label="Color Depth Result")
public class ColorDepthResult extends AbstractDomainObject {

    /** Properties of the search at the time that this search was run */
    private ColorDepthParameters parameters;

    private List<ColorDepthMaskResult> maskResults = new ArrayList<>();

    public ColorDepthResult() {
    }

    public ColorDepthResult(ColorDepthParameters parameters, List<ColorDepthMaskResult> maskResults) {
        this.parameters = parameters;
        this.maskResults = maskResults;
    }

    public ColorDepthParameters getParameters() {
        return parameters;
    }

    public void setParameters(ColorDepthParameters parameters) {
        if (parameters==null) throw new IllegalArgumentException("Property cannot be null");
        this.parameters = parameters;
    }

    public List<ColorDepthMaskResult> getMaskResults() {
        return maskResults;
    }

    public void setMaskResults(List<ColorDepthMaskResult> maskResults) {
        if (maskResults==null) throw new IllegalArgumentException("Property cannot be null");
        this.maskResults = maskResults;
    }

    public List<ColorDepthMatch> getMaskMatches(ColorDepthMask mask) {
        return getMaskMatches(Reference.createFor(mask));
    }

    public List<ColorDepthMatch> getMaskMatches(Reference maskRef) {

        List<ColorDepthMatch> maskMatches = new ArrayList<>();
        for (ColorDepthMaskResult maskResult : maskResults) {
            if (maskResult.getMaskRef().equals(maskRef)) {
                maskMatches.addAll(maskResult.getMatches());
            }
        }

        return maskMatches;
    }

}
