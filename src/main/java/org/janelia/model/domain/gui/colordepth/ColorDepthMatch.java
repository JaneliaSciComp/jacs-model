package org.janelia.model.domain.gui.colordepth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.interfaces.HasFiles;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A color depth search is batched so that searches use the Spark cluster efficiently. Therefore, each
 * search runs against several masks in the same alignment space.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthMatch implements HasFilepath, HasFiles {

    private String filepath;
    private Reference sampleRef;
    private Integer score;
    private Double scorePercent;

    @JsonIgnore
    // This is calculated from filepath and cached, but never persisted
    private Integer channelNumber;

    @Override
    public String getFilepath() {
        return filepath;
    }

    @Override
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Reference getSample() {
        return sampleRef;
    }

    public void setSample(Reference sample) {
        this.sampleRef = sample;
    }

    @JsonIgnore
    public Integer getChannelNumber() {
        if (channelNumber==null) {
            Pattern p = Pattern.compile(".*?-CH(?<channelNum>\\d)_CDM\\.\\w+$");
            Matcher m = p.matcher(filepath);
            if (m.matches()) {
                channelNumber = new Integer(m.group("channelNum"));
            }
            else {
                throw new IllegalStateException("Color depth projection file has no channel number: "+filepath);
            }
        }
        return channelNumber;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer scoreAbs) {
        this.score = scoreAbs;
    }

    public Double getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Double scorePercent) {
        this.scorePercent = scorePercent;
    }

    @JsonIgnore
    public Map<FileType, String> getFiles() {
        return ImmutableMap.of(FileType.Unclassified2d, filepath);
    }
}
