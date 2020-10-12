package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;

/**
 * Query for color depth mips.
 */
public class ColorDepthImageQuery {
    private String owner;
    private String alignmentSpace;
    private Collection<String> libraryIdentifiers;
    private Collection<String> exactNames;
    private Collection<String> fuzzyNames;
    private Collection<String> exactFilepaths;
    private Collection<String> fuzzyFilepaths;
    private Collection<String> sampleRefs;
    private int offset;
    private int length = -1;

    public String getOwner() {
        return owner;
    }

    public ColorDepthImageQuery withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public ColorDepthImageQuery withAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
        return this;
    }

    public Collection<String> getLibraryIdentifiers() {
        return libraryIdentifiers;
    }

    public ColorDepthImageQuery withLibraryIdentifiers(Collection<String> libraryIdentifiers) {
        this.libraryIdentifiers = libraryIdentifiers;
        return this;
    }

    public Collection<String> getExactNames() {
        return exactNames;
    }

    public ColorDepthImageQuery withExactNames(Collection<String> exactNames) {
        this.exactNames = exactNames;
        return this;
    }

    public Collection<String> getFuzzyNames() {
        return fuzzyNames;
    }

    public ColorDepthImageQuery withFuzzyNames(Collection<String> fuzzyNames) {
        this.fuzzyNames = fuzzyNames;
        return this;
    }

    public Collection<String> getExactFilepaths() {
        return exactFilepaths;
    }

    public ColorDepthImageQuery withExactFilepaths(Collection<String> exactFilepaths) {
        this.exactFilepaths = exactFilepaths;
        return this;
    }

    public Collection<String> getFuzzyFilepaths() {
        return fuzzyFilepaths;
    }

    public ColorDepthImageQuery withFuzzyFilepaths(Collection<String> fuzzyFilepaths) {
        this.fuzzyFilepaths = fuzzyFilepaths;
        return this;
    }

    public Collection<String> getSampleRefs() {
        return sampleRefs;
    }

    public ColorDepthImageQuery withSampleRefs(Collection<String> sampleRefs) {
        this.sampleRefs = sampleRefs;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public ColorDepthImageQuery withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public int getLength() {
        return length;
    }

    public ColorDepthImageQuery withLength(int length) {
        this.length = length;
        return this;
    }
}