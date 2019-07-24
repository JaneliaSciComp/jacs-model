package org.janelia.model.domain.sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasName;
import org.janelia.model.domain.interfaces.HasRelativeFiles;

/**
 * A group of files with a common parent path. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class FileGroup implements HasName, HasRelativeFiles, Serializable {

    private String key;
    private String filepath;
	private Map<FileType, String> files = new HashMap<>();
    
	public FileGroup() {
	}
	
	public FileGroup(String key) {
	    this.key = key;
	}

    /**
     * The name is simply equal to the key.
     * @return
     */
    @JsonIgnore
    @Override
    public String getName() {
        return key;
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
