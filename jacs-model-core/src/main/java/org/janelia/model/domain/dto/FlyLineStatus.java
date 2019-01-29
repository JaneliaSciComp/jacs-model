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
    private int numSamples = 0;
    @JsonIgnore
    private int num20xLsms = 0;
    @JsonIgnore
    private int num63xLsms = 0;
    @JsonIgnore
    private Set<String> releaseIds = new LinkedHashSet<>();


    @JsonProperty
    public int getNumSamples() {
        return numSamples;
    }
    public void addSamples(int numSamples) {
        this.numSamples += numSamples;
    }
    @JsonProperty
    public int getNum20xLsms(){return num20xLsms;}
    public void add20xLsms(int num20xLsms) {this.num20xLsms += num20xLsms; }
    @JsonProperty
    public int getNum63xLsms(){return num63xLsms;}
    public void add63xLsms(int num63xLsms) {this.num63xLsms += num63xLsms; }
    @JsonProperty
    public Set<String> getReleaseIds() {
        return releaseIds;
    }
    
}