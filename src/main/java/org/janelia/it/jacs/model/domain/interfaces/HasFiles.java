package org.janelia.it.jacs.model.domain.interfaces;

import java.util.Map;

import org.janelia.it.jacs.model.domain.enums.FileType;

/**
 * Any object implementing this interface has the option of associated files of specific types. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasFiles {

    public abstract Map<FileType, String> getFiles();

}
