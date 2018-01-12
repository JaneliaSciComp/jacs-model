package org.janelia.model.domain.gui.colordepth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.sample.Sample;

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

    private Reference maskRef;
    private String filepath;
    private Integer score;
    private Double scorePercent;

    @JsonIgnore
    // This is calculated from filepath and cached, but never persisted
    private transient Reference sampleRef;

    @JsonIgnore
    // This is calculated from filepath and cached, but never persisted
    private transient Integer channelNumber;

    public Reference getMaskRef() {
        return maskRef;
    }

    public void setMaskRef(Reference maskRef) {
        this.maskRef = maskRef;
    }

    @Override
    public String getFilepath() {
        return filepath;
    }

    @Override
    public void setFilepath(String filepath) {
        this.filepath = filepath;
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

    @JsonIgnore
    public Reference getSample() {
        if (sampleRef==null) parse();
        return sampleRef;
    }

    @JsonIgnore
    public Integer getChannelNumber() {
        if (channelNumber==null) parse();
        return channelNumber;
    }

    private void parse() {
        Pattern p = Pattern.compile(".*?-(?<sampleId>\\d+)-CH(?<channelNum>\\d)_CDM\\.\\w+$");
        Matcher m = p.matcher(filepath);
        if (m.matches()) {
            sampleRef = Reference.createFor(Sample.class, new Long(m.group("sampleId")));
            channelNumber = new Integer(m.group("channelNum"));
        }
        else {
            throw new IllegalStateException("Misnamed color depth file: "+filepath);
        }
    }
}
