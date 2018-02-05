package org.janelia.model.domain.gui.colordepth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.sample.Sample;

import java.io.File;
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

    // These fields are calculated from filepath and cached, but never persisted

    @JsonIgnore
    private transient String dataSet;

    @JsonIgnore
    private transient Reference sampleRef;

    @JsonIgnore
    private transient Integer channelNumber;

    @JsonIgnore
    private transient File file;

    @JsonIgnore
    private transient boolean parsed;


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
        this.parsed = false; // We need to reparse the filepath since it changed
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
    public String getDataSet() {
        parse();
        return dataSet;
    }

    @JsonIgnore
    public Reference getSample() {
        parse();
        return sampleRef;
    }

    @JsonIgnore
    public Integer getChannelNumber() {
        parse();
        return channelNumber;
    }

    @JsonIgnore
    public File getFile() {
        return file;
    }

    private void parse() {

        if (parsed) return;

        this.file = new File(filepath);
        this.dataSet = file.getParentFile().getName();

        Pattern p = Pattern.compile(".*?-(?<sampleId>\\d+)-CH(?<channelNum>\\d)_CDM\\.\\w+$");
        Matcher m = p.matcher(filepath);
        if (m.matches()) {
            sampleRef = Reference.createFor(Sample.class, new Long(m.group("sampleId")));
            channelNumber = new Integer(m.group("channelNum"));
        }
        else {
            sampleRef = null;
            channelNumber = null;
        }

        this.parsed = true;
    }
}
