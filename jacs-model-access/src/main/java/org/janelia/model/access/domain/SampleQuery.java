package org.janelia.model.access.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SampleQuery {
    private final Set<Long> sampleIds = new HashSet<>();
    private final Set<String> sampleRefs = new HashSet<>();
    private final Set<String> datasetIds = new HashSet<>();
    private final Set<String> sampleNames = new HashSet<>();
    private final Set<String> sampleLines = new HashSet<>();
    private final Set<String> sampleSlideCodes = new HashSet<>();
    private final Set<String> sampleFlycoreIds = new HashSet<>();
    private final Set<Integer> sampleCrossBarcodes = new HashSet<>();
    private long offset;
    private int length;

    public boolean hasSampleIds() {
        return !sampleIds.isEmpty();
    }

    public Collection<Long> getSampleIds() {
        return Collections.unmodifiableSet(sampleIds);
    }

    public SampleQuery addSampleIds(Collection<Long> ids) {
        if (ids != null) {
            this.sampleIds.addAll(ids);
        }
        return this;
    }

    public boolean hasSampleRefs() {
        return !sampleRefs.isEmpty();
    }

    public Collection<String> getSampleRefs() {
        return Collections.unmodifiableSet(sampleRefs);
    }

    public SampleQuery addSampleRefs(Collection<String> refs) {
        if (refs != null) {
            this.sampleRefs.addAll(refs);
        }
        return this;
    }

    public boolean hasDatasetIds() {
        return !datasetIds.isEmpty();
    }

    public Collection<String> getDatasetIds() {
        return Collections.unmodifiableSet(datasetIds);
    }

    public SampleQuery addDatasetIds(Collection<String> ids) {
        if (ids != null) {
            this.datasetIds.addAll(datasetIds);
        }
        return this;
    }

    public boolean hasSampleNames() {
        return !sampleNames.isEmpty();
    }

    public Collection<String> getSampleNames() {
        return Collections.unmodifiableSet(sampleNames);
    }

    public SampleQuery addSampleNames(Collection<String> names) {
        if (names != null) {
            this.sampleNames.addAll(names);
        }
        return this;
    }

    public boolean hasSampleLines() {
        return !sampleLines.isEmpty();
    }

    public Collection<String> getSampleLines() {
        return Collections.unmodifiableSet(sampleLines);
    }

    public SampleQuery addSampleLines(Collection<String> lines) {
        if (lines != null) {
            this.sampleLines.addAll(lines);
        }
        return this;
    }

    public boolean hasSampleSlideCodes() {
        return !sampleSlideCodes.isEmpty();
    }

    public Collection<String> getSampleSlideCodes() {
        return Collections.unmodifiableSet(sampleSlideCodes);
    }

    public SampleQuery addSampleSlideCodes(Collection<String> slideCodes) {
        if (slideCodes != null) {
            this.sampleSlideCodes.addAll(slideCodes);
        }
        return this;
    }

    public boolean hasSampleFlycoreIds() {
        return !sampleFlycoreIds.isEmpty();
    }

    public Collection<String> getSampleFlycoreIds() {
        return Collections.unmodifiableSet(sampleFlycoreIds);
    }

    public SampleQuery addSampleFlycoreIds(Collection<String> flycoreIds) {
        if (flycoreIds != null) {
            this.sampleFlycoreIds.addAll(flycoreIds);
        }
        return this;
    }

    public boolean hasSampleCrossBarcodes() {
        return !sampleCrossBarcodes.isEmpty();
    }

    public Collection<Integer> getSampleCrossBarcodes() {
        return Collections.unmodifiableSet(sampleCrossBarcodes);
    }

    public SampleQuery addSampleCrossBarcodes(Collection<Integer> crossBarcodes) {
        if (crossBarcodes != null) {
            this.sampleCrossBarcodes.addAll(crossBarcodes);
        }
        return this;
    }

    public long getOffset() {
        return offset;
    }

    public SampleQuery setOffset(long offset) {
        this.offset = offset;
        return this;
    }

    public int getLength() {
        return length;
    }

    public SampleQuery setLength(int length) {
        this.length = length;
        return this;
    }
}
