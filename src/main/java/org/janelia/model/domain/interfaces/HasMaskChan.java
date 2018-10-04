package org.janelia.model.domain.interfaces;

/**
 * Any object implementing this interface has a pair of "mask/chan" files, 
 * for rapid loading into a 3d space. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasMaskChan {

    String getMaskFilepath();

    String getChanFilepath();

}
