package org.janelia.it.jacs.model.domain.interfaces;

/**
 * Any object implementing this interface has a pair of "mask/chan" files, 
 * for rapid loading into a 3d space. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasMaskChan {

    public abstract String getMaskFilepath();

    public abstract String getChanFilepath();

}
