package org.janelia.model.domain.tiledMicroscope;

public enum AnnotationNavigationDirection {
    // easy: toward or away from the root of the neuron
    ROOTWARD_JUMP,
    ROOTWARD_STEP,
    ENDWARD_JUMP,
    ENDWARD_STEP,
    // assuming the children of each branch are in some stable
    //  order, next/prev implies movement between sibling
    //  branches of the nearest rootward branch point
    NEXT_PARALLEL,
    PREV_PARALLEL,
}