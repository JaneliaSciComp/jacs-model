package org.janelia.it.jacs.model.domain.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import org.janelia.it.jacs.model.util.ModelStringUtil;

/**
 * A set of LSMs in a Sample with a common objective. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ObjectiveSample implements Serializable {

    private String objective;
    private String chanSpec;
    private List<SampleTile> tiles = new ArrayList<>();
    private List<SamplePipelineRun> pipelineRuns = new ArrayList<>();
    
    private transient Sample parent;

    public ObjectiveSample() {
    }
    
    public ObjectiveSample(String objective) {
        this.objective = objective;
    }

    @JsonIgnore
    public Sample getParent() {
        return parent;
    }

    @JsonIgnore
    void setParent(Sample parent) {
        this.parent = parent;
    }
    
    public String getObjective() {
        return objective;
    }
    
    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getChanSpec() {
        return chanSpec;
    }

    public void setChanSpec(String chanSpec) {
        this.chanSpec = chanSpec;
    }

    @JsonIgnore
    public boolean hasPipelineRuns() {
        return pipelineRuns!=null && !pipelineRuns.isEmpty();
    }

    public List<SamplePipelineRun> getPipelineRuns() {
        for(SamplePipelineRun pipelineRun : pipelineRuns) {
            pipelineRun.setParent(this);
        }
        return pipelineRuns==null?null:Collections.unmodifiableList(pipelineRuns);
    }

    public void setPipelineRuns(List<SamplePipelineRun> pipelineRuns) {
        if (pipelineRuns==null) throw new IllegalArgumentException("Property cannot be null");
        this.pipelineRuns = pipelineRuns;
    }
    
    @JsonIgnore
    public void addRun(SamplePipelineRun pipelineRun) {
        pipelineRun.setParent(this);
        pipelineRuns.add(pipelineRun);
    }

    @JsonIgnore
    public void removeRun(SamplePipelineRun pipelineRun) {
        pipelineRun.setParent(null);
        pipelineRuns.remove(pipelineRun);
    }

    @JsonIgnore
    public SamplePipelineRun getLatestRun() {
        if (pipelineRuns.isEmpty()) {
            return null;
        }
        return getPipelineRuns().get(pipelineRuns.size() - 1);
    }

    @JsonIgnore
    public SamplePipelineRun getLatestSuccessfulRun() {
        if (pipelineRuns.isEmpty()) {
            return null;
        }
        for(SamplePipelineRun run : Lists.reverse(getPipelineRuns())) {
            if (!run.hasError()) return run;
        }
        return null;
    }

    @JsonIgnore
    public<T extends PipelineResult> List<T> getLatestResultsOfType(Class<T> resultClass) {
        List<T> results = new ArrayList<>();
        for(SamplePipelineRun run : Lists.reverse(getPipelineRuns())) {
            for(T result : Lists.reverse(run.getResultsOfType(resultClass))) {
            	results.add(result);
            }
        }
        return results;
    }

    @JsonIgnore
    public <T extends PipelineResult> T getLatestResultOfType(Class<T> resultClass) {
        return getLatestResultOfType(resultClass, null);
    }

    @JsonIgnore
    public <T extends PipelineResult> T getLatestResultOfType(Class<T> resultClass, String resultName) {
        for(SamplePipelineRun run : Lists.reverse(getPipelineRuns())) {
            for(T result : Lists.reverse(run.getResultsOfType(resultClass))) {
                if (resultName==null || result.getName().equals(resultName)) {
                    return result;
                }
            }
        }
        return null;
    }
    
    @JsonIgnore
    public SamplePipelineRun getRunById(Long pipelineRunId) {
        for(SamplePipelineRun run : pipelineRuns) {
            if (run.getId().equals(pipelineRunId)) {
                return run;
            }
        }
        return null;
    }

    public <T extends PipelineResult> List<T> getResultsById(Class<T> resultClass, Long resultEntityId) {
        List<T> results = new ArrayList<>();
        for(SamplePipelineRun run : getPipelineRuns()) {
            for(T result : run.getResultsById(resultClass, resultEntityId)) {
                results.add(result);
            }
        }
        return results;
    }

    public List<SampleTile> getTiles() {
        for(SampleTile tile : tiles) {
            tile.setParent(this);
        }
        return Collections.unmodifiableList(tiles);
    }

    public void setTiles(List<SampleTile> tiles) {
        if (tiles==null) throw new IllegalArgumentException("Property cannot be null");
        this.tiles = tiles;
    }
    
    @JsonIgnore
    public void addTile(SampleTile tile) {
        tile.setParent(this);
        tiles.add(tile);
    }

    @JsonIgnore
    public void removeTile(SampleTile tile) {
        tile.setParent(null);
        tiles.remove(tile);
    }

    @JsonIgnore
    public SampleTile getTileByNameAndArea(String name, String area) {
        for(SampleTile tile : tiles) {
            if (ModelStringUtil.areEqual(tile.getName(),name) && ModelStringUtil.areEqual(tile.getAnatomicalArea(),area)) {
                return tile;
            }
        }
        return null;
    }

    @JsonIgnore
    public String getName() {
        return getParent().getName()+"~"+getObjective();
    }
}
