package org.janelia.model.domain.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class FlyLineStatus {
    
    @JsonIgnore
    private int numRepresentatives = 0;
    @JsonIgnore
    private int numSamples = 0;
    @JsonIgnore
    private Set<String> releaseIds = new LinkedHashSet<>();

    @JsonProperty
    public int getNumRepresentatives() {
        return numRepresentatives;
    }
    public void addRepresentatives(int numRepresentatives) {
        this.numRepresentatives += numRepresentatives;
    }
    @JsonProperty
    public int getNumSamples() {
        return numSamples;
    }
    public void addSamples(int numSamples) {
        this.numSamples += numSamples;
    }
    @JsonProperty
    public Set<String> getReleaseIds() {
        return releaseIds;
    }
    
}