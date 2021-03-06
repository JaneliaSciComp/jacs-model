package org.janelia.model.domain.interfaces;

import java.util.Map;

import org.janelia.model.domain.enums.FileType;

/**
 * Any object implementing this interface has the option of associated files of specific types. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasFiles {
    Map<FileType, String> getFiles();
}
