package org.janelia.it.jacs.model.domain.interfaces;

import java.util.List;
import java.util.Set;

import org.janelia.it.jacs.model.domain.sample.FileGroup;

/**
 * Has groups of files, keyed by string. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasFileGroups {

    public Set<String> getGroupKeys();
    
    public FileGroup getGroup(String key);
    
    public List<FileGroup> getGroups();
}
