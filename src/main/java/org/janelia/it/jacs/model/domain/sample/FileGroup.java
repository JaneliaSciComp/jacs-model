package org.janelia.it.jacs.model.domain.sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.janelia.it.jacs.model.domain.enums.FileType;
import org.janelia.it.jacs.model.domain.interfaces.HasRelativeFiles;

/**
 * A group of files with a common parent path. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class FileGroup implements HasRelativeFiles, Serializable {

    private String key;
    private String filepath;
	private Map<FileType, String> files = new HashMap<>();
    
	public FileGroup() {
	}
	
	public FileGroup(String key) {
	    this.key = key;
	}
	
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getFilepath() {
        return filepath;
    }
    
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }
}
