package org.janelia.model.domain.gui.color_depth;

import java.io.File;
import java.text.ParseException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.sample.AlignedImage2d;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchType;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="colorDepthImage",label="Color Depth Image")
@SearchType(key="colorDepthImage",label="Color Depth Image")
public class ColorDepthImage extends AlignedImage2d {

    private String libraryIdentifier;

    @JsonIgnore
    private transient ColorDepthFilepathParser parser;


    private synchronized ColorDepthFilepathParser getParser() {
        if (parser==null) {
            try {
                parser = ColorDepthFilepathParser.parse(getFilepath());
            }
            catch (ParseException e) {
                throw new RuntimeException("Could not parse filepath: "+getFilepath());
            }
        }
        return parser;
    }

    public String getLibraryIdentifier() {
        return libraryIdentifier;
    }

    public void setLibraryIdentifier(String libraryIdentifier) {
        this.libraryIdentifier = libraryIdentifier;
    }

    @JsonIgnore
    public Reference getSampleRef() {
        ColorDepthFilepathParser parser = getParser();
        return parser==null?null:getParser().getSampleRef();
    }

    @JsonIgnore
    public Integer getChannelNumber() {
        ColorDepthFilepathParser parser = getParser();
        return parser==null?null:getParser().getChannelNumber();
    }

    @JsonIgnore
    public File getFile() {
        ColorDepthFilepathParser parser = getParser();
        return parser==null?null:getParser().getFile();
    }

    @JsonIgnore
    public String getSampleName() {
        ColorDepthFilepathParser parser = getParser();
        return parser==null?null:getParser().getSampleName();
    }

    @JsonIgnore
    public String getObjective() {
        ColorDepthFilepathParser parser = getParser();
        return parser==null?null:getParser().getObjective();
    }

    @JsonIgnore
    public String getAnatomicalArea() {
        ColorDepthFilepathParser parser = getParser();
        return parser==null?null:getParser().getAnatomicalArea();
    }

    @JsonIgnore
    public Map<FileType, String> getFiles() {
        return ImmutableMap.of(FileType.Unclassified2d, getFilepath());
    }

    @Override
    public void setFiles(Map<FileType, String> files) {
        throw new UnsupportedOperationException("This field is calculated from filepath and cannot be changed");
    }
}
