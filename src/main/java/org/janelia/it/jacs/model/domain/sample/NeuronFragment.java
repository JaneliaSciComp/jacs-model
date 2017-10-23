package org.janelia.it.jacs.model.domain.sample;

import org.janelia.it.jacs.model.domain.AbstractDomainObject;
import org.janelia.it.jacs.model.domain.Reference;
import org.janelia.it.jacs.model.domain.enums.FileType;
import org.janelia.it.jacs.model.domain.interfaces.HasRelativeFiles;
import org.janelia.it.jacs.model.domain.support.MongoMapped;
import org.janelia.it.jacs.model.domain.support.SearchAttribute;
import org.janelia.it.jacs.model.domain.support.SearchTraversal;

import java.util.HashMap;
import java.util.Map;

/**
 * A neuron fragment segmented from an image by the Neuron Separator. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="fragment",label="Neuron Fragment")
//@SearchType(key="fragment",label="Neuron Fragment")
public class NeuronFragment extends AbstractDomainObject implements HasRelativeFiles {

    @SearchTraversal({NeuronFragment.class})
    private Reference sampleRef;
    
    private Long separationId;
        
    @SearchAttribute(key="number_i",label="Number")
    private Integer number;
    
    @SearchAttribute(key="filepath_txt",label="File Path")
    private String filepath;

    @SearchAttribute(key="voxel_weight_i",label="Voxel Weight")
    private Integer voxelWeight;
    
    private Map<FileType, String> files = new HashMap<>();
    
    public Integer getNumber() {
        return number;
    }

    public Reference getSample() {
		return sampleRef;
	}

	public void setSample(Reference sample) {
		this.sampleRef = sample;
	}

	public Long getSeparationId() {
        return separationId;
    }

    public void setSeparationId(Long separationId) {
        this.separationId = separationId;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }

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

    public Integer getVoxelWeight() {
        return voxelWeight;
    }

    public void setVoxelWeight(Integer voxelWeight) {
        this.voxelWeight = voxelWeight;
    }

}
