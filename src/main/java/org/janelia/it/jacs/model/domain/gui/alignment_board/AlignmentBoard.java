package org.janelia.it.jacs.model.domain.gui.alignment_board;

import java.util.ArrayList;
import java.util.List;

import org.janelia.it.jacs.model.domain.AbstractDomainObject;
import org.janelia.it.jacs.model.domain.support.MongoMapped;

@MongoMapped(collectionName="alignmentBoard",label="Alignment Board")
public class AlignmentBoard extends AbstractDomainObject {

    private String imageSize;
    private String opticalResolution;
    private String alignmentSpace;
    private String encodedUserSettings;
    private List<AlignmentBoardItem> children = new ArrayList<>();

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public String getOpticalResolution() {
        return opticalResolution;
    }

    public void setOpticalResolution(String opticalResolution) {
        this.opticalResolution = opticalResolution;
    }

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    public String getEncodedUserSettings() {
        return encodedUserSettings;
    }

    public void setEncodedUserSettings(String encodedUserSettings) {
        this.encodedUserSettings = encodedUserSettings;
    }

    public List<AlignmentBoardItem> getChildren() {
        return children;
    }

    public void setChildren(List<AlignmentBoardItem> children) {
        if (children==null) throw new IllegalArgumentException("Property cannot be null");
        this.children = children;
    }
    
    @Override
    public String toString() {
        return alignmentSpace + ": " + imageSize + " " + opticalResolution;
    }
}
