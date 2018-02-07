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

    /** Properties of the search at the time that this search was run */
    private ColorDepthParameters parameters;

    /** All the returns of the search */
    private List<ColorDepthMatch> matches = new ArrayList<>();

    public ColorDepthResult() {
    }

    public ColorDepthResult(ColorDepthParameters parameters, List<ColorDepthMatch> matches) {
        this.parameters = parameters;
        this.matches = matches;
    }

    public ColorDepthParameters getParameters() {
        return parameters;
    }

    public void setParameters(ColorDepthParameters parameters) {
        if (parameters==null) throw new IllegalArgumentException("Property cannot be null");
        this.parameters = parameters;
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
        //return matches.stream().filter(match -> match.getProxyObject().equals(maskRef)).collect(Collectors.toList());

        List<ColorDepthMatch> maskMatches = new ArrayList<>();
        for (ColorDepthMatch match : matches) {
            if (match.getMaskRef().equals(maskRef)) {
                maskMatches.add(match);
            }
        }

        return maskMatches;
    }

}
