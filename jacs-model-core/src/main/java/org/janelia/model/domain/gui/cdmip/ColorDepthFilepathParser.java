package org.janelia.model.domain.gui.cdmip;

import java.io.File;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.sample.Sample;

/**
 * Parse a standardized color depth MIP filename to extract various metadata.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthFilepathParser {

    private File file;
    private Reference sampleRef;
    private String alignmentSpace;
    private String sampleName;
    private String objective;
    private String anatomicalArea;
    private Integer channelNumber;

    private ColorDepthFilepathParser(String filepath) throws ParseException {
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
        else {
            throw new ParseException("Could not parse path "+filepath, 0);
        }
    }

    public static ColorDepthFilepathParser parse(String filepath) throws ParseException {
        return new ColorDepthFilepathParser(filepath);
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
}
