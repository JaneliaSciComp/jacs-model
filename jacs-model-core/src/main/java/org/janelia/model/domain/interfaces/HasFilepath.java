package org.janelia.model.domain.interfaces;

/**
 * Any object implementing this interface has an associated file or directory on disk.  
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasFilepath {
    String getFilepath();

    void setFilepath(String filepath);
}
