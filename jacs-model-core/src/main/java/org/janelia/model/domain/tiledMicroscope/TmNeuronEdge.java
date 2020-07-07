package org.janelia.model.domain.tiledMicroscope;

public class TmNeuronEdge {
    private final TmGeoAnnotation child;
    private final TmGeoAnnotation parent;

    TmNeuronEdge(TmGeoAnnotation parent, TmGeoAnnotation child)
    {
        this.parent = parent;
        this.child = child;
    }

    public TmGeoAnnotation getParentVertex() {
        return parent;
    }

    public TmGeoAnnotation getChildVertex() {
        return child;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TmNeuronEdge other = (TmNeuronEdge) obj;
        if (parent!=other.parent && child!=other.child) {
            return false;
        }
        return true;
    }
}
