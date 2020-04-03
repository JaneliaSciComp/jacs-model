package org.janelia.model.domain.gui.cdmip;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.sample.Sample;

/**
 * Parse a standardized color depth MIP filename to extract various metadata.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthFileComponents {

    public static ColorDepthFileComponents fromFilepath(String filepath) {
        return new ColorDepthFileComponents(filepath);
    }

    private File file;
    private Reference sampleRef;
    private String alignmentSpace;
    private String sampleName;
    private String objective;
    private String anatomicalArea;
    private Integer channelNumber;

    private ColorDepthFileComponents(String filepath) {
        this.file = new File(filepath);

        Pattern p = Pattern.compile("^(?<sampleName>.*?)-(?<objective>\\d+x)-(?<anatomicalArea>\\w+?)-" +
                "(?<alignmentSpace>\\w+?)-(?<sampleId>\\d+)-CH(?<channelNum>\\d)_CDM\\.\\w+$");

        Matcher m = p.matcher(file.getName());
        if (m.matches()) {
            this.sampleName = m.group("sampleName");
            this.objective = m.group("objective");
            this.anatomicalArea = m.group("anatomicalArea");
            sampleRef = Reference.createFor(Sample.class, new Long(m.group("sampleId")));
            channelNumber = new Integer(m.group("channelNum"));
            alignmentSpace = m.group("alignmentSpace");
        }
    }

    public File getFile() {
        return file;
    }

    public Reference getSampleRef() {
        return sampleRef;
    }

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public String getSampleName() {
        return sampleName;
    }

    public String getObjective() {
        return objective;
    }

    public String getAnatomicalArea() {
        return anatomicalArea;
    }

    public Integer getChannelNumber() {
        return channelNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ColorDepthFileComponents that = (ColorDepthFileComponents) o;

        return new EqualsBuilder()
                .append(file, that.file)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(file)
                .toHashCode();
    }
}
