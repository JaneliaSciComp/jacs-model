package org.janelia.it.jacs.model.domain.interfaces;

/**
 * Any object implementing this interface has a globally unique identifier (GUID) 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasIdentifier {
    
    public Long getId();
}
