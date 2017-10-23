package org.janelia.it.jacs.model.domain.sample;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.janelia.it.jacs.model.domain.interfaces.HasFileGroups;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Summary files for all of the LSMs in an ObjectiveSample.
 * Generally this consists of MIPs and movies.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */

public class LSMSummaryResult extends PipelineResult implements HasFileGroups {

    private List<FileGroup> groups = new ArrayList<>();

    @Override
    @JsonIgnore
    public Set<String> getGroupKeys() {
        Set<String> groupKeys = new LinkedHashSet<>();
        for(FileGroup fileGroup : groups) {
            groupKeys.add(fileGroup.getKey());
        }
        return groupKeys;
    }

    @Override
    @JsonIgnore
    public FileGroup getGroup(String key) {
        for(FileGroup fileGroup : groups) {
            if (fileGroup.getKey().equals(key)) {
                return fileGroup;
            }
        }
        return null;
    }

    @JsonIgnore
    public void addGroup(FileGroup group) {
        if (getGroupKeys().contains(group.getKey())) {
            throw new IllegalArgumentException("Duplicate group key: "+group.getKey());
        }
        groups.add(group);
    }

    public List<FileGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<FileGroup> groups) {
        if (groups==null) throw new IllegalArgumentException("Property cannot be null");
        this.groups = groups;
    }


}
