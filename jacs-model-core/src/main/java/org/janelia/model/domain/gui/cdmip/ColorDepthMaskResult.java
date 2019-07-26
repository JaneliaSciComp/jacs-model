package org.janelia.model.domain.gui.cdmip;

import java.util.ArrayList;
import java.util.List;

import org.janelia.model.domain.Reference;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthMaskResult {

    private Reference maskRef;

    private List<ColorDepthMatch> matches = new ArrayList<>();

    public Reference getMaskRef() {
        return maskRef;
    }

    public void setMaskRef(Reference maskRef) {
        this.maskRef = maskRef;
    }

    public List<ColorDepthMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<ColorDepthMatch> matches) {
        if (matches==null) throw new IllegalArgumentException("Property cannot be null");
        this.matches = matches;
    }

    public void addMatch(ColorDepthMatch match) {
        matches.add(match);
    }

}
