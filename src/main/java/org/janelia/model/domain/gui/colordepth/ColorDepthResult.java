package org.janelia.model.domain.gui.colordepth;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of running a ColorDepthSearch on the cluster. Each search mask has a list of ColorDepthResults associated
 * with it.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="colorDepthResult",label="Color Depth Result")
public class ColorDepthResult extends AbstractDomainObject {

    private List<ColorDepthMatch> matches = new ArrayList<>();

    public ColorDepthResult() {
    }

    public ColorDepthResult(List<ColorDepthMatch> matches) {
        this.matches = matches;
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

    public List<ColorDepthMatch> getMaskMatches(ColorDepthMask mask) {
        return getMaskMatches(Reference.createFor(mask));
    }

    public List<ColorDepthMatch> getMaskMatches(Reference maskRef) {

        // Can't use java 8 syntax in JACSv1 :(
        //return matches.stream().filter(match -> match.getMaskRef().equals(maskRef)).collect(Collectors.toList());

        List<ColorDepthMatch> matches = new ArrayList<>();
        for (ColorDepthMatch match : matches) {
            if (match.getMaskRef().equals(maskRef)) {
                matches.add(match);
            }
        }

        return matches;
    }

}
