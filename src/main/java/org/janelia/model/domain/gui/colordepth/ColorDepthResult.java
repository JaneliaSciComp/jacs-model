package org.janelia.model.domain.gui.colordepth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.interfaces.HasFilepath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A color depth search is batched so that searches use the Spark cluster efficiently. Therefore, each
 * search runs against several masks in the same alignment space.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthResult implements HasFilepath {

    private String filepath;
    private Reference sampleRef;
    private Double matchScore;

    @JsonIgnore
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

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }
}
