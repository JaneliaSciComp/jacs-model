package org.janelia.model.domain.gui.cdmip;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
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

    public static String createCDMNameFromNameComponents(String sampleName,
                                                         String objective,
                                                         String anatomicalArea,
                                                         String alignmentSpace,
                                                         Reference sampleRef,
                                                         Integer channelNumber,
                                                         String versionSuffix) {
        StringBuilder cdmNameBuilder = new StringBuilder()
                .append(sampleName).append('-')
                .append(objective).append('-')
                .append(anatomicalArea).append('-')
                .append(alignmentSpace).append('-')
                .append(sampleRef.getTargetId()).append('-')
                .append("CH").append(channelNumber);
        if (StringUtils.isNotBlank(versionSuffix)) {
            cdmNameBuilder.append('-').append(versionSuffix).append("_CDM");
        }
        return cdmNameBuilder.toString();
    }

    private final File file;
    private final Reference sampleRef;
    private final String alignmentSpace;
    private final String sampleName;
    private final String objective;
    private final String anatomicalArea;
    private final Integer channelNumber;
    private final String versionSuffix;
    private final boolean componentsFound;

    private ColorDepthFileComponents(String filepath) {
        this.file = new File(filepath);

        Pattern p = Pattern.compile("^(?<sampleName>.*?)-(?<objective>\\d+x)-(?<anatomicalArea>\\w+?)-" +
                "(?<alignmentSpace>\\w+?)-(?<sampleId>\\d+)-CH(?<channelNum>\\d)(-(?<versionSuffix>.+?))?_CDM\\.\\w+$");

        Matcher m = p.matcher(file.getName());
        if (m.matches()) {
            this.sampleName = m.group("sampleName");
            this.objective = m.group("objective");
            this.anatomicalArea = m.group("anatomicalArea");
            sampleRef = Reference.createFor(Sample.class, new Long(m.group("sampleId")));
            channelNumber = new Integer(m.group("channelNum"));
            alignmentSpace = m.group("alignmentSpace");
            versionSuffix = m.group("versionSuffix");
            componentsFound = true;
        } else {
            this.sampleName = null;
            this.objective = null;
            this.anatomicalArea = null;
            sampleRef = null;
            channelNumber = -1;
            alignmentSpace = null;
            versionSuffix = null;
            componentsFound = false;
        }
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file != null
                ? RegExUtils.replacePattern(file.getName(), "\\.\\D*$", "")
                : null;
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

    public String getVersionSuffix() {
        return versionSuffix;
    }

    public boolean hasNameComponents() {
        return this.componentsFound;
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
