package org.janelia.model.domain.tiledMicroscope;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

/**
 * this class manages text tags applied to a neuron in the LVV; it's
 * a many-to-many relationship (many tags on one neuron, many neurons
 * for each tag)
 */
public class TmNeuronTagMap {

    public static final ImmutableSet<String> PREDEFINED_TAGS = ImmutableSet.of(
            "auto", "hidden"
    );

    // here's our many-to-many maps; keep them synchronized, which
    //  means methods have to be synchronized

    private Map<Long,Set<String>> userTags = new HashMap<>();
    private SetMultimap<TmNeuronMetadata, String> tags = HashMultimap.create();
    private SetMultimap<String, TmNeuronMetadata> neurons = HashMultimap.create();
    private Map<String,Map<String,Object>> tagGroupMappings = new HashMap<>();
    private Boolean saveUserGroupState = false;

    public TmNeuronTagMap() {}

    public Set<String> getPredefinedTags() {
        return PREDEFINED_TAGS;
    }

    public Set<String> getTags(TmNeuronMetadata neuron) {
        return tags.get(neuron);
    }

    public Set<String> getAllTags() {
        return neurons.keySet();
    }

    public Set<TmNeuronMetadata> getNeurons(String tag) {
        return neurons.get(tag);
    }

    public Set<TmNeuronMetadata> getAllNeurons() {
        return tags.keySet();
    }

    public boolean hasTag(TmNeuronMetadata neuron, String tag) {
        return tags.get(neuron).contains(tag);
    }

    public synchronized void addTag(String tag, TmNeuronMetadata neuron) {
        tags.put(neuron, tag);
        neurons.put(tag, neuron);
    }

    public synchronized void addUserTag(String tag, TmNeuronMetadata neuron) {
        Set userNeuronSet = userTags.get(neuron.getId());
        if (userNeuronSet==null) {
            userNeuronSet = new HashSet<String>();
            userTags.put(neuron.getId(),userNeuronSet);
        }
        userNeuronSet.add(tag);
    }

    public synchronized void removeUserTag(String tag, TmNeuronMetadata neuron) {
        Set userNeuronTags = userTags.get(neuron.getId());
        if (userNeuronTags!=null) {
            userNeuronTags.remove(tag);
            if (userNeuronTags.isEmpty()) {
                userTags.remove(neuron.getId());
            } else {
                userTags.put(neuron.getId(), userNeuronTags);
            }
        }

    }

    public synchronized Map<Long, Set<String>> getUserTags() {
        return userTags;
    }

    public synchronized void setUserTags(Map<Long, Set<String>> map) {
        userTags = map;
    }

    public synchronized void setTagGroupMapping(String tag, Map<String,Object> meta) {
        tagGroupMappings.put(tag, meta);
    }

    public synchronized void saveTagGroupMappings(Map<String,Map<String,Object>> allTagGroupMappings) {
        tagGroupMappings = allTagGroupMappings;
    }

    public synchronized Map<String,Object> geTagGroupMapping(String tag) {
        return tagGroupMappings.get(tag);
    }

    public synchronized Map<String,Map<String,Object>> getAllTagGroupMappings() {
        return tagGroupMappings;
    }

    public synchronized void removeTag(String tag, TmNeuronMetadata neuron) {
        tags.remove(neuron, tag);
        neurons.remove(tag, neuron);
    }

    public synchronized void clearTags(TmNeuronMetadata neuron) {
        for (String tag: tags.get(neuron)) {
            neurons.remove(tag, neuron);
        }
        tags.removeAll(neuron);
    }

    public synchronized void clearAll() {
        tags.clear();
        neurons.clear();
    }

    public Boolean isSaveUserGroupState() {
        return saveUserGroupState;
    }

    public void setSaveUserGroupState(boolean saveUserGroupState) {
        this.saveUserGroupState = saveUserGroupState;
    }

}
