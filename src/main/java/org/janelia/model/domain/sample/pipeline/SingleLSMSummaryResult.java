package org.janelia.model.domain.sample.pipeline;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.interfaces.HasFiles;

import java.util.HashMap;
import java.util.Map;

/**
 * Summary for a single LSM.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class SingleLSMSummaryResult implements HasFilepath, HasFiles {

    private Long lsmId;
    private String lsmName;
    private String filepath;
    private Map<FileType, String> files = new HashMap<>();
    private String brightnessCompensation;

    public Long getLsmId() {
        return lsmId;
    }

    public void setLsmId(Long lsmId) {
        this.lsmId = lsmId;
    }

    public String getLsmName() {
        return lsmName;
    }

    public void setLsmName(String lsmName) {
        this.lsmName = lsmName;
    }

    @Override
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Use DomainUtils.getFilepath instead of this method, to get the absolute path. This method is only here to support serialization.
     */
    @Override
    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files == null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }

    public String getBrightnessCompensation() {
        return brightnessCompensation;
    }

    public void setBrightnessCompensation(String brightnessCompensation) {
        this.brightnessCompensation = brightnessCompensation;
    }
}