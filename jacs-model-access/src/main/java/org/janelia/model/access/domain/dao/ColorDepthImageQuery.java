package org.janelia.model.access.domain.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;

/**
 * Query for color depth mips.
 */
public class ColorDepthImageQuery {
    private String owner;
    private String alignmentSpace;
    private Collection<String> libraryIdentifiers;
    private Collection<Long> ids;
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

    public Collection<Long> getIds() {
        return ids;
    }

    public ColorDepthImageQuery withIds(Collection<Long> ids) {
        this.ids = ids;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ColorDepthImageQuery that = (ColorDepthImageQuery) o;

        return new EqualsBuilder()
                .append(offset, that.offset)
                .append(length, that.length)
                .append(owner, that.owner)
                .append(alignmentSpace, that.alignmentSpace)
                .append(libraryIdentifiers, that.libraryIdentifiers)
                .append(exactNames, that.exactNames)
                .append(fuzzyNames, that.fuzzyNames)
                .append(exactFilepaths, that.exactFilepaths)
                .append(fuzzyFilepaths, that.fuzzyFilepaths)
                .append(sampleRefs, that.sampleRefs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(owner)
                .append(alignmentSpace)
                .append(libraryIdentifiers)
                .append(exactNames)
                .append(fuzzyNames)
                .append(exactFilepaths)
                .append(fuzzyFilepaths)
                .append(sampleRefs)
                .append(offset)
                .append(length)
                .toHashCode();
    }
}
