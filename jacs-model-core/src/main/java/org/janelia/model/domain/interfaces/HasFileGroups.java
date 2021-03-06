package org.janelia.model.domain.interfaces;

import java.util.List;
import java.util.Set;

import org.janelia.model.domain.sample.FileGroup;

/**
 * Has groups of files, keyed by string. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasFileGroups {
    Set<String> getGroupKeys();
    
    FileGroup getGroup(String key);
    
    List<FileGroup> getGroups();
}
